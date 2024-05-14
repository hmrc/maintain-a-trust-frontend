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
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TrustMldStatusSpec extends SpecBase with ScalaCheckPropertyChecks {

  "TrustMldStatus" when {

    ".is5mldTrustIn5mldMode" must {

      "return false when Underlying4mldTrustIn5mldMode" in {

        val trustMldStatuses: Seq[TrustMldStatus] = Seq[TrustMldStatus](
          Underlying4mldTrustIn5mldMode
        )

        forAll(Gen.oneOf(trustMldStatuses)) {
          trustMldStatus =>
            trustMldStatus.is5mldTrustIn5mldMode mustEqual false
        }
      }

      "return true when Underlying5mldTaxableTrustIn5mldMode or Underlying5mldNonTaxableTrustIn5mldMode" in {

        val trustMldStatuses: Seq[TrustMldStatus] = Seq[TrustMldStatus](
          Underlying5mldTaxableTrustIn5mldMode,
          Underlying5mldNonTaxableTrustIn5mldMode
        )

        forAll(Gen.oneOf(trustMldStatuses)) {
          trustMldStatus =>
            trustMldStatus.is5mldTrustIn5mldMode mustEqual true
        }
      }
    }
  }
}
