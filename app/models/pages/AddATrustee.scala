/*
 * Copyright 2020 HM Revenue & Customs
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

package models.pages

import models.WithName
import models.enums.Enumerable
import viewmodels.RadioOption

sealed trait AddATrustee

object AddATrustee extends Enumerable.Implicits {

  case object YesNow extends WithName("add-them-now") with AddATrustee
  case object YesLater extends WithName("add-them-later") with AddATrustee
  case object NoComplete extends WithName("no-complete") with AddATrustee

  val values: List[AddATrustee] = List(
    YesNow, YesLater, NoComplete
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("addATrustee", value.toString)
  }

  implicit val enumerable: Enumerable[AddATrustee] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
