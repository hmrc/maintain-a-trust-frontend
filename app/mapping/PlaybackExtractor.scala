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

package mapping

import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackImplicits._
import models.http.{AddressType, EntityType}
import models.{Address, InternationalAddress, UKAddress, UserAnswers}
import pages.{EmptyPage, QuestionPage}
import play.api.Logging
import utils.Constants.GB

import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

abstract class PlaybackExtractor[T <: EntityType : ClassTag] extends Logging {

  val optionalEntity: Boolean

  def extract(answers: UserAnswers, data: Option[List[T]]): Either[PlaybackExtractionError, UserAnswers] = {
    data match {
      case None if optionalEntity => Right(answers)
      case None => Left(FailedToExtractData(s"No entities of type ${classTag[T].runtimeClass.getSimpleName}"))
      case Some(entities) =>

        val updated = entities.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
          case (answers, (entity, index)) =>

            updateUserAnswers(answers, entity, index)
        }

        updated match {
          case Success(a) =>
            Right(a)
          case Failure(exception) =>
            logger.warn(s"[UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
            Left(FailedToExtractData(classTag[T].runtimeClass.getSimpleName))
        }
    }
  }

  def updateUserAnswers(answers: Try[UserAnswers], entity: T, index: Int): Try[UserAnswers]

  def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfResidencePage(index: Int): QuestionPage[String] = new EmptyPage[String]

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

  def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfNationalityPage(index: Int): QuestionPage[String] = new EmptyPage[String]

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

  def addressYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukAddressPage(index: Int): QuestionPage[Address] = new EmptyPage[Address]
  def nonUkAddressPage(index: Int): QuestionPage[Address] = new EmptyPage[Address]

  def extractAddress(address: AddressType,
                     index: Int,
                     answers: UserAnswers): Try[UserAnswers] = {

    extractIfTaxable(answers) {
      address.convert match {
        case uk: UKAddress =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), true))
            .flatMap(_.set(ukAddressPage(index), uk))
        case nonUk: InternationalAddress =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), false))
            .flatMap(_.set(nonUkAddressPage(index), nonUk))
      }
    }
  }

  def extractOptionalAddress(address: Option[AddressType],
                             index: Int,
                             answers: UserAnswers): Try[UserAnswers] = {

    extractIfTaxable(answers) {
      address.convert match {
        case Some(uk: UKAddress) =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), true))
            .flatMap(_.set(ukAddressPage(index), uk))
        case Some(nonUk: InternationalAddress) =>
          answers.set(addressYesNoPage(index), true)
            .flatMap(_.set(ukAddressYesNoPage(index), false))
            .flatMap(_.set(nonUkAddressPage(index), nonUk))
        case None =>
          answers.set(addressYesNoPage(index), false)
      }
    }
  }

  def extractIfTaxable(answers: UserAnswers)(block: Try[UserAnswers]): Try[UserAnswers] = {
    if (answers.isTrustTaxable) {
      block
    } else {
      Success(answers)
    }
  }
}
