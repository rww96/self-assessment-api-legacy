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

import uk.gov.hmrc.r2.selfassessmentapi.models.properties.PropertyType
import uk.gov.hmrc.r2.selfassessmentapi.support.BaseFunctionalSpec

class PropertiesAnnualSummaryResourceISpec extends BaseFunctionalSpec {

  "amending annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 204 when amending annual summaries for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillBeUpdatedFor(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 400 when amending annual summaries with invalid data for $propertyType" in {
        val expectedJson = Jsons.Errors.invalidRequest(
          "INVALID_MONETARY_AMOUNT" -> "/allowances/annualInvestmentAllowance",
          "INVALID_MONETARY_AMOUNT" -> "/adjustments/privateUseAdjustment")

        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .when()
          .put(invalidAnnualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson.toString)
      }

      s"return code 404 when amending annual summaries for a properties business that does not exist for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des()
          .properties
          .willBeCreatedFor(nino)
          .des().properties.annualSummaryWillNotBeReturnedFor(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 404 when amending annual summaries and DES returns a NOT_FOUND_PROPERTY error for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillNotBeReturnedDueToNotFoundProperty(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 204 when amending annual summaries and DES returns a 204 response for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.logicallyDeleted(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 204 when amending annual summaries and DES returns a 410 GONE error for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.alreadyLogicallyDeleted(nino, propertyType, taxYear)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(204)
      }

      s"return code 500 when provided with an invalid Originator-Id header for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().invalidOriginatorIdFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 400 when provided with an invalid payload for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().payloadFailsValidationFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(400)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.invalidRequest)
      }

      s"return code 400 when updating properties annual summary for a non MTD year for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/2015-16")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsError("TAX_YEAR_INVALID")
      }

      s"return code 500 when DES is experiencing problems for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().serverErrorFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when a dependent system is not responding for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().serviceUnavailableFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().isATeapotFor(nino)
          .when()
          .put(annualSummary(propertyType)).at(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  "amending annual summaries for FHL Property business" should {
    "return code 400 when submitted with invalid data for 'Period of grace adjustment'" in {

      given()
        .userIsSubscribedToMtdFor(nino)
        .clientIsFullyAuthorisedForTheResource
        .when()
        .put(s"/r2/ni/$nino/uk-properties/${PropertyType.FHL}/$taxYear", Some(Jsons.Properties.invalidFhlAnnualSummary))
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(Jsons.Errors.invalidRequest("INVALID_BOOLEAN_VALUE" -> "/adjustments/periodOfGraceAdjustment"))
    }
  }

  "retrieving annual summaries" should {
    for (propertyType <- Seq(PropertyType.OTHER, PropertyType.FHL)) {
      s"return code 200 containing annual summary information for $propertyType" in {
        val expectedJson = annualSummary(propertyType).toString()
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillBeReturnedFor(nino, propertyType, taxYear, desAnnualSummary(propertyType))
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsJson()
          .bodyIsLike(expectedJson)
      }

      s"return code 404 when no data can be found for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.noAnnualSummaryFor(nino, propertyType, taxYear)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 404 when retrieving an annual summary for a non-existent property for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillNotBeReturnedDueToNotFoundProperty(nino, propertyType, taxYear)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 404 when retrieving an annual summary for a non-existent period for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().properties.annualSummaryWillNotBeReturnedDueToNotFoundPeriod(nino, propertyType, taxYear)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(404)
      }

      s"return code 400 when retrieving annual summary for a non MTD year for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/2015-16")
          .thenAssertThat()
          .statusIs(400)
          .bodyIsError("TAX_YEAR_INVALID")
      }

      s"return code 500 when provided with an invalid Originator-Id header for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().invalidOriginatorIdFor(nino)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when DES is experiencing problems for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().serverErrorFor(nino)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when a dependent system is not responding for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().serviceUnavailableFor(nino)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
          .contentTypeIsJson()
          .bodyIsLike(Jsons.Errors.internalServerError)
      }

      s"return code 500 when we receive a status code from DES that we do not handle for $propertyType" in {
        given()
          .userIsSubscribedToMtdFor(nino)
          .clientIsFullyAuthorisedForTheResource
          .des().isATeapotFor(nino)
          .when()
          .get(s"/r2/ni/$nino/uk-properties/$propertyType/$taxYear")
          .thenAssertThat()
          .statusIs(500)
      }
    }
  }

  private def annualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary(rarJointLet = false)
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary()
  }

  private def invalidAnnualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => Jsons.Properties.otherAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      otherCapitalAllowance = 1000.20,
      zeroEmissionsGoodsVehicleAllowance = 50.50,
      costOfReplacingDomesticItems = 150.55,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34,
      bpraBalancingCharge = 0.0,
      nonResidentLandlord = true,
      rarJointLet = false
    )
    case PropertyType.FHL => Jsons.Properties.fhlAnnualSummary(
      annualInvestmentAllowance = -10000.50,
      otherCapitalAllowance = 1000.20,
      lossBroughtForward = 20.22,
      privateUseAdjustment = -22.23,
      balancingCharge = 350.34,
      periodOfGraceAdjustment = true)
  }

  private def desAnnualSummary(propertyType: PropertyType.Value) = propertyType match {
    case PropertyType.OTHER => DesJsons.Properties.AnnualSummary.other
    case PropertyType.FHL => DesJsons.Properties.AnnualSummary.fhl
  }
}
