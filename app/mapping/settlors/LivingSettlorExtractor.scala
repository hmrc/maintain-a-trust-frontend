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

package mapping.settlors

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackImplicits._
import mapping.{PassportType, PlaybackExtractor}
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.Status.Completed
import models.{Address, InternationalAddress, MetaData, UKAddress, UserAnswers}
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class LivingSettlorExtractor @Inject() extends PlaybackExtractor[Option[List[LivingSettlor]]] {

  override def extract(answers: UserAnswers, data: Option[List[LivingSettlor]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Living Settlors"))
        case Some(settlors) =>

          val updated = settlors.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (settlor, index)) =>

              settlor match {
                case x : DisplayTrustSettlorCompany => extractSettlorCompany(answers, index, x)
                case x : DisplayTrustSettlor => extractSettlorIndividual(answers, index, x)
                case _ => Failure(new RuntimeException("Unexpected settlor type"))
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              Logger.warn(s"[SettlorCompanyExtractor] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustSettlors.toString))
          }
      }
    }

  def extractSettlorIndividual(answers: Try[UserAnswers], index: Int, individual : DisplayTrustSettlor) = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(SettlorIndividualNamePage(index), individual.name.convert))
      .flatMap(answers => extractDateOfBirth(individual, index, answers))
      .flatMap(answers => extractIndividualIdentification(individual, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = individual.lineNo,
            bpMatchStatus = individual.bpMatchStatus,
            entityStart = individual.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorSafeIdPage(index), individual.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), Completed))
  }

  def extractSettlorCompany(answers: Try[UserAnswers], index: Int, company : DisplayTrustSettlorCompany) = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(SettlorBusinessNamePage(index), company.name))
      .flatMap(answers => extractCompanyIdentification(company, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = company.lineNo,
            bpMatchStatus = company.bpMatchStatus,
            entityStart = company.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorCompanyTypePage(index), company.companyType))
      .flatMap(_.set(SettlorCompanyTimePage(index), company.companyTime))
      .flatMap(_.set(SettlorSafeIdPage(index), company.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), Completed))
  }

  private def extractIndividualIdentification(individual: DisplayTrustSettlor, index: Int, answers: UserAnswers) = {
    individual.identification map {

      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), true)
          .flatMap(_.set(SettlorIndividualNINOPage(index), nino))

      case DisplayTrustIdentificationType(_, None, Some(passport), None) =>
        Logger.error(s"[LivingSettlorExtractor] only passport identification returned in DisplayTrustOrEstate api")
        case object InvalidExtractorState extends RuntimeException
        Failure(InvalidExtractorState)

      case DisplayTrustIdentificationType(_, None, None, Some(address)) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), false)
          .flatMap(_.set(SettlorIndividualPassportIDCardYesNoPage(index), false))
          .flatMap(answers => extractAddress(address.convert, index, answers))

      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(SettlorIndividualNINOYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))
          .flatMap(answers => extractPassportIdCard(passport, index, answers))

    } getOrElse {
      answers.set(SettlorIndividualNINOYesNoPage(index), false)
        .flatMap(_.set(SettlorAddressYesNoPage(index), false))
    }
  }

  private def extractCompanyIdentification(company: DisplayTrustSettlorCompany, index: Int, answers: UserAnswers) = {
    company.identification map {

      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(SettlorUtrYesNoPage(index), true)
          .flatMap(_.set(SettlorUtrPage(index), utr))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(SettlorUtrYesNoPage(index), false)
          .flatMap(answers => extractAddress(address.convert, index, answers))

    } getOrElse {
      answers.set(SettlorUtrYesNoPage(index), false)
        .flatMap(_.set(SettlorAddressYesNoPage(index), false))
    }
  }

  private def extractDateOfBirth(settlorIndividual: DisplayTrustSettlor, index: Int, answers: UserAnswers) = {
    settlorIndividual.dateOfBirth match {
      case Some(dob) =>
        answers.set(SettlorIndividualDateOfBirthYesNoPage(index), true)
          .flatMap(_.set(SettlorIndividualDateOfBirthPage(index), dob.convert))
      case None =>
        // Assumption that user answered no as utr is not provided
        answers.set(SettlorIndividualDateOfBirthYesNoPage(index), false)
    }
  }

  private def extractPassportIdCard(passport: PassportType, index: Int, answers: UserAnswers) = {
    answers.set(SettlorIndividualPassportIDCardYesNoPage(index), true)
      .flatMap(_.set(SettlorIndividualPassportIDCardPage(index), passport.convert))
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(SettlorAddressUKPage(index), uk)
          .flatMap(_.set(SettlorAddressYesNoPage(index), true))
          .flatMap(_.set(SettlorAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(SettlorAddressInternationalPage(index), nonUk)
          .flatMap(_.set(SettlorAddressYesNoPage(index), true))
          .flatMap(_.set(SettlorAddressUKYesNoPage(index), false))
    }
  }
}