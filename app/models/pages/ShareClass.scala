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

sealed trait ShareClass

object ShareClass extends Enumerable.Implicits {

  case object Ordinary extends WithName("Ordinary shares") with ShareClass
  case object NonVoting extends WithName("Non-voting shares") with ShareClass
  case object Redeemable extends WithName("Redeemable shares") with ShareClass
  case object Preference extends WithName("Preference shares") with ShareClass
  case object Deferred extends WithName("Deferred ordinary shares") with ShareClass
  case object Management extends WithName("Management shares") with ShareClass
  case object OtherClasses extends WithName("Other classes of shares") with ShareClass
  case object Voting extends WithName("Voting shares") with ShareClass
  case object Dividend extends WithName("Dividend shares") with ShareClass
  case object Capital extends WithName("Capital share") with ShareClass
  case object Other extends WithName("Other") with ShareClass

  val values: List[ShareClass] = List(
    Ordinary, NonVoting, Redeemable, Preference, Deferred, Management, OtherClasses, Voting, Dividend, Capital, Other
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("shareClass", value.toString)
  }

  implicit val enumerable: Enumerable[ShareClass] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def fromDES(value: String): ShareClass = value match {
    case "Ordinary shares" => Ordinary
    case "Non-voting shares" => NonVoting
    case "Redeemable shares" => Redeemable
    case "Preference shares" => Preference
    case "Deferred ordinary shares" => Deferred
    case "Management shares" => Management
    case "Other classes of shares" => OtherClasses
    case "Voting shares" => Voting
    case "Dividend shares" => Dividend
    case "Capital share" => Capital
    case _ => Other
  }

}