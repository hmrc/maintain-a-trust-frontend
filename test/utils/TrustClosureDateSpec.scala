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

package utils

import base.SpecBase
import models.UserAnswers
import pages.close.nontaxable.DateClosedPage
import pages.close.taxable.DateLastAssetSharedOutPage
import utils.TrustClosureDate.{getClosureDate, setClosureDate}

import java.time.LocalDate

class TrustClosureDateSpec extends SpecBase {

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "TrustClosureDate" when {

    ".getClosureDate" must {

      "return None" when {
        "there is no closure date" when {

          "taxable" in {

            val result: Option[LocalDate] = getClosureDate(emptyUserAnswersForUtr)

            result mustBe None
          }

          "non-taxable" in {

            val result: Option[LocalDate] = getClosureDate(emptyUserAnswersForUrn)

            result mustBe None
          }
        }
      }

      "return Some" when {
        "there is a closure date" when {

          "taxable" in {

            val userAnswers = emptyUserAnswersForUtr
              .set(DateLastAssetSharedOutPage, date).value

            val result: Option[LocalDate] = getClosureDate(userAnswers)

            result mustBe Some(date)
          }

          "non-taxable" in {

            val userAnswers = emptyUserAnswersForUrn
              .set(DateClosedPage, date).value

            val result: Option[LocalDate] = getClosureDate(userAnswers)

            result mustBe Some(date)
          }
        }
      }
    }

    ".setClosureDate" must {

      "return unmodified user answers" when {
        "there is no closure date" in {

          val userAnswers = emptyUserAnswersForUtr

          val result: UserAnswers = setClosureDate(userAnswers, None).value

          result mustBe userAnswers
        }
      }

      "return modified user answers" when {
        "there is a closure date" when {

          val date = LocalDate.parse("1996-02-03")

          "taxable" in {

            val userAnswers = emptyUserAnswersForUtr

            val result: UserAnswers = setClosureDate(userAnswers, Some(date)).value

            result mustNot be(userAnswers)
            result.get(DateLastAssetSharedOutPage).get mustBe date
            result.get(DateClosedPage) mustBe None
          }

          "non-taxable" in {

            val userAnswers = emptyUserAnswersForUrn

            val result: UserAnswers = setClosureDate(userAnswers, Some(date)).value

            result mustNot be(userAnswers)
            result.get(DateLastAssetSharedOutPage) mustBe None
            result.get(DateClosedPage).get mustBe date
          }
        }
      }
    }
  }
}
