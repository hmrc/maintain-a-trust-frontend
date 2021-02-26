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

package mapping.protectors

import mapping.PlaybackExtractionErrors.InvalidExtractorState
import mapping.PlaybackImplicits._
import models.http.{DisplayTrustIdentificationType, DisplayTrustProtector, PassportType}
import models.pages.IndividualOrBusiness
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.protectors.ProtectorIndividualOrBusinessPage
import pages.protectors.individual._

import scala.util.{Failure, Try}

class IndividualProtectorExtractor extends ProtectorPlaybackExtractor[DisplayTrustProtector] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressUKYesNoPage(index)
  override def ukAddressPage(index: Int): QuestionPage[Address] = IndividualProtectorAddressPage(index)
  override def nonUkAddressPage(index: Int): QuestionPage[Address] = IndividualProtectorAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustProtector, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(IndividualProtectorNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity, index, answers))
      .flatMap(answers => extractIdentification(entity, index, answers))
      .flatMap(_.set(IndividualProtectorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap {
        _.set(
          IndividualProtectorMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
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
          .flatMap(answers => extractAddress(address, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
        answers.set(IndividualProtectorNINOYesNoPage(index), false)
          .flatMap(answers => extractAddress(address, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(_), None)) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only passport identification returned in DisplayTrustIdentificationType")
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

}
