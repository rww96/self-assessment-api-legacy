/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.r2.selfassessmentapi.resources

import javax.inject.Inject
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.connectors.{PropertiesPeriodConnector, PropertiesPeriodConnectorT}
import uk.gov.hmrc.r2.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.r2.selfassessmentapi.models.Errors.InvalidPeriod
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.models.audit.PeriodicUpdate
import uk.gov.hmrc.r2.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties._
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.{PeriodMapper, PropertiesPeriodResponse, ResponseMapper}
import uk.gov.hmrc.r2.selfassessmentapi.services.{AuditData, AuditService, AuthorisationService}

import scala.concurrent.Future


class PropertiesPeriodResource @Inject()(
                                          override val appContext: AppContext,
                                          override val authService: AuthorisationService,
                                          connector: PropertiesPeriodConnector,
                                          auditService: AuditService
                                        ) extends BaseResource {

  def createPeriod(nino: Nino, id: PropertyType): Action[JsValue] =
    APIAction(nino, SourceType.Properties, Some("periods")).async(parse.json) { implicit request =>
      validateCreateRequest(id, nino, request) map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right((periodId, response)) =>
          auditService.audit(makePeriodCreateAudit(nino, id, request.authContext, response, periodId))
          response.filter {
            case 200 =>
              Created.withHeaders(LOCATION -> response.createLocationHeader(nino, id, periodId))
            case 403 if response.errorCodeIs(DesErrorCode.NOT_FOUND_INCOME_SOURCE) =>
              Logger.warn(s"[PropertiesPeriodResource] [createPeriod#$id] - Converted NOT_FOUND_INCOME_SOURCE to NotFound")
              NotFound
            case 409 if response.errorCodeIs(DesErrorCode.BOTH_EXPENSES_SUPPLIED) =>
              Logger.warn(s"[PropertiesPeriodResource] [createPeriod#$id] - DES returned ${response.json}")
              BadRequest(Json.toJson(Errors.badRequest(Errors.BothExpensesSupplied)))
            case 409 if response.errorCodeIs(DesErrorCode.INVALID_PERIOD) =>
              Logger.warn(
                s"[PropertiesPeriodResource] [createPeriod#$id]\n" +
                  s"Received DES:\n ${response.underlying.body}\n" +
                  s"Transformed to:\n${toJson(InvalidPeriod)}.")
              BadRequest(toJson(Errors.badRequest(Errors.InvalidPeriod)))
          }
      } recoverWith exceptionHandling
    }

  def updatePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[JsValue] =
    APIAction(nino, SourceType.Properties, Some("periods")).async(parse.json) { implicit request =>
      periodId match {
        case Period(from, to) =>
          validateUpdateRequest(id, nino, Period(from, to), request) map {
            case Left(errorResult) => handleErrors(errorResult)
            case Right(response) =>
              auditService.audit(makePeriodUpdateAudit(nino, id, periodId, request.authContext, response))
              response.filter {
                case 200 => NoContent
                case 400 if response.errorCodeIs(DesErrorCode.INVALID_PERIOD) =>
                  Logger.warn(
                    s"[PropertiesPeriodResource] [createPeriod#$id]\n" +
                      s"Received from DES:\n ${response.underlying.body}\n" +
                      s"Transformed to:\n${toJson(Errors.InternalServerError)}.\n" +
                      s"CorrelationId: ${correlationId(response)}")
                  InternalServerError(toJson(Errors.InternalServerError))
                case 404 if response.errorCodeIs(DesErrorCode.NOT_FOUND_PROPERTY) =>
                  Logger.warn(s"[PropertiesPeriodResource] [createPeriod#$id] - Des Returned: ${DesErrorCode.NOT_FOUND_PROPERTY}. Returning 404")
                  NotFound
              }
          } recoverWith exceptionHandling
        case _ => Future.successful(NotFound)
      }
    }

  private def toResult[P <: Period, D <: des.properties.Period](response: PropertiesPeriodResponse)(
    implicit rm: ResponseMapper[P, D],
    pm: PeriodMapper[P, D],
    r: Reads[D],
    w: Writes[P]): Result =
    ResponseMapper[P, D].period(response).map(period => Ok(Json.toJson(period))).getOrElse(NotFound)

  def retrievePeriod(nino: Nino, id: PropertyType, periodId: PeriodId): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("periods")).async { implicit request =>
      periodId match {
        case Period(from, to) =>
          connector.retrieve(nino, from, to, id).map { response =>
            response.filter {
              case 200 =>
                id match {
                  case PropertyType.FHL => toResult[FHL.Properties, des.properties.FHL.Properties](response)
                  case PropertyType.OTHER => toResult[Other.Properties, des.properties.Other.Properties](response)
                }
            }
          } recoverWith exceptionHandling
        case _ => Future.successful(NotFound)
      }
    }

  def retrievePeriods(nino: Nino, id: PropertyType): Action[AnyContent] =
    APIAction(nino, SourceType.Properties, Some("periods")).async { implicit request =>
      connector.retrieveAll(nino, id).map { response =>
        response.filter {
          case 200 =>
            id match {
              case PropertyType.FHL =>
                ResponseMapper[FHL.Properties, des.properties.FHL.Properties]
                  .allPeriods(response)
                  .map(seq => Ok(Json.toJson(seq)))
                  .getOrElse(InternalServerError)
              case PropertyType.OTHER =>
                ResponseMapper[Other.Properties, des.properties.Other.Properties]
                  .allPeriods(response)
                  .map(seq => Ok(Json.toJson(seq)))
                  .getOrElse(InternalServerError)
            }
        }
      } recoverWith exceptionHandling
    }

  private def validateAndCreate[P <: Period, F <: Financials](nino: Nino, request: Request[JsValue])(
    implicit hc: HeaderCarrier,
    p: PropertiesPeriodConnectorT[P, F],
    r: Reads[P]): Future[Either[ErrorResult, (PeriodId, PropertiesPeriodResponse)]] =
    validate[P, (PeriodId, PropertiesPeriodResponse)](request.body) { period =>
      p.create(nino, period).map((period.periodId, _))
    }

  private def validateCreateRequest(id: PropertyType, nino: Nino, request: Request[JsValue])(
    implicit hc: HeaderCarrier): Future[Either[ErrorResult, (PeriodId, PropertiesPeriodResponse)]] = {
    implicit val a = connector.FHLPropertiesPeriodConnector
    implicit val b = connector.OtherPropertiesPeriodConnector
    id match {
      case PropertyType.OTHER => validateAndCreate[Other.Properties, Other.Financials](nino, request)
      case PropertyType.FHL => validateAndCreate[FHL.Properties, FHL.Financials](nino, request)
    }
  }

  private def validateAndUpdate[P <: Period, F <: Financials](id: PropertyType,
                                                              nino: Nino,
                                                              period: Period,
                                                              request: Request[JsValue])(
                                                               implicit hc: HeaderCarrier,
                                                               p: PropertiesPeriodConnectorT[P, F],
                                                               r: Reads[P],
                                                               w: Format[F]): Future[Either[ErrorResult, PropertiesPeriodResponse]] = {
    validate[F, PropertiesPeriodResponse](request.body)(p.update(nino, id, period, _))
  }

  private def validateUpdateRequest(id: PropertyType, nino: Nino, period: Period, request: Request[JsValue])(
    implicit hc: HeaderCarrier): Future[Either[ErrorResult, PropertiesPeriodResponse]] = {
    implicit val a = connector.FHLPropertiesPeriodConnector
    implicit val b = connector.OtherPropertiesPeriodConnector
    id match {
      case PropertyType.OTHER => validateAndUpdate[Other.Properties, Other.Financials](id, nino, period, request)
      case PropertyType.FHL => validateAndUpdate[FHL.Properties, FHL.Financials](id, nino, period, request)
    }
  }

  private def makePeriodCreateAudit(
                                     nino: Nino,
                                     id: PropertyType,
                                     authCtx: AuthContext,
                                     response: PropertiesPeriodResponse,
                                     periodId: PeriodId)(implicit hc: HeaderCarrier, request: Request[JsValue]): AuditData[PeriodicUpdate] =
    AuditData(
      detail = PeriodicUpdate(
        auditType = "submitPeriodicUpdate",
        httpStatus = response.status,
        nino = nino,
        sourceId = id.toString,
        periodId = periodId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = response.status / 100 match {
          case 2 => response.transactionReference
          case _ => None
        },
        requestPayload = request.body,
        responsePayload = response.status match {
          case 200 | 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = s"$id-property-periodic-create"
    )

  private def makePeriodUpdateAudit(nino: Nino,
                                    id: PropertyType,
                                    periodId: PeriodId,
                                    authCtx: AuthContext,
                                    response: PropertiesPeriodResponse)(
                                     implicit hc: HeaderCarrier,
                                     request: Request[JsValue]): AuditData[PeriodicUpdate] =
    AuditData(
      detail = PeriodicUpdate(
        auditType = "amendPeriodicUpdate",
        httpStatus = response.status,
        nino = nino,
        sourceId = id.toString,
        periodId = periodId,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = None,
        requestPayload = request.body,
        responsePayload = response.status match {
          case 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = s"$id-property-periodic-update"
    )
}
