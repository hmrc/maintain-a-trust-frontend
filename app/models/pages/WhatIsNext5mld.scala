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

package models.pages

import models.{Enumerable, WithName}
import viewmodels.RadioOption

sealed trait WhatIsNext5mld

object WhatIsNext5mld extends Enumerable.Implicits {

  case object DeclareTheTrustIsUpToDate extends WithName("declare") with WhatIsNext5mld

  case object MakeChanges extends WithName("make-changes") with WhatIsNext5mld

  case object CloseTrust extends WithName("close-trust") with WhatIsNext5mld

  case object NoLongerTaxable extends WithName("no-longer-taxable") with WhatIsNext5mld

  val values: List[WhatIsNext5mld] = List(
    DeclareTheTrustIsUpToDate, MakeChanges, CloseTrust, NoLongerTaxable
  )

  val options: List[(RadioOption, String)] = values.map {
    value =>
      (RadioOption("declarationWhatNext5mld", value.toString), s"declarationWhatNext5mld.${value.toString}.hint")
  }

  implicit val enumerable: Enumerable[WhatIsNext5mld] =
    Enumerable(values.map(v => v.toString -> v): _*)
}