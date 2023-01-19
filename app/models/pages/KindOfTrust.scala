/*
 * Copyright 2023 HM Revenue & Customs
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

sealed trait KindOfTrust

object KindOfTrust extends Enumerable.Implicits {

  case object Intervivos extends WithName("Lifetime") with KindOfTrust
  case object Deed extends WithName("Deed") with KindOfTrust
  case object Employees extends WithName("Employees") with KindOfTrust
  case object FlatManagement extends WithName("Building") with KindOfTrust
  case object HeritageMaintenanceFund extends WithName("Repair") with KindOfTrust

  val values: List[KindOfTrust] = List(
    Intervivos, Deed, Employees, FlatManagement, HeritageMaintenanceFund
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("kindOfTrust", value.toString)
  }

  implicit val enumerable: Enumerable[KindOfTrust] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
