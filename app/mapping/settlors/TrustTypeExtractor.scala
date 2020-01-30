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

import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.{DisplayTrust, DisplayTrustWillType}
import models.pages.{DeedOfVariation, KindOfTrust, TypeOfTrust}
import pages.settlors.SetUpAfterSettlorDiedYesNoPage
import pages.settlors.living_settlor.trust_type._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class TrustTypeExtractor extends PlaybackExtractor[Option[DisplayTrust]] {

  override def extract(answers: UserAnswers, data: Option[DisplayTrust]): Either[PlaybackExtractionError, UserAnswers] =
  {
    data match {
      case None => Left(FailedToExtractData("No Trust Type"))
      case displayTrust =>

        val updated = displayTrust.foldLeft[Try[UserAnswers]](Success(answers)){
          case (answers, trust) =>
            answers
              .flatMap(answers => extractTrustType(trust, answers))
        }

        updated match {
          case Success(a) =>
            Right(a)
          case Failure(exception) =>
            Logger.warn(s"[TrustTypeExtractor] failed to extract data due to ${exception.getMessage}")
            Left(FailedToExtractData(DisplayTrustWillType.toString))
        }
    }
  }

  private def extractTrustType(trust: DisplayTrust, answers: UserAnswers) = {
    trust.details.typeOfTrust match {
      case TypeOfTrust.DeedOfVariation =>
        answers.set(KindOfTrustPage, KindOfTrust.Deed)
          .flatMap(answers => extractDeedOfVariation(trust, answers))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

      case TypeOfTrust.IntervivosSettlementTrust =>
        answers.set(KindOfTrustPage, KindOfTrust.Intervivos)
          .flatMap(_.set(HoldoverReliefYesNoPage, trust.details.interVivos))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

      case TypeOfTrust.EmployeeRelated =>
        answers.set(KindOfTrustPage, KindOfTrust.Employees)
          .flatMap(answers => extractEfrbs(trust, answers))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

      case TypeOfTrust.FlatManagementTrust =>
        answers.set(KindOfTrustPage, KindOfTrust.FlatManagement)
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

      case TypeOfTrust.HeritageTrust =>
        answers.set(KindOfTrustPage, KindOfTrust.HeritageMaintenanceFund)
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

      case TypeOfTrust.WillTrustOrIntestacyTrust =>
        answers.set(SetUpAfterSettlorDiedYesNoPage, true)
    }
  }

  private def extractDeedOfVariation(trust: DisplayTrust, answers: UserAnswers) = {
    trust.details.deedOfVariation match {
      case Some(DeedOfVariation.AdditionToWill) =>
        answers.set(SetUpInAdditionToWillTrustYesNoPage, true)
      case Some(_) =>
        answers.set(SetUpInAdditionToWillTrustYesNoPage, false)
          .flatMap(_.set(HowDeedOfVariationCreatedPage, trust.details.deedOfVariation))
      case _ =>
        Success(answers)
    }
  }

  private def extractEfrbs(trust: DisplayTrust, answers: UserAnswers) = {
    trust.details.efrbsStartDate match {
      case Some(date) =>
        answers.set(EfrbsYesNoPage, true)
          .flatMap(_.set(EfrbsStartDatePage, date))
      case _ =>
        answers.set(EfrbsYesNoPage, false)
    }
  }

}