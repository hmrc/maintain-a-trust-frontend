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

import mapping.PlaybackImplicits._
import models.http.{DisplayTrustIdentificationType, DisplayTrustIndividualDetailsType, PassportType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.individual._

import scala.util.{Failure, Try}

class IndividualBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustIndividualDetailsType] {

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryIncomeYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = IndividualBeneficiaryIncomePage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = IndividualBeneficiaryCountryOfResidencePage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = IndividualBeneficiaryCountryOfNationalityPage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualBeneficiaryAddressUKYesNoPage(index)
  override def ukAddressPage(index: Int): QuestionPage[Address] = IndividualBeneficiaryAddressPage(index)
  override def nonUkAddressPage(index: Int): QuestionPage[Address] = IndividualBeneficiaryAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustIndividualDetailsType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IndividualBeneficiaryNamePage(index), entity.name))
      .flatMap(answers => extractRoleInCompany(entity, index, answers))
      .flatMap(answers => extractDateOfBirth(entity, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.countryOfNationality, index, answers))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(answers => extractIdentification(entity, index, answers))
      .flatMap(answers => extractVulnerability(entity.vulnerableBeneficiary, index, answers))
      .flatMap {
        _.set(
          IndividualBeneficiaryMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
      .flatMap(_.set(IndividualBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
  }

  private def extractRoleInCompany(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      answers.set(IndividualBeneficiaryRoleInCompanyPage(index), individualBeneficiary.beneficiaryType)
    }
  }

  private def extractIdentification(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      individualBeneficiary.identification match {

        case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
          answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), true)
            .flatMap(_.set(IndividualBeneficiaryNationalInsuranceNumberPage(index), nino))

        case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
          answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
            .flatMap(_.set(IndividualBeneficiaryPassportIDCardYesNoPage(index), false))
            .flatMap(answers => extractAddress(address, index, answers))

        case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
          answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
            .flatMap(answers => extractPassportIdCard(passport, index, answers))

        case Some(DisplayTrustIdentificationType(_, None, Some(_), None)) =>
          logger.error(s"[UTR/URN: ${answers.identifier}] only passport identification returned in DisplayTrustOrEstate api")
          case object InvalidExtractorState extends RuntimeException
          Failure(InvalidExtractorState)

        case _ =>
          answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
            .flatMap(_.set(IndividualBeneficiaryAddressYesNoPage(index), false))

      }
    }
  }

  private def extractDateOfBirth(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    individualBeneficiary.dateOfBirth match {
      case Some(dob) =>
        answers.set(IndividualBeneficiaryDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryDateOfBirthPage(index), dob.convert))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(IndividualBeneficiaryDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      answers.set(IndividualBeneficiaryPassportIDCardYesNoPage(index), true)
        .flatMap(_.set(IndividualBeneficiaryPassportIDCardPage(index), passport.convert))
    }
  }

  private def extractVulnerability(vulnerable: Option[Boolean], index: Int, answers: UserAnswers): Try[UserAnswers] = {
    // TODO - will this statement hold if migrating from non-taxable to taxable?
    extractIfTaxable(answers) {
      vulnerable match {
        case Some(value) => answers.set(IndividualBeneficiaryVulnerableYesNoPage(index), value)
        case None => Failure(new Throwable("Vulnerability must be answered for taxable trust."))
      }
    }
  }

}
