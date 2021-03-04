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

import mapping.PlaybackExtractionErrors.InvalidExtractorState
import models.http.DisplayTrustIndividualDetailsType
import models.{Address, MetaData, PassportOrIdCardDetails, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.individual._

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

class IndividualBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustIndividualDetailsType] {

  override def metaDataPage(index: Int): QuestionPage[MetaData] = IndividualBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryIncomeYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = IndividualBeneficiaryIncomePage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = IndividualBeneficiaryCountryOfResidencePage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = IndividualBeneficiaryCountryOfNationalityPage(index)

  override def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryMentalCapacityYesNoPage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = IndividualBeneficiaryAddressPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryNationalInsuranceYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = IndividualBeneficiaryNationalInsuranceNumberPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportOrIdCardDetails] = IndividualBeneficiaryPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = IndividualBeneficiaryDateOfBirthPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustIndividualDetailsType,
                                 index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(IndividualBeneficiaryNamePage(index), entity.name))
      .flatMap(answers => extractRoleInCompany(entity, index, answers))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.countryOfNationality, index, answers))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(answers => extractMentalCapacity(entity.legallyIncapable, index, answers))
      .flatMap(answers => extractVulnerability(entity.vulnerableBeneficiary, index, answers))
      .flatMap(_.set(IndividualBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
  }

  private def extractRoleInCompany(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      answers.set(IndividualBeneficiaryRoleInCompanyPage(index), individualBeneficiary.beneficiaryType)
    }
  }


  private def extractVulnerability(vulnerable: Option[Boolean], index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      vulnerable match {
        case Some(value) => answers.set(IndividualBeneficiaryVulnerableYesNoPage(index), value)
        case None =>
          logger.error(s"[UTR/URN: ${answers.identifier}] Vulnerability must be answered for taxable trust.")
          Failure(InvalidExtractorState)
      }
    }
  }

}
