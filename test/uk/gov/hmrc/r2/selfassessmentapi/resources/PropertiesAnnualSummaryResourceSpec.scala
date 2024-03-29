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

package uk.gov.hmrc.r2.selfassessmentapi.resources

import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import uk.gov.hmrc.r2.selfassessmentapi.mocks.connectors.MockPropertiesAnnualSummaryConnector
import uk.gov.hmrc.r2.selfassessmentapi.mocks.services.MockAuditService
import uk.gov.hmrc.r2.selfassessmentapi.models.SourceType
import uk.gov.hmrc.r2.selfassessmentapi.models.properties._

import scala.concurrent.Future


class PropertiesAnnualSummaryResourceSpec extends ResourceSpec
  with MockPropertiesAnnualSummaryConnector
  with MockAuditService {

  class Setup {
    val resource = new PropertiesAnnualSummaryResource(
      mockAppContext,
      mockAuthorisationService,
      mockPropertiesAnnualSummaryConnector,
      mockAuditService
    )
    mockAPIAction(SourceType.Properties)
  }

  val otherPropertiesAllowances = OtherPropertiesAllowances(
    annualInvestmentAllowance = Some(0.0),
    otherCapitalAllowance = Some(0.0),
    costOfReplacingDomesticItems = Some(0.0),
    zeroEmissionsGoodsVehicleAllowance = Some(0.0),
    businessPremisesRenovationAllowance = Some(0.0),
    propertyAllowance = Some(0.0)
  )
  val otherPropertiesAdjustments = OtherPropertiesAdjustments(
    lossBroughtForward = Some(0.0),
    privateUseAdjustment = Some(0.0),
    balancingCharge = Some(0.0),
    bpraBalancingCharge = Some(0.0)
  )
  val otherPropertiesOther = OtherPropertiesOther(
    nonResidentLandlord = Some(false),
    rarJointLet = Some(false)
  )

  val otherPropertiesAnnualSummary: PropertiesAnnualSummary = OtherPropertiesAnnualSummary(
    Some(otherPropertiesAllowances),
    Some(otherPropertiesAdjustments),
    Some(otherPropertiesOther)
  )
  val otherPropertiesAnnualSummaryJson = Jsons.Properties.otherAnnualSummary(rarJointLet = false)

  "updateAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        val request = FakeRequest().withBody[JsValue](otherPropertiesAnnualSummaryJson)

        MockPropertiesAnnualSummaryConnector.update(nino, PropertyType.OTHER, taxYear, otherPropertiesAnnualSummary)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.updateAnnualSummary(nino, PropertyType.OTHER, taxYear)(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }

  "retrieveAnnualSummary" should {
    "return a 500" when {
      "the connector returns a failed future" in new Setup {
        MockPropertiesAnnualSummaryConnector.get(nino, PropertyType.OTHER, taxYear)
          .returns(Future.failed(new RuntimeException("something went wrong")))

        val result = resource.retrieveAnnualSummary(nino, PropertyType.OTHER, taxYear)(FakeRequest())
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentType(result) shouldBe None
      }
    }
  }
}
