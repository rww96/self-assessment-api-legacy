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

package uk.gov.hmrc.selfassessmentapi.models.des

import play.api.libs.json.{Json, Reads}

case class CrystObligations(obligations: Seq[CrystObligation])

object CrystObligations {
  implicit val reads: Reads[CrystObligations] = Json.reads[CrystObligations]
}

case class CrystObligation(identification: ObligationIdentification, obligationDetails: Seq[ObligationDetail])

object CrystObligation {
  implicit val reads: Reads[CrystObligation] = Json.reads[CrystObligation]
}

case class ObligationIdentification(incomeSourceType: String, referenceNumber: String, referenceType: String)

object ObligationIdentification {
  implicit val reads: Reads[ObligationIdentification] = Json.reads[ObligationIdentification]
}