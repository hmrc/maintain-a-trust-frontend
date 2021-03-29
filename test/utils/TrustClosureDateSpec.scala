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

package utils

import base.SpecBase
import pages.close.nontaxable.DateClosedPage
import pages.close.taxable.DateLastAssetSharedOutPage
import utils.TrustClosureDate.getClosureDate

import java.time.LocalDate

class TrustClosureDateSpec extends SpecBase {

  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "TrustClosureDate" must {

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
            .set(DateLastAssetSharedOutPage, date).success.value

          val result: Option[LocalDate] = getClosureDate(userAnswers)

          result mustBe Some(date)
        }

        "non-taxable" in {

          val userAnswers = emptyUserAnswersForUrn
            .set(DateClosedPage, date).success.value

          val result: Option[LocalDate] = getClosureDate(userAnswers)

          result mustBe Some(date)
        }
      }
    }
  }
}
