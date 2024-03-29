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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.r2.selfassessmentapi.connectors.SelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.r2.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.r2.selfassessmentapi.models._
import uk.gov.hmrc.r2.selfassessmentapi.models.audit.AnnualSummaryUpdate
import uk.gov.hmrc.r2.selfassessmentapi.models.des.DesErrorCode
import uk.gov.hmrc.r2.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.SelfEmploymentAnnualSummaryResponse
import uk.gov.hmrc.r2.selfassessmentapi.services.{AuditData, AuditService, AuthorisationService}
//import uk.gov.hmrc.r2.selfassessmentapi.services.AuditService.audit
import play.api.libs.concurrent.Execution.Implicits._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext


class SelfEmploymentAnnualSummaryResource @Inject()(
                                                     override val appContext: AppContext,
                                                     override val authService: AuthorisationService,
                                                     connector: SelfEmploymentAnnualSummaryConnector,
                                                     auditService: AuditService
                                                   ) extends BaseResource {

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] =
    APIAction(nino, SourceType.SelfEmployments, Some("annual")).async(parse.json) { implicit request =>
      validate[SelfEmploymentAnnualSummary, SelfEmploymentAnnualSummaryResponse](request.body) { summary =>
        connector.update(nino, id, taxYear, des.selfemployment.SelfEmploymentAnnualSummary.from(summary))
      } map {
        case Left(errorResult) => handleErrors(errorResult)
        case Right(response) =>
          auditService.audit(makeAnnualSummaryUpdateAudit(nino, id, taxYear, request.authContext, response))
          response.filter {
            case 200 => NoContent
            case 204 => NoContent
            case 410 if response.errorCodeIs(DesErrorCode.GONE) => NoContent
          }
      } recoverWith exceptionHandling
    }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] =
    APIAction(nino, SourceType.SelfEmployments, Some("annual")).async { implicit request =>
      connector.get(nino, id, taxYear).map { response =>
        response.filter {
          case 200 => response.annualSummary.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
        }
      } recoverWith exceptionHandling
    }

  private def makeAnnualSummaryUpdateAudit(nino: Nino,
                                           id: SourceId,
                                           taxYear: TaxYear,
                                           authCtx: AuthContext,
                                           response: SelfEmploymentAnnualSummaryResponse)(
                                            implicit hc: HeaderCarrier,
                                            request: Request[JsValue]): AuditData[AnnualSummaryUpdate] =
    AuditData(
      detail = AnnualSummaryUpdate(
        httpStatus = response.status,
        nino = nino,
        sourceId = id,
        taxYear = taxYear,
        affinityGroup = authCtx.affinityGroup,
        agentCode = authCtx.agentCode,
        transactionReference = response.status match {
          case 200 => response.transactionReference
          case _ => None
        },
        requestPayload = request.body,
        responsePayload = response.status match {
          case 400 => Some(response.json)
          case _ => None
        }
      ),
      transactionName = "self-employment-annual-summary-update"
    )
}
