/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{Enumerable, WithName}
import viewmodels.RadioOption

sealed trait KindOfBusiness

object KindOfBusiness extends Enumerable.Implicits {

  case object Trading extends WithName("Trading") with KindOfBusiness
  case object Investment extends WithName("Investment") with KindOfBusiness

  val values: List[KindOfBusiness] = List(
    Trading, Investment
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("kindOfBusiness", value.toString)
  }

  implicit val enumerable: Enumerable[KindOfBusiness] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
