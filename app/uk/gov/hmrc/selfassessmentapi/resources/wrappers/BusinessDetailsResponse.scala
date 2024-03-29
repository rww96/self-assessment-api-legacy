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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import uk.gov.hmrc.selfassessmentapi.models.MtdId
import uk.gov.hmrc.http.HttpResponse

case class BusinessDetailsResponse(underlying: HttpResponse) extends Response {
  def mtdId: Option[MtdId] =
    (json \ "mtdbsa").asOpt[String] match {
      case Some(id) =>
        Some(MtdId(id))
      case None =>
        logger.warn(s"The response from DES does not match the expected format. JSON: [$json]")
        None
    }
}
