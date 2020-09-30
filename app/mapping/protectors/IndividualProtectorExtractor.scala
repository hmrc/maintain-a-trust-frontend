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

package mapping.protectors

import mapping.PassportType
import mapping.PlaybackExtractionErrors.InvalidExtractorState
import models.{Address, InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.{DisplayTrustIdentificationType, DisplayTrustProtector}
import pages.protectors.ProtectorIndividualOrBusinessPage
import pages.protectors.individual._
import play.api.Logger

import scala.util.{Failure, Try}
import mapping.PlaybackImplicits._
import models.pages.IndividualOrBusiness

class IndividualProtectorExtractor {

  def extract(answers: Try[UserAnswers], index: Int, individualProtector : DisplayTrustProtector): Try[UserAnswers] =
    {
      answers
        .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
        .flatMap(_.set(IndividualProtectorNamePage(index), individualProtector.name.convert))
        .flatMap(answers => extractDateOfBirth(individualProtector, index, answers))
        .flatMap(answers => extractIdentification(individualProtector, index, answers))
        .flatMap(_.set(IndividualProtectorSafeIdPage(index), individualProtector.identification.flatMap(_.safeId)))
        .flatMap {
          _.set(
            IndividualProtectorMetaData(index),
            MetaData(
              lineNo = individualProtector.lineNo.getOrElse(""),
              bpMatchStatus = individualProtector.bpMatchStatus,
              entityStart = individualProtector.entityStart
            )
          )
        }
    }

  private def extractDateOfBirth(individualProtector: DisplayTrustProtector, index: Int, answers: UserAnswers) = {
    individualProtector.dateOfBirth match {
      case Some(dateOfBirth) =>
        answers.set(IndividualProtectorDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(IndividualProtectorDateOfBirthPage(index), dateOfBirth.convert))
      case None =>
        // Assumption that user answered no as the date of birth is not provided
        answers.set(IndividualProtectorDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractIdentification(individualProtector: DisplayTrustProtector, index: Int, answers: UserAnswers) = {
    individualProtector.identification match {

      case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
        answers.set(IndividualProtectorNINOYesNoPage(index), true)
          .flatMap(_.set(IndividualProtectorNINOPage(index), nino))

      case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
        answers.set(IndividualProtectorNINOYesNoPage(index), false)
          .flatMap(_.set(IndividualProtectorPassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
        answers.set(IndividualProtectorNINOYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(_), None)) =>
        Logger.error(s"[IndividualProtectorExtractor] only passport identification returned in DisplayTrustIdentificationType")
        Failure(InvalidExtractorState)

      case _ =>
        answers.set(IndividualProtectorNINOYesNoPage(index), false)
          .flatMap(_.set(IndividualProtectorAddressYesNoPage(index), false))

    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
    answers.set(IndividualProtectorPassportIDCardYesNoPage(index), true)
      .flatMap(_.set(IndividualProtectorPassportIDCardPage(index), passport.convert))
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(IndividualProtectorAddressPage(index), uk)
          .flatMap(_.set(IndividualProtectorAddressYesNoPage(index), true))
          .flatMap(_.set(IndividualProtectorAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(IndividualProtectorAddressPage(index), nonUk)
          .flatMap(_.set(IndividualProtectorAddressYesNoPage(index), true))
          .flatMap(_.set(IndividualProtectorAddressUKYesNoPage(index), false))
    }
  }
}