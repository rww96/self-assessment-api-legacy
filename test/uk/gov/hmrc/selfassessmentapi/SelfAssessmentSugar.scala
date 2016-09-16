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

package uk.gov.hmrc.selfassessmentapi

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{TaxDeducted => _, TaxYearProperties => _, _}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Liability, SelfAssessment => _, _}

object SelfAssessmentSugar extends UnitSpec {

  def aLiability(saUtr: SaUtr = generateSaUtr(),
                 taxYear: TaxYear = taxYear,
                 incomeFromEmployments: Seq[EmploymentIncome] = Nil,
                 profitFromSelfEmployments: Seq[SelfEmploymentIncome] = Nil,
                 interestFromUKBanksAndBuildingSocieties: Seq[InterestFromUKBanksAndBuildingSocieties] = Nil,
                 dividendsFromUKSources: Seq[DividendsFromUKSources] = Nil,
                 deductionsRemaining: Option[BigDecimal] = Some(0),
                 personalSavingsAllowance: Option[BigDecimal] = None,
                 retirementAnnuityContract: Option[BigDecimal] = None,
                 savingsStartingRate: Option[BigDecimal] = None,
                 profitFromUkProperties: Seq[UkPropertyIncome] = Nil,
                 incomeFromFurnishedHolidayLettings: Seq[FurnishedHolidayLettingIncome] = Nil): Liability = {

    val iD = BSONObjectID.generate
    Liability(id = iD,
              liabilityId = iD.stringify,
              saUtr = saUtr,
              taxYear = taxYear,
              employmentIncome = incomeFromEmployments,
              selfEmploymentIncome = profitFromSelfEmployments,
              ukPropertyIncome = Nil,
              furnishedHolidayLettingsIncome = Nil,
              savingsIncome = interestFromUKBanksAndBuildingSocieties,
              ukDividendsIncome = dividendsFromUKSources,
              totalIncomeReceived = 0,
              totalTaxableIncome = 0,
              allowancesAndReliefs = AllowancesAndReliefs(personalSavingsAllowance = personalSavingsAllowance,
                                                          savingsStartingRate = savingsStartingRate,
                                                          retirementAnnuityContract = retirementAnnuityContract),
              taxDeducted = TaxDeducted(),
              dividendTaxBandSummary = Nil,
              savingsTaxBandSummary = Nil,
              nonSavingsTaxBandSummary = Nil,
              pensionSavingsChargesSummary = Nil,
              taxes = TaxesCalculated(
                totalIncomeTax = 0,
                totalTaxDeducted = 0,
                totalTaxDue = 0,
                totalTaxOverPaid = 0,
                pensionSavingsCharges = 0,
                taxPaidByPensionScheme = 0
              )
    )
  }

  def now = DateTime.now(DateTimeZone.UTC)

  def aTaxYearProperty = TaxYearProperties(BSONObjectID.generate, generateSaUtr(), taxYear, now, now)

  def aSelfAssessment(employments: Seq[Employment] = Nil,
                      selfEmployments: Seq[SelfEmployment] = Nil,
                      unearnedIncomes: Seq[UnearnedIncome] = Nil,
                      ukProperties: Seq[UKProperties] = Nil,
                      taxYearProperties: Option[api.TaxYearProperties] = None,
                      furnishedHolidayLettings: Seq[FurnishedHolidayLettings] = Nil) =
    SelfAssessment(employments,
                   selfEmployments,
                   unearnedIncomes,
                   ukProperties,
                   taxYearProperties,
                   furnishedHolidayLettings)

}