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

package uk.gov.hmrc.r2.selfassessmentapi.connectors

import javax.inject.Inject
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.r2.selfassessmentapi.config.AppContext
import uk.gov.hmrc.r2.selfassessmentapi.models.des.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.r2.selfassessmentapi.models.{SourceId, TaxYear}
import uk.gov.hmrc.r2.selfassessmentapi.resources.wrappers.SelfEmploymentAnnualSummaryResponse

import scala.concurrent.{ExecutionContext, Future}


class SelfEmploymentAnnualSummaryConnector @Inject()(
                                                      override val appContext: AppContext,
                                                      override val http: DefaultHttpClient
                                                    ) extends BaseConnector {

  val baseUrl: String = appContext.desUrl

  def update(nino: Nino, id: SourceId, taxYear: TaxYear, update: SelfEmploymentAnnualSummary)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentAnnualSummaryResponse] =
    httpPut[SelfEmploymentAnnualSummary, SelfEmploymentAnnualSummaryResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}",
      update,
      SelfEmploymentAnnualSummaryResponse)

  def get(nino: Nino, id: SourceId, taxYear: TaxYear)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SelfEmploymentAnnualSummaryResponse] =
    httpGet[SelfEmploymentAnnualSummaryResponse](
      baseUrl + s"/income-store/nino/$nino/self-employments/$id/annual-summaries/${taxYear.toDesTaxYear}",
      SelfEmploymentAnnualSummaryResponse)

}
