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
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import mapping.PlaybackImplicits._
import models.http.{DisplayTrustIdentificationType, DisplayTrustIndividualDetailsType, PassportType}
import models.{Address, InternationalAddress, MetaData, UKAddress, UTR, UserAnswers}
import pages.beneficiaries.individual._
import play.api.Logging
import utils.Constants.GB

import scala.util.{Failure, Success, Try}

class IndividualBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustIndividualDetailsType]]] with Logging {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustIndividualDetailsType]]): Either[PlaybackExtractionError, UserAnswers] = {
    data match {
      case None => Left(FailedToExtractData("No Individual Beneficiary"))
      case Some(individual) =>

        val updated = individual.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)) {
          case (answers, (individualBeneficiary, index)) =>

            answers
              .flatMap(_.set(IndividualBeneficiaryNamePage(index), individualBeneficiary.name))
              .flatMap(answers => extractRoleInCompany(individualBeneficiary, index, answers))
              .flatMap(answers => extractDateOfBirth(individualBeneficiary, index, answers))
              .flatMap(answers => extractCountryOfResidence(individualBeneficiary, index, answers))
              .flatMap(answers => extractCountryOfNationality(individualBeneficiary, index, answers))
              .flatMap(answers => extractShareOfIncome(individualBeneficiary, index, answers))
              .flatMap(answers => extractIdentification(individualBeneficiary, index, answers))
              .flatMap(answers => extractVulnerability(individualBeneficiary.vulnerableBeneficiary, index, answers))
              .flatMap {
                _.set(
                  IndividualBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = individualBeneficiary.lineNo.getOrElse(""),
                    bpMatchStatus = individualBeneficiary.bpMatchStatus,
                    entityStart = individualBeneficiary.entityStart
                  )
                )
              }
              .flatMap(_.set(IndividualBeneficiarySafeIdPage(index), individualBeneficiary.identification.flatMap(_.safeId)))
        }

        updated match {
          case Success(a) =>
            Right(a)
          case Failure(exception) =>
            logger.warn(s"[UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
            Left(FailedToExtractData(DisplayTrustIndividualDetailsType.toString))
        }
    }
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
            .flatMap(answers => extractAddress(address.convert, index, answers))

        case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
          answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
            .flatMap(answers => extractAddress(address.convert, index, answers))
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

  private def extractCountryOfResidence(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    individualBeneficiary.countryOfResidence match {
      case Some(GB) =>
        answers.set(IndividualBeneficiaryCountryOfResidenceYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), true))
          .flatMap(_.set(IndividualBeneficiaryCountryOfResidencePage(index), GB))
      case Some(country) =>
        answers.set(IndividualBeneficiaryCountryOfResidenceYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(index), false))
          .flatMap(_.set(IndividualBeneficiaryCountryOfResidencePage(index), country))
      case None =>
        answers.set(IndividualBeneficiaryCountryOfResidenceYesNoPage(index), false)
    }
  }

  private def extractCountryOfNationality(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    individualBeneficiary.countryOfNationality match {
      case Some(GB) =>
        answers.set(IndividualBeneficiaryCountryOfNationalityYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index), true))
          .flatMap(_.set(IndividualBeneficiaryCountryOfNationalityPage(index), GB))
      case Some(country) =>
        answers.set(IndividualBeneficiaryCountryOfNationalityYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(index), false))
          .flatMap(_.set(IndividualBeneficiaryCountryOfNationalityPage(index), country))
      case None =>
        answers.set(IndividualBeneficiaryCountryOfNationalityYesNoPage(index), false)
    }
  }
  
  private def extractShareOfIncome(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      individualBeneficiary.beneficiaryShareOfIncome match {
        case Some(income) =>
          answers.set(IndividualBeneficiaryIncomeYesNoPage(index), false)
            .flatMap(_.set(IndividualBeneficiaryIncomePage(index), income))
        case None =>
          // Assumption that user answered yes as the share of income is not provided
          answers.set(IndividualBeneficiaryIncomeYesNoPage(index), true)
      }
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      answers.set(IndividualBeneficiaryPassportIDCardYesNoPage(index), true)
        .flatMap(_.set(IndividualBeneficiaryPassportIDCardPage(index), passport.convert))
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxable(answers) {
      address match {
        case uk: UKAddress =>
          answers.set(IndividualBeneficiaryAddressPage(index), uk)
            .flatMap(_.set(IndividualBeneficiaryAddressYesNoPage(index), true))
            .flatMap(_.set(IndividualBeneficiaryAddressUKYesNoPage(index), true))
        case nonUk: InternationalAddress =>
          answers.set(IndividualBeneficiaryAddressPage(index), nonUk)
            .flatMap(_.set(IndividualBeneficiaryAddressYesNoPage(index), true))
            .flatMap(_.set(IndividualBeneficiaryAddressUKYesNoPage(index), false))
      }
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
