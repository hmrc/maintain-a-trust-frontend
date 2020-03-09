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

package mapping.trustees

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, InvalidExtractorState, PlaybackExtractionError}
import mapping.{PassportType, PlaybackExtractor, PlaybackImplicits}
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.Status.Completed
import models.{Address, InternationalAddress, MetaData, UKAddress, UserAnswers}
import pages.entitystatus.TrusteeStatus
import pages.trustees._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class TrusteesExtractor @Inject() extends PlaybackExtractor[Option[List[Trustees]]] {

  import PlaybackImplicits._

  override def extract(answers: UserAnswers, data: Option[List[Trustees]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None =>
          Left(FailedToExtractData("No Trustees"))
        case Some(trustees) =>
          val updated = trustees.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (trustee, index)) =>
              trustee match {
                case x : DisplayTrustLeadTrusteeIndType => extractLeadTrusteeIndividual(answers, index, x)
                case x : DisplayTrustLeadTrusteeOrgType => extractLeadTrusteeCompany(answers, index, x)
                case x : DisplayTrustTrusteeOrgType => extractTrusteeCompany(answers, index, x)
                case x : DisplayTrustTrusteeIndividualType => extractTrusteeIndividual(answers, index, x)
                case _ => Failure(new RuntimeException("Unexpected trustee type"))
              }
          }
          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              Logger.warn(s"[TrusteesExtractor] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustTrusteeType.toString))
          }
      }
    }

  def extractLeadTrusteeIndividual(answers: Try[UserAnswers], index: Int, leadIndividual : DisplayTrustLeadTrusteeIndType) = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), leadIndividual.name.convert))
      .flatMap(_.set(TrusteeDateOfBirthPage(index), leadIndividual.dateOfBirth.convert))
      .flatMap(answers => extractLeadIndividualIdentification(leadIndividual, index, answers))
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

  def extractLeadTrusteeCompany(answers: Try[UserAnswers], index: Int, leadCompany : DisplayTrustLeadTrusteeOrgType) = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), leadCompany.name))
      .flatMap(answers => extractLeadOrgIdentification(leadCompany, index, answers))
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

  def extractTrusteeIndividual(answers: Try[UserAnswers], index: Int, individual : DisplayTrustTrusteeIndividualType) = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), individual.name.convert))
      .flatMap(answers => extractDateOfBirth(individual, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), individual.phoneNumber))
      .flatMap(answers => extractIndividualIdentification(individual, index, answers))
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
      .flatMap(_.set(TrusteeStatus(index), Completed))
  }

  def extractTrusteeCompany(answers: Try[UserAnswers], index: Int, company: DisplayTrustTrusteeOrgType) = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), company.name))
      .flatMap(answers => extractCompanyIdentification(company, index, answers))
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
      .flatMap(_.set(TrusteeStatus(index), Completed))
  }

  private def extractLeadIndividualIdentification(leadIndividual: DisplayTrustLeadTrusteeIndType, index: Int, answers: UserAnswers) = {
    leadIndividual.identification match {

      case DisplayTrustIdentificationType(_, Some(nino), None, Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), false)
          .flatMap(answers => extractPassportIdCard(passport, index, answers))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), None) =>
        Logger.error(s"[TrusteesExtractor] only passport identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)

      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))

      case DisplayTrustIdentificationType(_, None, None, Some(address)) =>
        Logger.error(s"[TrusteesExtractor] only address identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)

      case DisplayTrustIdentificationType(_, _, _, _) =>
        Logger.error(s"[TrusteesExtractor] no identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
    }
  }


  private def extractIndividualIdentification(individual: DisplayTrustTrusteeIndividualType, index: Int, answers: UserAnswers) = {
    individual.identification map {

      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(TrusteeNinoYesNoPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))

      case DisplayTrustIdentificationType(_, None, Some(passport), None) =>
        Logger.error(s"[TrusteesExtractor] only passport identification returned for trustee individual in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)

      case DisplayTrustIdentificationType(_, None, None, Some(address)) =>
        answers.set(TrusteeNinoYesNoPage(index), false)
          .flatMap(_.set(TrusteePassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(TrusteeNinoYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

    } getOrElse {
      answers.set(TrusteeNinoYesNoPage(index), false)
        .flatMap(_.set(TrusteeAddressYesNoPage(index), false))
    }
  }

  private def extractLeadOrgIdentification(leadIndividual: DisplayTrustLeadTrusteeOrgType, index: Int, answers: UserAnswers) = {
    leadIndividual.identification match {

      case DisplayTrustIdentificationOrgType(_, Some(utr), Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))

      case DisplayTrustIdentificationOrgType(_, _, _) =>
        Logger.error(s"[TrusteesExtractor] no identification for lead trustee company returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
    }
  }

  private def extractCompanyIdentification(company: DisplayTrustTrusteeOrgType, index: Int, answers: UserAnswers) = {
    company.identification map {

      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))

    } getOrElse {
      answers.set(TrusteeUtrYesNoPage(index), false)
        .flatMap(_.set(TrusteeAddressYesNoPage(index), false))
    }
  }

  private def extractDateOfBirth(trusteeIndividual: DisplayTrustTrusteeIndividualType, index: Int, answers: UserAnswers) = {
    trusteeIndividual.dateOfBirth match {
      case Some(dob) =>
        answers.set(TrusteeDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(TrusteeDateOfBirthPage(index), dob.convert))
      case None =>
        // Assumption that user answered no as utr is not provided
        answers.set(TrusteeDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
    answers.set(TrusteePassportIDCardYesNoPage(index), true)
      .flatMap(_.set(TrusteePassportIDCardPage(index), passport.convert))
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(TrusteeAddressPage(index), uk)
          .flatMap(_.set(TrusteeAddressYesNoPage(index), true))
          .flatMap(_.set(TrusteeAddressInTheUKPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(TrusteeAddressPage(index), nonUk)
          .flatMap(_.set(TrusteeAddressYesNoPage(index), true))
          .flatMap(_.set(TrusteeAddressInTheUKPage(index), false))
    }
  }

  private def extractEmail(email: Option[String], index: Int, answers: UserAnswers) = {
    email match {
      case Some(x) => {
        answers.set(TrusteeEmailYesNoPage(index), true)
          .flatMap(_.set(TrusteeEmailPage(index), x))
      }
      case _ =>  answers.set(TrusteeEmailYesNoPage(index), false)
    }
  }

}