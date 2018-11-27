/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.r2.selfassessmentapi.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.r2.selfassessmentapi.models._

case class FHLPropertiesAdjustments(lossBroughtForward: Option[BigDecimal] = None,
                                    privateUseAdjustment: Option[BigDecimal] = None,
                                    balancingCharge: Option[BigDecimal] = None,
                                    periodOfGraceAdjustment: Option[Boolean] = None)

object FHLPropertiesAdjustments {
  implicit val writes: Writes[FHLPropertiesAdjustments] = Json.writes[FHLPropertiesAdjustments]

  implicit val reads: Reads[FHLPropertiesAdjustments] = (
    (__ \ "lossBroughtForward").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "privateUseAdjustment").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "balancingCharge").readNullable[BigDecimal](nonNegativeAmountValidator) and
      (__ \ "periodOfGraceAdjustment").readNullable[Boolean]
    ) (FHLPropertiesAdjustments.apply _)
}