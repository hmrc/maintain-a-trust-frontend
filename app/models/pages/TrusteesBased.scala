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

sealed trait TrusteesBased

object TrusteesBased extends Enumerable.Implicits {

  case object AllTrusteesUkBased extends WithName("all-uk-based") with TrusteesBased
  case object NoTrusteesUkBased extends WithName("none-uk-based") with TrusteesBased
  case object InternationalAndUkBasedTrustees extends WithName("some-uk-based") with TrusteesBased

  val values: List[TrusteesBased] = List(
    AllTrusteesUkBased, NoTrusteesUkBased, InternationalAndUkBasedTrustees
  )

  implicit val enumerable: Enumerable[TrusteesBased] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
