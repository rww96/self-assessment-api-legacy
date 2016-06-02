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

package uk.gov.hmrc.selfassessmentapi.domain.unearnedincome

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.definition.EnumJson
import uk.gov.hmrc.selfassessmentapi.domain._
import uk.gov.hmrc.selfassessmentapi.domain.unearnedincome.SavingsIncomeType.SavingsIncomeType

object SavingsIncomeType extends Enumeration {
  type SavingsIncomeType = Value
  val InterestFromBanksTaxed, InterestFromBanksUntaxed = Value
}

case class SavingsIncome(id: Option[String] = None, `type`: SavingsIncomeType, amount: BigDecimal)

object SavingsIncome extends BaseDomain[SavingsIncome] {

  implicit val savingsIncomeTypes = EnumJson.enumFormat(SavingsIncomeType, Some("Unearned income savings income type is invalid"))
  implicit val writes = Json.writes[SavingsIncome]

  implicit val reads: Reads[SavingsIncome] = (
    Reads.pure(None) and
      (__ \ "type").read[SavingsIncomeType] and
      (__ \ "amount").read[BigDecimal](positiveAmountValidator("amount"))
    ) (SavingsIncome.apply _)

  override def example(id: Option[SummaryId] = None) = SavingsIncome(id, SavingsIncomeType.InterestFromBanksTaxed, BigDecimal(1000.00))
}
