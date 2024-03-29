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

package uk.gov.hmrc.selfassessmentapi.mocks.config

import org.scalatest.Suite
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.mocks.Mock

trait MockAppContext extends Mock { _: Suite =>

  val mockAppContext = mock[AppContext]

  object MockAppContext {
    def featureSwitch = when(mockAppContext.featureSwitch)
    def authEnabled = when(mockAppContext.authEnabled)
    def desToken = when(mockAppContext.desToken)
    def desEnv = when(mockAppContext.desEnv)
    def desUrl = when(mockAppContext.desUrl)
    def mtdDate = when(mockAppContext.mtdDate)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppContext)
  }
}
