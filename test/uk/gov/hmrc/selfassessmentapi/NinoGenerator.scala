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

package uk.gov.hmrc.selfassessmentapi

import uk.gov.hmrc.domain.Nino

import scala.util.Random

class NinoGenerator(random: Random) {
  def nextNino(): Nino = {
    val prefix = random.shuffle(Nino.validPrefixes).head
    val suffix = random.shuffle(Nino.validSuffixes).head
    val digits = (0 to 5).map(_ => random.nextInt(10)).foldLeft("")((acc, curr) => acc + curr.toString)

    Nino(s"$prefix$digits$suffix")
  }
}

object NinoGenerator {
  def apply(): NinoGenerator = new NinoGenerator(new Random)
  def apply(seed: Long): NinoGenerator = new NinoGenerator(new Random(seed))
}
