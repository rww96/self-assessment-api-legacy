/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.resources.Errors.Error
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, PeriodId, SourceType}
import uk.gov.hmrc.selfassessmentapi.controllers.{GenericErrorResult, ValidationErrorResult}
import uk.gov.hmrc.selfassessmentapi.domain.PeriodContainer
import uk.gov.hmrc.selfassessmentapi.resources.SelfEmploymentsResource._
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.Period
import uk.gov.hmrc.selfassessmentapi.services.PeriodService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class PeriodResource[ID <: String, P <: Period : Format, PC <: PeriodContainer[P, PC]] {

  val service: PeriodService[ID, P, PC]
  val sourceType: SourceType

  private lazy val featureSwitch = FeatureSwitchAction(sourceType, "periods")

  def createPeriod(nino: Nino, id: ID): Action[JsValue] = featureSwitch.asyncFeatureSwitch { request =>
    validate[P, Either[Error, PeriodId]](request.body) { period =>
      service.createPeriod(nino, id, period)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case Right(periodId) => Created.withHeaders(LOCATION -> s"/ni/$nino/${sourceType.name}/$id/periods/$periodId")
        case Left(error) =>
          if (error.code == ErrorCode.NOT_FOUND.toString) NotFound
          else Forbidden(Json.toJson(Errors.businessError(error)))
      }
    }
  }

  def updatePeriod(nino: Nino, id: ID, periodId: PeriodId) = featureSwitch.asyncFeatureSwitch { request =>
    validate[P, Boolean](request.body) {
      service.updatePeriod(nino, id, periodId, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrievePeriod(nino: Nino, id: ID, periodId: PeriodId) = featureSwitch.asyncFeatureSwitch {
    service.retrievePeriod(nino, id, periodId) map {
      case Some(period) => Ok(Json.toJson(period))
      case None => NotFound
    }
  }

  def retrievePeriods(nino: Nino, id: ID) = featureSwitch.asyncFeatureSwitch {
    service.retrieveAllPeriods(nino, id).map { periods => Ok(Json.toJson(periods)) }
  }
}