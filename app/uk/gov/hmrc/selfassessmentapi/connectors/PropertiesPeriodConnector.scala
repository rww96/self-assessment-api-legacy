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

package uk.gov.hmrc.selfassessmentapi.connectors

import javax.inject.Inject
import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.models.properties._
import uk.gov.hmrc.selfassessmentapi.models.{Financials, Period, des}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.PropertiesPeriodResponse

import scala.concurrent.{ExecutionContext, Future}


class PropertiesPeriodConnector @Inject()(
                                           override val http: DefaultHttpClient,
                                           override val appContext: AppContext
                                         ) extends BaseConnector {

  private lazy val baseUrl: String = appContext.desUrl

  def retrieveAll(nino: Nino, propertyType: PropertyType)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertiesPeriodResponse] =
    httpGet[PropertiesPeriodResponse](
      baseUrl + s"/income-tax/nino/$nino/uk-properties/$propertyType/periodic-summaries",
      PropertiesPeriodResponse)

}
