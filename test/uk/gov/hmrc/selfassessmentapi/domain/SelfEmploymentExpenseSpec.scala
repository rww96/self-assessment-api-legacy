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

package uk.gov.hmrc.selfassessmentapi.domain

import SelfEmploymentExpenseCategory._

class SelfEmploymentExpenseSpec extends JsonSpec {

  "format" should {
    "round trip SelfEmploymentIncome json when id present" in {
      SelfEmploymentExpenseCategory.values.foreach {
        cat => roundTripJson(SelfEmploymentExpense(id = Some("idm"), taxYear = "2016-17", category = cat, amount = BigDecimal(1000.99)))
      }
    }

    "round trip SelfEmployment json with no id" in {
      SelfEmploymentExpenseCategory.values.foreach {
        cat => roundTripJson(SelfEmploymentExpense(id = None, taxYear = "2016-17", category = cat, amount = BigDecimal(1000.99)))
      }
    }
  }

  "validate" should {
    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.1234), BigDecimal(1000.12345), BigDecimal(1000.123456789)).foreach { testAmount =>
        val seIncome = SelfEmploymentExpense(taxYear = "2016-17", category = CISPayments, amount = testAmount)
        assertValidationError[SelfEmploymentExpense](
          seIncome,
          Map(ErrorCode("INVALID_MONETARY_AMOUNT") -> "amount cannot have more than 2 decimal values"),
          "Expected invalid self-employment-income")
      }
    }

    "reject taxYear with invalid formats" in {
      Seq("2014-15", "2016-2017", "2016/17", "2016/2017", "2016 17", "20162017").foreach { testTaxYear =>
        val seIncome = SelfEmploymentExpense(taxYear = testTaxYear, category = CISPayments, amount = BigDecimal(1000.99))
        assertValidationError[SelfEmploymentExpense](
          seIncome,
          Map(ErrorCode("TAX_YEAR_INVALID") -> "tax year must be 2016-17"),
          "Expected invalid self-employment-income")
      }
    }
  }

}