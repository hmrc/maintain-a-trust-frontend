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

import models.UserAnswers
import models.errors.TrustErrors
import pages.QuestionPage
import pages.close.nontaxable.DateClosedPage
import pages.close.taxable.DateLastAssetSharedOutPage

import java.time.LocalDate

object TrustClosureDate {

  def getClosureDate(userAnswers: UserAnswers): Option[LocalDate] = {
    if (userAnswers.isTrustTaxable) {
      userAnswers.get(DateLastAssetSharedOutPage)
    } else {
      userAnswers.get(DateClosedPage)
    }
  }

  def setClosureDate(answers: UserAnswers, date: Option[LocalDate]): Either[TrustErrors, UserAnswers] = {
    date match {
      case Some(value) =>
        val page: QuestionPage[LocalDate] = if (answers.isTrustTaxable) DateLastAssetSharedOutPage else DateClosedPage
        answers.set(page, value)
      case None =>
        Right(answers)
    }
  }
}
