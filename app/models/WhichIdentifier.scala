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

package models

import viewmodels.RadioOption

sealed trait WhichIdentifier

object WhichIdentifier extends Enumerable.Implicits {

  case object UTRIdentifier extends WithName("UTR") with WhichIdentifier
  case object URNIdentifier extends WithName("URN") with WhichIdentifier
  case object NoIdentifier extends WithName("none") with WhichIdentifier

  val values: List[WhichIdentifier] = List(
    UTRIdentifier, URNIdentifier, NoIdentifier
  )

  val options: List[(RadioOption, String)] = values.map {
    value =>
      (
        RadioOption("whichIdentifier", value.toString),
        if (value != NoIdentifier) s"whichIdentifier.${value.toString}.hint" else ""
      )
  }

  implicit val enumerable: Enumerable[WhichIdentifier] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
