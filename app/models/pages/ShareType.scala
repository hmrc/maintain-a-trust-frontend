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

sealed trait ShareType

object ShareType extends Enumerable.Implicits {

  case object Quoted extends WithName("Quoted") with ShareType
  case object Unquoted extends WithName("Unquoted") with ShareType

  val values: List[ShareType] = List(
    Quoted, Unquoted
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("shareType", value.toString)
  }

  implicit val enumerable: Enumerable[ShareType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}