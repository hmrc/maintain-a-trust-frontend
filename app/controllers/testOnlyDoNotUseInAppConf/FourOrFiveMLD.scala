/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.testOnlyDoNotUseInAppConf

import models.{Enumerable, WithName}
import viewmodels.RadioOption

sealed trait FourOrFiveMLD
object FourOrFiveMLD extends Enumerable.Implicits {

  case object FourMLD extends WithName("four") with FourOrFiveMLD
  case object FiveMLD extends WithName("five") with FourOrFiveMLD

  val values: Set[FourOrFiveMLD] = Set(
    FourMLD, FiveMLD
  )

  val options: Set[RadioOption] = values.map {
    value =>
      RadioOption("testOnly.fourOrFiveMLD", value.toString)
  }

  implicit val enumerable: Enumerable[FourOrFiveMLD] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
