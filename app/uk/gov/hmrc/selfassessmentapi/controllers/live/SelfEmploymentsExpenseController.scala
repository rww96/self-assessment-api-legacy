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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.ErrorNotImplemented
import uk.gov.hmrc.selfassessmentapi.domain._

import scala.concurrent.Future

object SelfEmploymentsExpenseController extends uk.gov.hmrc.selfassessmentapi.controllers.SelfEmploymentsExpenseController {

  override def create(saUtr: SaUtr, seId: SelfEmploymentId) = Action.async(parse.json) { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def findById(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def find(saUtr: SaUtr, seId: SelfEmploymentId) = Action.async { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def update(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action.async(parse.json)  { _ =>
    Future.successful(NotImplemented(Json.toJson(ErrorNotImplemented)))
  }

  override def delete(saUtr: SaUtr, seId: SelfEmploymentId, seExpenseId: SelfEmploymentExpenseId) = Action { _ =>
   NotImplemented(Json.toJson(ErrorNotImplemented))
  }
}
