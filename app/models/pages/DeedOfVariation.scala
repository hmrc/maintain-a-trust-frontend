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
import play.api.libs.json._

sealed trait DeedOfVariation {
  val asString: String
}

object DeedOfVariation extends Enumerable.Implicits {

  case object PreviouslyAbsoluteInterestUnderWill extends WithName("replaceAbsoluteInterestOverWill") with DeedOfVariation {
    override val asString: String = "Previously there was only an absolute interest under the will"
  }

  case object ReplacedWill extends WithName("replaceWillTrust") with DeedOfVariation {
    override val asString: String = "Replaced the will trust"
  }

  case object AdditionToWill extends DeedOfVariation {
    override val asString: String = "Addition to the will trust"
  }

  implicit val reads: Reads[DeedOfVariation] = Reads {
    case JsString(PreviouslyAbsoluteInterestUnderWill.asString) => JsSuccess(PreviouslyAbsoluteInterestUnderWill)
    case JsString(ReplacedWill.asString) => JsSuccess(ReplacedWill)
    case JsString(AdditionToWill.asString) => JsSuccess(AdditionToWill)
    case _ => JsError("Invalid DeedOfVariation")
  }

  implicit val writes: Writes[DeedOfVariation] = Writes(x => JsString(x.asString))

  val values: Set[DeedOfVariation] = Set(
    PreviouslyAbsoluteInterestUnderWill,
    ReplacedWill,
    AdditionToWill
  )

  implicit val enumerable: Enumerable[DeedOfVariation] =
    Enumerable(values.toSeq.map(v => v.toString -> v): _*)
}
