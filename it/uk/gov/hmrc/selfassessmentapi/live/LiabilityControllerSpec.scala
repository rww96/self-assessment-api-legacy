package uk.gov.hmrc.selfassessmentapi.live

import uk.gov.hmrc.support.BaseFunctionalSpec

class LiabilityControllerSpec extends BaseFunctionalSpec {

  "request liability" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .post(s"/$saUtr/liabilities")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "retrieve liability" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

  "delete liability" should {
    "return a 501 response" in {
      given().userIsAuthorisedForTheResource(saUtr)
        .when()
        .delete(s"/$saUtr/liabilities/1234")
        .thenAssertThat()
        .resourceIsNotImplemented()
    }
  }

}
