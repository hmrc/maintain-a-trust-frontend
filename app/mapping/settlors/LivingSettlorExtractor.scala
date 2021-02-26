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

package mapping.settlors

import mapping.PlaybackExtractor
import mapping.PlaybackImplicits._
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.Tag.UpToDate
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._

import scala.util.{Failure, Try}

class LivingSettlorExtractor extends PlaybackExtractor[LivingSettlor] {

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = SettlorUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = SettlorUtrPage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressUKYesNoPage(index)
  override def ukAddressPage(index: Int): QuestionPage[Address] = SettlorAddressPage(index)
  override def nonUkAddressPage(index: Int): QuestionPage[Address] = SettlorAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: LivingSettlor, index: Int): Try[UserAnswers] = {
    entity match {
      case x: DisplayTrustSettlorCompany => extractSettlorCompany(answers, index, x)
      case x: DisplayTrustSettlor => extractSettlorIndividual(answers, index, x)
      case _ => Failure(new RuntimeException("Unexpected settlor type"))
    }
  }

  private def extractSettlorIndividual(answers: Try[UserAnswers],
                                       index: Int,
                                       individual: DisplayTrustSettlor): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(SettlorIndividualNamePage(index), individual.name))
      .flatMap(answers => extractDateOfBirth(individual, index, answers))
      .flatMap(answers => extractIndividualIdentification(individual, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = individual.lineNo.getOrElse(""),
            bpMatchStatus = individual.bpMatchStatus,
            entityStart = individual.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorSafeIdPage(index), individual.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }

  private def extractSettlorCompany(answers: Try[UserAnswers],
                                    index: Int,
                                    company: DisplayTrustSettlorCompany): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(SettlorBusinessNamePage(index), company.name))
      .flatMap(answers => extractOrgIdentification(company.identification, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = company.lineNo.getOrElse(""),
            bpMatchStatus = company.bpMatchStatus,
            entityStart = company.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorCompanyTypePage(index), company.companyType))
      .flatMap(_.set(SettlorCompanyTimePage(index), company.companyTime))
      .flatMap(_.set(SettlorSafeIdPage(index), company.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }

  private def extractIndividualIdentification(individual: DisplayTrustSettlor, index: Int, answers: UserAnswers) = {
    individual.identification match {

      case Some(DisplayTrustIdentificationType(_, Some(nino), None, None)) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), true)
          .flatMap(_.set(SettlorIndividualNINOPage(index), nino))

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), None)) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only passport identification returned in DisplayTrustOrEstate api")
        case object InvalidExtractorState extends RuntimeException
        Failure(InvalidExtractorState)

      case Some(DisplayTrustIdentificationType(_, None, None, Some(address))) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), false)
          .flatMap(_.set(SettlorIndividualPassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address, index, answers))

      case Some(DisplayTrustIdentificationType(_, None, Some(passport), Some(address))) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), false)
          .flatMap(answers => extractAddress(address, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

      case _ =>
        answers.set(SettlorIndividualNINOYesNoPage(index), false)
          .flatMap(_.set(SettlorAddressYesNoPage(index), false))

    }
  }

  private def extractDateOfBirth(settlorIndividual: DisplayTrustSettlor, index: Int, answers: UserAnswers) = {
    settlorIndividual.dateOfBirth match {
      case Some(dob) =>
        answers.set(SettlorIndividualDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(SettlorIndividualDateOfBirthPage(index), dob))
      case None =>
        // Assumption that user answered no as utr is not provided
        answers.set(SettlorIndividualDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
    answers.set(SettlorIndividualPassportIDCardYesNoPage(index), true)
      .flatMap(_.set(SettlorIndividualPassportIDCardPage(index), passport.convert))
  }
}
