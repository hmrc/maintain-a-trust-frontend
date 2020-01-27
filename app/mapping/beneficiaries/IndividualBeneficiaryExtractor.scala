/*
 * Copyright 2020 HM Revenue & Customs
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
import mapping.{PassportType, PlaybackExtractor}
import models.{Address, InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.{DisplayTrustIdentificationType, DisplayTrustIndividualDetailsType}
import pages.beneficiaries.individual._
import play.api.Logger

import scala.util.{Failure, Success, Try}
import mapping.PlaybackImplicits._

class IndividualBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustIndividualDetailsType]]] {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustIndividualDetailsType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Individual Beneficiary"))
        case Some(individual) =>

          val updated = individual.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (individualBeneficiary, index)) =>

            answers
              .flatMap(_.set(IndividualBeneficiaryNamePage(index), individualBeneficiary.name.convert))
              .flatMap(_.set(IndividualBeneficiaryRoleInCompanyPage(index), individualBeneficiary.beneficiaryType))
              .flatMap(answers => extractDateOfBirth(individualBeneficiary, index, answers))
              .flatMap(answers => extractShareOfIncome(individualBeneficiary, index, answers))
              .flatMap(answers => extractIdentification(individualBeneficiary, index, answers))
              .flatMap(_.set(IndividualBeneficiaryVulnerableYesNoPage(index), individualBeneficiary.vulnerableBeneficiary))
              .flatMap {
                _.set(
                  IndividualBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = individualBeneficiary.lineNo,
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
              Logger.warn(s"[IndividualBeneficiaryExtractor] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustIndividualDetailsType.toString))
          }
      }
    }

  private def extractIdentification(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers) = {
    individualBeneficiary.identification map {

      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryNationalInsuranceNumberPage(index), nino))

      case DisplayTrustIdentificationType(_, None, None, Some(address)) =>
        answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
          .flatMap(_.set(IndividualBeneficiaryPassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), None) =>
        Logger.error(s"[IndividualBeneficiaryExtractor] only passport identification returned in DisplayTrustOrEstate api")
        case object InvalidExtractorState extends RuntimeException
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(IndividualBeneficiaryNationalInsuranceYesNoPage(index), false)
        .flatMap(_.set(IndividualBeneficiaryAddressYesNoPage(index), false))
    }
  }


  private def extractDateOfBirth(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers) = {
    individualBeneficiary.dateOfBirth match {
      case Some(dob) =>
        answers.set(IndividualBeneficiaryDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(IndividualBeneficiaryDateOfBirthPage(index), dob.convert))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(IndividualBeneficiaryDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractShareOfIncome(individualBeneficiary: DisplayTrustIndividualDetailsType, index: Int, answers: UserAnswers) = {
    individualBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(IndividualBeneficiaryIncomeYesNoPage(index), false)
          .flatMap(_.set(IndividualBeneficiaryIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(IndividualBeneficiaryIncomeYesNoPage(index), true)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
      answers.set(IndividualBeneficiaryPassportIDCardYesNoPage(index), true)
        .flatMap(_.set(IndividualBeneficiaryPassportIDCardPage(index), passport.convert))
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
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

