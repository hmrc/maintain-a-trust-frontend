/*
 * Copyright 2022 HM Revenue & Customs
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

package mapping

import mapping.PlaybackExtractionErrors._
import mapping.PlaybackImplicits._
import models.http._
import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import pages.QuestionPage
import play.api.Logging
import utils.Constants.GB

import java.time.LocalDate
import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

abstract class PlaybackExtractor[T <: EntityType : ClassTag] extends Pages with ConditionalExtractor with Logging {

  val optionalEntity: Boolean = false

  def extract(answers: UserAnswers, data: List[T]): Either[PlaybackExtractionError, UserAnswers] = {
    data match {
      case Nil if optionalEntity => Right(answers)
      case Nil => Left(FailedToExtractData(s"No entities of type ${classTag[T].runtimeClass.getSimpleName}"))
      case entities =>

        val updated = entities.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
          case (answers, (entity, index)) =>
            updateUserAnswers(answers, entity, index)
        }

        updated match {
          case Success(a) =>
            Right(a)
          case Failure(exception) =>
            logger.warn(s"[PlaybackExtractor][extract][UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
            Left(FailedToExtractData(classTag[T].runtimeClass.getSimpleName))
        }
    }
  }

  def updateUserAnswers(answers: Try[UserAnswers], entity: T, index: Int): Try[UserAnswers] = {
    answers.flatMap(answers => extractMetaData(entity, index, answers))
  }

  def extractMentalCapacity(legallyIncapable: Option[Boolean], index: Int, answers: UserAnswers): Try[UserAnswers] = {
    legallyIncapable match {
      case Some(value) => answers.set(mentalCapacityYesNoPage(index), !value)
      case None => Success(answers)
    }
  }

  def extractCountryOfResidence(countryOfResidence: Option[String],
                                index: Int,
                                answers: UserAnswers): Try[UserAnswers] = {
    extractCountryOfResidenceOrNationality(
      country = countryOfResidence,
      answers = answers,
      yesNoPage = countryOfResidenceYesNoPage(index),
      ukYesNoPage = ukCountryOfResidenceYesNoPage(index),
      page = countryOfResidencePage(index)
    )
  }

  def extractCountryOfNationality(countryOfNationality: Option[String],
                                  index: Int,
                                  answers: UserAnswers): Try[UserAnswers] = {
    extractCountryOfResidenceOrNationality(
      country = countryOfNationality,
      answers = answers,
      yesNoPage = countryOfNationalityYesNoPage(index),
      ukYesNoPage = ukCountryOfNationalityYesNoPage(index),
      page = countryOfNationalityPage(index)
    )
  }

  private def extractCountryOfResidenceOrNationality(country: Option[String],
                                                     answers: UserAnswers,
                                                     yesNoPage: QuestionPage[Boolean],
                                                     ukYesNoPage: QuestionPage[Boolean],
                                                     page: QuestionPage[String]): Try[UserAnswers] = {
    extractIf5mldTrustIn5mldMode(answers) {
      country match {
        case Some(GB) =>
          answers.set(yesNoPage, true)
            .flatMap(_.set(ukYesNoPage, true))
            .flatMap(_.set(page, GB))
        case Some(country) =>
          answers.set(yesNoPage, true)
            .flatMap(_.set(ukYesNoPage, false))
            .flatMap(_.set(page, country))
        case None =>
          answers.set(yesNoPage, false)
      }
    }
  }

  def extractAddress(address: AddressType,
                     index: Int,
                     answers: UserAnswers): Try[UserAnswers] = {
      address.convert match {
        case uk: UKAddress =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), true))
            .flatMap(_.set(addressPage(index), uk))
        case nonUk: InternationalAddress =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), false))
            .flatMap(_.set(addressPage(index), nonUk))
      }
  }

  def extractOptionalAddress(optionalAddress: Option[AddressType],
                             index: Int,
                             answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      optionalAddress match {
        case Some(address) =>
          extractAddress(address, index, answers)
        case None =>
          answers.set(addressYesNoPage(index), false)
      }
    }
  }

  def extractIndIdentification(identification: Option[DisplayTrustIdentificationType],
                               index: Int,
                               answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      identification match {
        case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
          answers.set(ninoYesNoPage(index), true)
            .flatMap(_.set(ninoPage(index), nino))
        case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
            .flatMap(_.set(passportOrIdCardYesNoPage(index), false))
        case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
            .flatMap(answers => extractPassportIdCard(passport, index, answers))
        case Some(DisplayTrustIdentificationType(_, None, Some(_), None)) =>
          logger.error(s"[PlaybackExtractor][extractIndIdentification][UTR/URN: ${answers.identifier}] only passport identification returned in DisplayTrustIdentificationType")
          Failure(InvalidExtractorState)
        case _ =>
          answers.set(ninoYesNoPage(index), false)
            .flatMap(_.set(addressYesNoPage(index), false))
      }
    }
  }

  def extractPassportIdCard(passport: PassportType,
                            index: Int,
                            answers: UserAnswers): Try[UserAnswers] = {
    answers.set(passportOrIdCardYesNoPage(index), true)
      .flatMap(_.set(passportOrIdCardPage(index), passport))
  }

  def extractOrgIdentification(identification: Option[DisplayTrustIdentificationOrgType],
                               index: Int,
                               answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      identification match {
        case Some(DisplayTrustIdentificationOrgType(_, Some(utr), None)) =>
          answers.set(utrYesNoPage(index), true)
            .flatMap(_.set(utrPage(index), utr))
        case Some(DisplayTrustIdentificationOrgType(_, None, Some(address))) =>
          answers.set(utrYesNoPage(index), false)
            .flatMap(answers => extractAddress(address, index, answers))
        case _ =>
          answers.set(utrYesNoPage(index), false)
            .flatMap(_.set(addressYesNoPage(index), false))
      }
    }
  }

  def extractDateOfBirth(dateOfBirth: Option[LocalDate],
                         index: Int,
                         answers: UserAnswers): Try[UserAnswers] = {
    dateOfBirth match {
      case Some(dateOfBirth) =>
        answers.set(dateOfBirthYesNoPage(index), true)
          .flatMap(_.set(dateOfBirthPage(index), dateOfBirth))
      case None =>
        answers.set(dateOfBirthYesNoPage(index), false)
    }
  }

  def extractMetaData(entity: T,
                      index: Int,
                      answers: UserAnswers): Try[UserAnswers] = {
    val metaData = MetaData(
      lineNo = entity.lineNo.getOrElse(""),
      bpMatchStatus = bpMatchStatus(entity),
      entityStart = entity.entityStart
    )

    answers.set(metaDataPage(index), metaData)
  }

  def bpMatchStatus(entity: T): Option[String] = entity.bpMatchStatus

}
