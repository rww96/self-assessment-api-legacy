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

package uk.gov.hmrc.selfassessmentapi.repositories.domain

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONDouble, BSONObjectID, BSONString}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.DividendIncome
import uk.gov.hmrc.selfassessmentapi.controllers.api.dividend.DividendIncomeType.DividendIncomeType
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxYear, dividend, _}


case class DividendIncomeSummary(summaryId: SummaryId,
                                          `type`: DividendIncomeType,
                                          amount: BigDecimal) extends Summary {
  val arrayName = DividendIncomeSummary.arrayName

  def toDividendIncome: DividendIncome =
    DividendIncome(id = Some(summaryId),
      `type` = `type`,
      amount = amount)

  def toBsonDocument = BSONDocument(
    "summaryId" -> summaryId,
    "amount" -> BSONDouble(amount.doubleValue()),
    "type" -> BSONString(`type`.toString)
  )
}

object DividendIncomeSummary {

  val arrayName = "incomes"

  implicit val format = Json.format[DividendIncomeSummary]

  def toMongoSummary(dividend: DividendIncome, id: Option[SummaryId] = None): DividendIncomeSummary = {
    DividendIncomeSummary(
      summaryId = id.getOrElse(BSONObjectID.generate.stringify),
      `type` = dividend.`type`,
      amount = dividend.amount
    )
  }
}

case class MongoDividend(id: BSONObjectID,
                         sourceId: SourceId,
                         saUtr: SaUtr,
                         taxYear: TaxYear,
                         lastModifiedDateTime: DateTime,
                         createdDateTime: DateTime,
                         incomes: Seq[DividendIncomeSummary] = Nil) extends SourceMetadata {

  def toDividend = dividend.Dividend(id = Some(sourceId))
}

object MongoDividend {
  implicit val dateTimeFormat = ReactiveMongoFormats.dateTimeFormats
  implicit val localDateFormat = ReactiveMongoFormats.localDateFormats

  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    Format(Json.reads[MongoDividend], Json.writes[MongoDividend])
  })

  def create(saUtr: SaUtr, taxYear: TaxYear, se: dividend.Dividend): MongoDividend = {
    val id = BSONObjectID.generate
    val now = DateTime.now(DateTimeZone.UTC)
    MongoDividend(
      id = id,
      sourceId = id.stringify,
      saUtr = saUtr,
      taxYear = taxYear,
      lastModifiedDateTime = now,
      createdDateTime = now
    )
  }
}
