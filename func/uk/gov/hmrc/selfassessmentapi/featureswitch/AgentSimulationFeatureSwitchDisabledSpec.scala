package uk.gov.hmrc.selfassessmentapi.featureswitch

import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.XTestScenarioHeader
import uk.gov.hmrc.support.BaseFunctionalSpec

class AgentSimulationFeatureSwitchDisabledSpec extends BaseFunctionalSpec {

  private val conf =
    Map("Test" ->
      Map("feature-switch" ->
        Map("agent-simulation" ->
          Map("enabled" -> false)
        )
      )
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "Agent simulation filters" should {
    "not be applied if feature is switched off" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/ni/$nino/self-employments")
        .withHeaders(XTestScenarioHeader, "AGENT_NOT_SUBSCRIBED")
        .thenAssertThat()
        .statusIs(200)
    }
  }

}

