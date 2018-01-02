/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models.properties

import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.models.des.properties

case class Properties()

object Properties {
  // these odd choices are a workaround to the fact that you cannot use Json.reads[Properties] on an case class with no properties
  implicit val reads: Reads[Properties] = new Reads[Properties] {
    override def reads(json: JsValue) = json match {
      case JsObject(_) => JsSuccess(Properties())
      case _ => JsError()
    }
  }

  implicit val writes: Writes[Properties] = new Writes[Properties] {
    override def writes(o: Properties) = Json.obj()
  }

  def from(desProperties: properties.Properties): Properties = Properties()

}
