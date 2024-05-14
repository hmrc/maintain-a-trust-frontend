/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{Underlying4mldTrustIn5mldMode, Underlying5mldNonTaxableTrustIn5mldMode, Underlying5mldTaxableTrustIn5mldMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class WhatIsNextSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "WhatIsNext" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(WhatIsNext.values)

      forAll(gen) {
        whatIsNext =>

          JsString(whatIsNext.toString).validate[WhatIsNext].asOpt.value mustEqual whatIsNext
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhatIsNext.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[WhatIsNext] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(WhatIsNext.values)

      forAll(gen) {
        whatIsNext =>

          Json.toJson(whatIsNext) mustEqual JsString(whatIsNext.toString)
      }
    }

    "determine options correctly" when {

        "underlying trust is 4mld" in {
          WhatIsNext.options(Underlying4mldTrustIn5mldMode).map(_._1.value) mustBe
            List("declare", "make-changes", "close-trust", "generate-pdf")
        }

        "underlying trust is 5mld" when {
          "taxable" in {
            WhatIsNext.options(Underlying5mldTaxableTrustIn5mldMode).map(_._1.value) mustBe
              List("declare", "make-changes", "close-trust", "no-longer-taxable", "generate-pdf")
          }

          "non-taxable" in {
            WhatIsNext.options(Underlying5mldNonTaxableTrustIn5mldMode).map(_._1.value) mustBe
              List("make-changes", "close-trust", "needs-to-pay-tax", "generate-pdf")
          }
        }
    }

  }
}
