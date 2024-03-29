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

package uk.gov.hmrc.selfassessmentapi.filters

import akka.stream.Materializer
import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.routing.Router.Tags
import uk.gov.hmrc.api.controllers.{ErrorAcceptHeaderInvalid, HeaderValidator}
import uk.gov.hmrc.selfassessmentapi.config.ControllerConfiguration

import scala.concurrent.Future

class HeaderValidatorFilter @Inject()(implicit val mat: Materializer, controllerConfiguration: ControllerConfiguration) extends Filter with HeaderValidator {

  def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    val controller = rh.tags.get(Tags.ROUTE_CONTROLLER)

    val needsHeaderValidation =
      controller.forall(
        name =>
          controllerConfiguration
            .controllerParamsConfig(name)
            .needsHeaderValidation)

    if (!needsHeaderValidation || acceptHeaderValidationRules(rh.headers.get("Accept"))) next(rh)
    else Future.successful(Status(ErrorAcceptHeaderInvalid.httpStatusCode)(toJson(ErrorAcceptHeaderInvalid)))

  }

}
