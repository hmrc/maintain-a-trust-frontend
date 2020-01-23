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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class AddATrusteeSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "AddATrustee" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(AddATrustee.values.toSeq)

      forAll(gen) {
        addATrustee =>

          JsString(addATrustee.toString).validate[AddATrustee].asOpt.value mustEqual addATrustee
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AddATrustee.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AddATrustee] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(AddATrustee.values.toSeq)

      forAll(gen) {
        addATrustee =>

          Json.toJson(addATrustee) mustEqual JsString(addATrustee.toString)
      }
    }
  }
}
