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

package uk.gov.hmrc.selfassessmentapi.mocks.connectors

import org.scalatest.Suite
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentBISSConnector
import uk.gov.hmrc.selfassessmentapi.mocks.Mock
import uk.gov.hmrc.selfassessmentapi.models.TaxYear

trait MockSelfEmploymentBISSConnector extends Mock{ _: Suite =>

  val mockSelfEmploymentBISSConnector = mock[SelfEmploymentBISSConnector]

  object MockSelfEmploymentBISSConnector {

    def getSummary(nino: Nino, taxYear: TaxYear, id: String) = {
      when(mockSelfEmploymentBISSConnector.getSummary(eqTo(nino), eqTo(taxYear), eqTo(id))(any(), any()))
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfEmploymentBISSConnector)
  }
}
