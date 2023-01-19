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

import models.pages.DeedOfVariation._
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsNull, JsString, Json}

class DeedOfVariationSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "DeedOfVariation" must {

    "serialise and deserailise" when {

      "Previously there was only an absolute interest under the will" in {
        val json = JsString("Previously there was only an absolute interest under the will")
        val deedOfVariation = json.as[DeedOfVariation]
        deedOfVariation mustEqual PreviouslyAbsoluteInterestUnderWill
        Json.toJson(deedOfVariation) mustEqual json
      }

      "Replaced the will trust" in {
        val json = JsString("Replaced the will trust")
        val deedOfVariation = json.as[DeedOfVariation]
        deedOfVariation mustEqual ReplacedWill
        Json.toJson(deedOfVariation) mustEqual json
      }

      "Addition to the will trust" in {
        val json = JsString("Addition to the will trust")
        val deedOfVariation = json.as[DeedOfVariation]
        deedOfVariation mustEqual AdditionToWill
        Json.toJson(deedOfVariation) mustEqual json
      }
    }

    "return error when reading invalid deed of variation" in {
      val json = JsNull
      json.validate[DeedOfVariation].isError mustBe true
    }
  }
}
