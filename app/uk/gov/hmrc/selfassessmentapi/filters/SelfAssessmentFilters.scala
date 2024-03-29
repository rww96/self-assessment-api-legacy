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

package uk.gov.hmrc.selfassessmentapi.filters

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.play.bootstrap.filters._


case class SelfAssessmentFilters @Inject()(
                                    agentSimulationFilter: AgentSimulationFilter,
                                    emptyResponseFilter: EmptyResponseFilter,
                                    headerValidatorFilter: HeaderValidatorFilter,
                                    controllerLoggingFilter: ControllerLoggingFilter,
                                    setContentTypeFilter: SetContentTypeFilter,
                                    setXContentTypeOptionsFilter: SetXContentTypeOptionsFilter,
                                    defaultFilters: MicroserviceFilters
                                  ) extends DefaultHttpFilters(
  defaultFilters.filters :+
    agentSimulationFilter :+
    setXContentTypeOptionsFilter :+
    emptyResponseFilter :+
    headerValidatorFilter :+
    controllerLoggingFilter :+
    setContentTypeFilter : _*)