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

package uk.gov.hmrc.selfassessmentapi.models.calculation

import uk.gov.hmrc.selfassessmentapi.resources.JsonSpec
import uk.gov.hmrc.selfassessmentapi.models.{ErrorCode, TaxYear}

class CalculationRequestSpec extends JsonSpec {

  "CalculationRequest JSON" should {

    "round trip" in {
      roundTripJson(CalculationRequest(TaxYear("2017-18")))
    }

    "return a TAX_YEAR_INVALID error when account name is too long" in {
      val input = CalculationRequest(TaxYear("2013-14"))
      assertValidationErrorWithCode(input, "/taxYear", ErrorCode.TAX_YEAR_INVALID)
    }
  }
}
