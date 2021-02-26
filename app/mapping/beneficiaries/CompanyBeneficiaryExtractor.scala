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

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.InvalidExtractorState
import models.http.{DisplayTrustCompanyType, DisplayTrustIdentificationOrgType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.company._

import scala.util.{Failure, Try}

class CompanyBeneficiaryExtractor @Inject() extends BeneficiaryPlaybackExtractor[DisplayTrustCompanyType] {

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = CompanyBeneficiaryCountryOfResidenceYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = CompanyBeneficiaryCountryOfResidencePage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = CompanyBeneficiaryCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = CompanyBeneficiaryCountryOfResidencePage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = CompanyBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = CompanyBeneficiaryAddressUKYesNoPage(index)
  override def ukAddressPage(index: Int): QuestionPage[Address] = CompanyBeneficiaryAddressPage(index)
  override def nonUkAddressPage(index: Int): QuestionPage[Address] = CompanyBeneficiaryAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustCompanyType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(CompanyBeneficiaryNamePage(index), entity.organisationName))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(_.set(CompanyBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractIdentification(entity.identification, index, answers))
      .flatMap {
        _.set(
          CompanyBeneficiaryMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
  }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTrustTaxable) {
      identification map {
        case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
          answers.set(CompanyBeneficiaryUtrPage(index), utr)
            .flatMap(_.set(CompanyBeneficiaryAddressYesNoPage(index), false))

        case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
          extractAddress(address, index, answers)

        case _ =>
          logger.error(s"[UTR/URN: ${answers.identifier}] only both utr and address parsed")
          Failure(InvalidExtractorState)

      } getOrElse {
        answers.set(CompanyBeneficiaryAddressYesNoPage(index), false)
      }
    } else {
      Try(answers)
    }
  }
}
