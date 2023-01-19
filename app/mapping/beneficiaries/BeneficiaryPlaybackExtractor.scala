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

package mapping.beneficiaries

import mapping.PlaybackExtractionErrors.InvalidExtractorState
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.{BeneficiaryType, DisplayTrustIdentificationOrgType, OrgBeneficiaryType}
import pages.{EmptyPage, QuestionPage}

import scala.util.{Failure, Try}

trait BeneficiaryPlaybackExtractor[T <: BeneficiaryType] extends PlaybackExtractor[T] {

  def namePage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def shareOfIncomePage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def extractShareOfIncome(shareOfIncome: Option[String],
                           index: Int,
                           answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      shareOfIncome match {
        case Some(income) =>
          answers.set(shareOfIncomeYesNoPage(index), false)
            .flatMap(_.set(shareOfIncomePage(index), income))
        case None =>
          answers.set(shareOfIncomeYesNoPage(index), true)
      }
    }
  }

  override def extractOrgIdentification(identification: Option[DisplayTrustIdentificationOrgType],
                                        index: Int,
                                        answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      identification map {
        case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
          answers.set(utrPage(index), utr)
            .flatMap(_.set(addressYesNoPage(index), false))
        case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
          extractAddress(address, index, answers)
        case _ =>
          logger.error(s"[BeneficiaryPlaybackExtractor][extractOrgIdentification][UTR/URN: ${answers.identifier}] both utr/urn and address parsed")
          Failure(InvalidExtractorState)
      } getOrElse {
        answers.set(addressYesNoPage(index), false)
      }
    }
  }

  def updateUserAnswersForOrgBeneficiary(answers: Try[UserAnswers],
                                         entity: OrgBeneficiaryType,
                                         index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity.asInstanceOf[T], index)
      .flatMap(_.set(namePage(index), entity.organisationName))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(_.set(safeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
  }

}
