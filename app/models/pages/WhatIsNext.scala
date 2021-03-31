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

import models._
import viewmodels.RadioOption

sealed trait WhatIsNext

object WhatIsNext extends Enumerable.Implicits {

  case object DeclareTheTrustIsUpToDate extends WithName("declare") with WhatIsNext
  case object MakeChanges extends WithName("make-changes") with WhatIsNext
  case object CloseTrust extends WithName("close-trust") with WhatIsNext
  case object NoLongerTaxable extends WithName("no-longer-taxable") with WhatIsNext
  case object NeedsToPayTax extends WithName("needs-to-pay-tax") with WhatIsNext
  case object GeneratePdf extends WithName("generate-pdf") with WhatIsNext

  val values: List[WhatIsNext] = List(
    DeclareTheTrustIsUpToDate, MakeChanges, CloseTrust, NoLongerTaxable, NeedsToPayTax, GeneratePdf
  )

  def options(trustMldStatus: TrustMldStatus): List[(RadioOption, String)] = {
    val suffix: String = if (trustMldStatus != Underlying4mldTrustIn4mldMode) "5mld" else ""
    values
      .filterNot(_ == DeclareTheTrustIsUpToDate && trustMldStatus == Underlying5mldNonTaxableTrustIn5mldMode)
      .filterNot(_ == NoLongerTaxable && trustMldStatus != Underlying5mldTaxableTrustIn5mldMode)
      .filterNot(_ == NeedsToPayTax && trustMldStatus != Underlying5mldNonTaxableTrustIn5mldMode)
      .filterNot(_ == GeneratePdf && trustMldStatus == Underlying4mldTrustIn4mldMode)
      .map(value => (RadioOption(s"declarationWhatNext$suffix", value.toString), s"declarationWhatNext$suffix.$value.hint"))
  }

  implicit val enumerable: Enumerable[WhatIsNext] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
