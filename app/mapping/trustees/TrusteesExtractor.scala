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

package mapping.trustees

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.InvalidExtractorState
import mapping.PlaybackExtractor
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.Tag.UpToDate
import models.{Address, MetaData, PassportOrIdCardDetails, UserAnswers}
import pages.QuestionPage
import pages.entitystatus.TrusteeStatus
import pages.trustees._

import java.time.LocalDate
import scala.util.{Failure, Try}

class TrusteesExtractor @Inject() extends PlaybackExtractor[Trustees] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeAddressInTheUKPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = TrusteeAddressPage(index)

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = TrusteeUtrPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = TrusteeDateOfBirthPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = TrusteePassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportOrIdCardDetails] = TrusteePassportIDCardPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeNinoYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = TrusteeNinoPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: Trustees, index: Int): Try[UserAnswers] = {
    entity match {
      case x: DisplayTrustLeadTrusteeIndType => extractLeadTrusteeIndividual(answers, index, x)
      case x: DisplayTrustLeadTrusteeOrgType => extractLeadTrusteeCompany(answers, index, x)
      case x: DisplayTrustTrusteeOrgType => extractTrusteeCompany(answers, index, x)
      case x: DisplayTrustTrusteeIndividualType => extractTrusteeIndividual(answers, index, x)
      case _ => Failure(new RuntimeException("Unexpected trustee type"))
    }
  }

  private def extractLeadTrusteeIndividual(answers: Try[UserAnswers],
                                           index: Int,
                                           leadIndividual: DisplayTrustLeadTrusteeIndType): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), leadIndividual.name))
      .flatMap(_.set(TrusteeDateOfBirthPage(index), leadIndividual.dateOfBirth))
      .flatMap(answers => extractLeadIndividualIdentification(leadIndividual.identification, index, answers))
      .flatMap(answers => extractEmail(leadIndividual.email, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), leadIndividual.phoneNumber))
      .flatMap(_.set(TrusteeSafeIdPage(index), leadIndividual.identification.safeId))
      .flatMap {
        _.set(
          TrusteeMetaData(index),
          MetaData(
            lineNo = leadIndividual.lineNo.getOrElse(""),
            bpMatchStatus = leadIndividual.bpMatchStatus,
            entityStart = leadIndividual.entityStart
          )
        )
      }
  }

  private def extractLeadTrusteeCompany(answers: Try[UserAnswers],
                                        index: Int,
                                        leadCompany: DisplayTrustLeadTrusteeOrgType): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), leadCompany.name))
      .flatMap(answers => extractLeadOrgIdentification(leadCompany.identification, index, answers))
      .flatMap(answers => extractEmail(leadCompany.email, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), leadCompany.phoneNumber))
      .flatMap(_.set(TrusteeSafeIdPage(index), leadCompany.identification.safeId))
      .flatMap {
        _.set(
          TrusteeMetaData(index),
          MetaData(
            lineNo = leadCompany.lineNo.getOrElse(""),
            bpMatchStatus = leadCompany.bpMatchStatus,
            entityStart = leadCompany.entityStart
          )
        )
      }
  }

  private def extractTrusteeIndividual(answers: Try[UserAnswers],
                                       index: Int,
                                       individual: DisplayTrustTrusteeIndividualType): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), individual.name))
      .flatMap(answers => extractDateOfBirth(individual.dateOfBirth, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), individual.phoneNumber))
      .flatMap(answers => extractIndIdentification(individual.identification, index, answers))
      .flatMap {
        _.set(
          TrusteeMetaData(index),
          MetaData(
            lineNo = individual.lineNo.getOrElse(""),
            bpMatchStatus = individual.bpMatchStatus,
            entityStart = individual.entityStart
          )
        )
      }
      .flatMap(_.set(TrusteeSafeIdPage(index), individual.identification.flatMap(_.safeId)))
      .flatMap(_.set(TrusteeStatus(index), UpToDate))
  }

  private def extractTrusteeCompany(answers: Try[UserAnswers],
                                    index: Int,
                                    company: DisplayTrustTrusteeOrgType): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), company.name))
      .flatMap(answers => extractOrgIdentification(company.identification, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), company.phoneNumber))
      .flatMap(_.set(TrusteeEmailPage(index), company.email))
      .flatMap(_.set(TrusteeSafeIdPage(index), company.identification.flatMap(_.safeId)))
      .flatMap {
        _.set(
          TrusteeMetaData(index),
          MetaData(
            lineNo = company.lineNo.getOrElse(""),
            bpMatchStatus = company.bpMatchStatus,
            entityStart = company.entityStart
          )
        )
      }
      .flatMap(_.set(TrusteeStatus(index), UpToDate))
  }

  private def extractLeadIndividualIdentification(identification: DisplayTrustIdentificationType,
                                                  index: Int,
                                                  answers: UserAnswers): Try[UserAnswers] = {
    identification match {
      case DisplayTrustIdentificationType(_, Some(nino), None, Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))
          .flatMap(answers => extractAddress(address, index, answers))
      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), false)
          .flatMap(answers => extractPassportIdCard(passport, index, answers))
          .flatMap(answers => extractAddress(address, index, answers))
      case DisplayTrustIdentificationType(_, None, Some(_), None) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only passport identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))
      case DisplayTrustIdentificationType(_, None, None, Some(_)) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only address identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
      case DisplayTrustIdentificationType(_, _, _, _) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] no identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
    }
  }

  private def extractLeadOrgIdentification(identification: DisplayTrustIdentificationOrgType,
                                           index: Int,
                                           answers: UserAnswers): Try[UserAnswers] = {
    identification match {
      case DisplayTrustIdentificationOrgType(_, Some(utr), Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))
          .flatMap(answers => extractAddress(address, index, answers))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), false)
          .flatMap(answers => extractAddress(address, index, answers))

      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))

      case DisplayTrustIdentificationOrgType(_, _, _) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] no identification for lead trustee company returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
    }
  }

  private def extractEmail(email: Option[String],
                           index: Int,
                           answers: UserAnswers): Try[UserAnswers] = {
    email match {
      case Some(value) =>
        answers.set(TrusteeEmailYesNoPage(index), true)
          .flatMap(_.set(TrusteeEmailPage(index), value))
      case _ =>
        answers.set(TrusteeEmailYesNoPage(index), false)
    }
  }
  
}
