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

package models

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class TrustDetailsSpec extends SpecBase with ScalaCheckPropertyChecks {

  "TrustDetails" when {

    ".is5mld" must {

      "return true when expressTrust is defined" in {
        forAll(arbitrary[Option[Boolean]], arbitrary[Boolean]) {
          (trustTaxable, expressTrust) =>

            val trustDetails = TrustDetails(LocalDate.now(), trustTaxable, Some(expressTrust), None)
            trustDetails.is5mld mustBe true
        }
      }

      "return false when expressTrust is undefined" in {
        forAll(arbitrary[Option[Boolean]]) {
          trustTaxable =>

            val trustDetails = TrustDetails(LocalDate.now(), trustTaxable, None, None)
            trustDetails.is5mld mustBe false
        }
      }
    }

    ".isTaxable" must {

      "return true" when {

        "trustTaxable undefined" in {
          forAll(arbitrary[Option[Boolean]]) {
            expressTrust =>

              val trustDetails = TrustDetails(LocalDate.now(), None, expressTrust, None)
              trustDetails.isTaxable mustBe true
          }
        }

        "trustTaxable contains true" in {
          forAll(arbitrary[Option[Boolean]]) {
            expressTrust =>

              val trustDetails = TrustDetails(LocalDate.now(), Some(true), expressTrust, None)
              trustDetails.isTaxable mustBe true
          }
        }
      }

      "return false" when {
        "trustTaxable contains false" in {
          forAll(arbitrary[Option[Boolean]]) {
            expressTrust =>

              val trustDetails = TrustDetails(LocalDate.now(), Some(false), expressTrust, None)
              trustDetails.isTaxable mustBe false
          }
        }
      }

    }

    ".hasSchedule3aExemptAnswer" must {

      "return true when schedule3aExempt is defined" in {
        forAll(arbitrary[Option[Boolean]], arbitrary[Boolean], arbitrary[Boolean]) {
          (trustTaxable, expressTrust, schedule3aExempt) =>

            val trustDetails = TrustDetails(LocalDate.now(), trustTaxable, Some(expressTrust), Some(schedule3aExempt))
            trustDetails.hasSchedule3aExemptAnswer mustBe true
        }
      }

      "return false when schedule3aExempt is undefined" in {
        forAll(arbitrary[Option[Boolean]], arbitrary[Boolean]) {
          (trustTaxable, expressTrust) =>

            val trustDetails = TrustDetails(LocalDate.now(), trustTaxable, Some(expressTrust), None)
            trustDetails.hasSchedule3aExemptAnswer mustBe false
        }
      }
    }


  }
}
