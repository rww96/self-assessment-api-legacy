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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.Logger
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentBISSConnector
import uk.gov.hmrc.selfassessmentapi.httpparsers.SelfEmploymentBISSHttpParser.SelfEmploymentBISSOutcome
import uk.gov.hmrc.selfassessmentapi.models.{Errors, TaxYear}

import scala.concurrent.{ExecutionContext, Future}

object SelfEmploymentBISSService extends SelfEmploymentBISSService {
  override val connector = SelfEmploymentBISSConnector
}

trait SelfEmploymentBISSService {

  val connector: SelfEmploymentBISSConnector
  val logger: Logger = Logger(this.getClass.getSimpleName)

  def getSummary(nino: Nino,
                 taxYear: TaxYear,
                 selfEmploymentId: String)
                (implicit hc: HeaderCarrier,
                 ec: ExecutionContext): Future[SelfEmploymentBISSOutcome] = {

    logger.debug(s"[SelfEmploymentBISSService][getSummary] Get BISS for NI number: $nino with selfEmploymentId: $selfEmploymentId")

    val selfEmploymentPattern = "^X[A-Z0-9]{1}IS[0-9]{11}$"

    if (selfEmploymentId.matches(selfEmploymentPattern)) {
      connector.getSummary(nino, taxYear, selfEmploymentId)
    } else {
      Future.successful(Left(Errors.SelfEmploymentIDInvalid))
    }
  }
}
