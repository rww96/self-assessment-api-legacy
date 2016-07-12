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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation.steps

import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBand.{AdditionalHigherTaxBand, BasicTaxBand, HigherTaxBand}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.TaxBandSummary
import uk.gov.hmrc.selfassessmentapi.{SelfEmploymentSugar, UnitSpec}

class PayPensionProfitsTaxCalculationSpec extends UnitSpec with SelfEmploymentSugar {

  "run" should {

    "calculate tax for total pay pension and profit received lesser than 32000" in {
      val liability = aLiability().copy(payPensionProfitsReceived = Some(BigDecimal(31999)))
      val result = PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      result.payPensionsProfits shouldBe
        Seq(TaxBandSummary(31999, BasicTaxBand),
            TaxBandSummary(0, HigherTaxBand),
            TaxBandSummary(0, AdditionalHigherTaxBand))
    }

    "calculate tax for total pay pension and profit received equal to 32000" in {
      val liability = aLiability().copy(payPensionProfitsReceived = Some(BigDecimal(32000)))
      val result = PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      result.payPensionsProfits shouldBe
        Seq(TaxBandSummary(32000, BasicTaxBand),
            TaxBandSummary(0, HigherTaxBand),
            TaxBandSummary(0, AdditionalHigherTaxBand))
    }

    "calculate tax for total pay pension and profit received greater than 32000 but lesser than 150000" in {
      val liability = aLiability().copy(payPensionProfitsReceived = Some(BigDecimal(60000)))
      val result = PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      result.payPensionsProfits shouldBe
        Seq(TaxBandSummary(32000, BasicTaxBand),
            TaxBandSummary(28000, HigherTaxBand),
            TaxBandSummary(0, AdditionalHigherTaxBand))
    }

    "calculate tax for total pay pension and profit received equal to 150000" in {
      val liability = aLiability().copy(payPensionProfitsReceived = Some(BigDecimal(150000)))
      val result = PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      result.payPensionsProfits shouldBe
        Seq(TaxBandSummary(32000, BasicTaxBand),
            TaxBandSummary(118000, HigherTaxBand),
            TaxBandSummary(0, AdditionalHigherTaxBand))
    }

  "calculate tax for total pay pension and profit received  greater than 150000" in {
      val liability = aLiability().copy(payPensionProfitsReceived = Some(BigDecimal(300000)))
      val result = PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      result.payPensionsProfits shouldBe
        Seq(TaxBandSummary(32000, BasicTaxBand),
            TaxBandSummary(118000, HigherTaxBand),
            TaxBandSummary(150000, AdditionalHigherTaxBand))
    }

    "throw exception if payPensionProfitsReceived is None" in {
      val liability = aLiability()
      intercept[IllegalStateException]{
        PayPensionProfitsTaxCalculation.run(SelfAssessment(), liability)
      }
    }
  }
}
