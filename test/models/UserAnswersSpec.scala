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

package models

import base.SpecBase
import _root_.pages.trustdetails.ExpressTrustYesNoPage

class UserAnswersSpec extends SpecBase {

  "UserAnswers" when {

    ".trustMldStatus" must {

      "return correct MLD status of the trust" when {

        "4mld" in {
          val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)
          userAnswers.trustMldStatus mustBe Underlying4mldTrustIn4mldMode
        }

        "5mld" when {

          "underlying data is 4mld" in {
            val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
            userAnswers.trustMldStatus mustBe Underlying4mldTrustIn5mldMode
          }

          "underlying data is 5mld" when {

            "taxable" in {
              val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
                .set(ExpressTrustYesNoPage, false).success.value
              userAnswers.trustMldStatus mustBe Underlying5mldTaxableTrustIn5mldMode
            }

            "non-taxable" in {
              val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = false)
                .set(ExpressTrustYesNoPage, false).success.value
              userAnswers.trustMldStatus mustBe Underlying5mldNonTaxableTrustIn5mldMode
            }
          }
        }
      }
    }
  }
}
