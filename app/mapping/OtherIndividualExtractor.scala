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

import com.google.inject.Inject
import mapping.PlaybackImplicits._
import models.http.{DisplayTrustIdentificationType, NaturalPersonType, PassportType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.individual._

import scala.util.{Failure, Try}

class OtherIndividualExtractor @Inject() extends PlaybackExtractor[NaturalPersonType] {

  override val optionalEntity: Boolean = true

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualAddressUKYesNoPage(index)
  override def ukAddressPage(index: Int): QuestionPage[Address] = OtherIndividualAddressPage(index)
  override def nonUkAddressPage(index: Int): QuestionPage[Address] = OtherIndividualAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: NaturalPersonType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(OtherIndividualNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity, index, answers))
      .flatMap(answers => extractIdentification(entity, index, answers))
      .flatMap {
        _.set(
          OtherIndividualMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
      .flatMap(_.set(OtherIndividualSafeIdPage(index), entity.identification.flatMap(_.safeId)))
  }

  private def extractIdentification(individual: NaturalPersonType, index: Int, answers: UserAnswers) = {
    individual.identification match {

      case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
        answers.set(OtherIndividualNationalInsuranceYesNoPage(index), true)
          .flatMap(_.set(OtherIndividualNationalInsuranceNumberPage(index), nino))

      case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
        answers.set(OtherIndividualNationalInsuranceYesNoPage(index), false)
          .flatMap(_.set(OtherIndividualPassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
        answers.set(OtherIndividualNationalInsuranceYesNoPage(index), false)
          .flatMap(answers => extractAddress(address, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(_), None)) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only passport identification returned in DisplayTrustOrEstate api")
        case object InvalidExtractorState extends RuntimeException
        Failure(InvalidExtractorState)

      case _ =>
        answers.set(OtherIndividualNationalInsuranceYesNoPage(index), false)
          .flatMap(_.set(OtherIndividualAddressYesNoPage(index), false))

    }
  }

  private def extractDateOfBirth(individual: NaturalPersonType, index: Int, answers: UserAnswers) = {
    individual.dateOfBirth match {
      case Some(dob) =>
        answers.set(OtherIndividualDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(OtherIndividualDateOfBirthPage(index), dob.convert))
      case None =>
        // Assumption that user answered no as dob is not provided
        answers.set(OtherIndividualDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
      answers.set(OtherIndividualPassportIDCardYesNoPage(index), true)
        .flatMap(_.set(OtherIndividualPassportIDCardPage(index), passport.convert))
  }

}
