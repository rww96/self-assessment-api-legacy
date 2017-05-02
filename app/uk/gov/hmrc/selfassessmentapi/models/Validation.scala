/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.models

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsSuccess, Reads}

case class Validation[T](paths: Seq[String], validator: T => Boolean, validationError: ValidationError)

object Validation {
  implicit class CumulativeValidator[T](reads: Reads[T]) {
    def validate(validations: Seq[Validation[T]]): Reads[T] =
      reads.flatMap { t =>
        Reads[T] { _ =>
          val errors = validations.foldLeft(Seq.empty[(JsPath, Seq[ValidationError])]) {
            case (errs, Validation(paths, validator, err)) =>
              if (validator(t)) errs
              else errs ++ paths.map(path => (JsPath \ path) -> Seq(err))
          }
          if (errors.isEmpty) JsSuccess(t) else JsError(errors)
        }
      }
  }
}