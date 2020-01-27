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

import models.{Enumerable, WithName}

sealed trait DeedOfVariation

object DeedOfVariation extends Enumerable.Implicits {

  case object DeedOfVariation extends WithName("Previously there was only an absolute interest under the will") with DeedOfVariation
  case object ReplacedWill extends WithName("Replaced the will trust") with DeedOfVariation
  case object AdditionToWill extends WithName("Addition to the will trust") with DeedOfVariation

  val values: Set[DeedOfVariation] = Set(
    DeedOfVariation,
    ReplacedWill,
    AdditionToWill
  )

  implicit val enumerable: Enumerable[DeedOfVariation] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}