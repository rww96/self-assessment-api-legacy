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

package uk.gov.hmrc.selfassessmentapi.models.des.charitablegiving

import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, __, _}
import uk.gov.hmrc.selfassessmentapi.models.{Amount, charitablegiving, nonNegativeAmountValidator}


case class CharitableGivings(giftAidPayments: Option[GiftAidPayments],
                             gifts: Option[Gifts])

object CharitableGivings{
  implicit val format: Format[CharitableGivings] = Json.format[CharitableGivings]

  def from(charitableGivings: charitablegiving.CharitableGivings): CharitableGivings = {
    CharitableGivings(giftAidPayments = GiftAidPayments.from(charitableGivings.giftAidPayments),
      gifts = Gifts.from(charitableGivings.gifts))
  }
}

case class GiftAidPayments(currentYear: Option[Amount] = None,
                           oneOffCurrentYear: Option[Amount] = None,
                           currentYearTreatedAsPreviousYear: Option[Amount] = None,
                           nextYearTreatedAsCurrentYear: Option[Amount] = None,
                           nonUKCharities: Option[Amount] = None
                          )
object GiftAidPayments{

  implicit val writes: Writes[GiftAidPayments] = Json.writes[GiftAidPayments]

  implicit val reads: Reads[GiftAidPayments] = (
    (__ \ "currentYear").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "oneOffCurrentYear").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "currentYearTreatedAsPreviousYear").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "nextYearTreatedAsCurrentYear").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "nonUKCharities").readNullable[Amount](nonNegativeAmountValidator)
    )(GiftAidPayments.apply _)

  def from(giftAidPayments: Option[charitablegiving.GiftAidPayments]): Option[GiftAidPayments] = {
    giftAidPayments match {
      case Some(payments) =>
        Some(GiftAidPayments(currentYear = payments.currentYear,
          oneOffCurrentYear = payments.oneOffCurrentYear,
          currentYearTreatedAsPreviousYear = payments.currentYearTreatedAsPreviousYear,
          nextYearTreatedAsCurrentYear = payments.nextYearTreatedAsCurrentYear,
          nonUKCharities = payments.nonUKCharities))
      case None => None
    }
  }
}

case class Gifts(landAndBuildings: Option[Amount] = None,
                 sharesOrSecurities: Option[Amount] = None,
                 investmentsNonUKCharities: Option[Amount] = None)

object Gifts {

  implicit val writes: Writes[Gifts] = Json.writes[Gifts]

  implicit val reads: Reads[Gifts] = (
    (__ \ "landAndBuildings").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "sharesOrSecurities").readNullable[Amount](nonNegativeAmountValidator) and
      (__ \ "investmentsNonUKCharities").readNullable[Amount](nonNegativeAmountValidator)
    )(Gifts.apply _)

  def from(gifts: Option[charitablegiving.Gifts]): Option[Gifts] = {
    gifts match {
      case Some(gift) =>
        Some(Gifts(landAndBuildings = gift.landAndBuildings,
          sharesOrSecurities = gift.sharesOrSecurities,
          investmentsNonUKCharities = gift.investmentsNonUKCharities))
      case None => None
    }
  }
}