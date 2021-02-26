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

package mapping.beneficiaries

import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.BeneficiaryType
import pages.{EmptyPage, QuestionPage}

import scala.util.{Success, Try}

trait BeneficiaryPlaybackExtractor[T <: BeneficiaryType] extends PlaybackExtractor[T] {

  override val optionalEntity: Boolean = false

  def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def shareOfIncomePage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def extractShareOfIncome(shareOfIncome: Option[String],
                           index: Int,
                           answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTrustTaxable) {
      shareOfIncome match {
        case Some(income) =>
          answers.set(shareOfIncomeYesNoPage(index), false)
            .flatMap(_.set(shareOfIncomePage(index), income))
        case None =>
          answers.set(shareOfIncomeYesNoPage(index), true)
      }
    } else {
      Success(answers)
    }
  }

}
