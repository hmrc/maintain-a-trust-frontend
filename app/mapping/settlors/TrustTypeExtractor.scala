/*
 * Copyright 2024 HM Revenue & Customs
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

import mapping.ConditionalExtractor
import models.UserAnswers
import models.errors.{FailedToExtractData, TrustErrors}
import models.http.{DisplayTrust, DisplayTrustWillType, TrustDetailsType}
import models.pages.DeedOfVariation.AdditionToWill
import models.pages.{DeedOfVariation, KindOfTrust, TypeOfTrust}
import pages.settlors.living_settlor.trust_type._
import pages.trustdetails.SetUpAfterSettlorDiedYesNoPage
import play.api.Logging

class TrustTypeExtractor extends ConditionalExtractor with Logging {

  private val className = getClass.getSimpleName

  def extract(answers: UserAnswers, data: DisplayTrust): Either[TrustErrors, UserAnswers] = {
    extractTrustType(data.details, answers) match {
      case Right(a) =>
        Right(a)
      case Left(_) =>
        Left(FailedToExtractData(DisplayTrustWillType.toString))
    }
  }

  private def extractTrustType(details: TrustDetailsType, answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      (details.typeOfTrust, details.deedOfVariation) match {
        case (Some(TypeOfTrust.DeedOfVariation), _) | (_, Some(AdditionToWill)) => answers
          .set(KindOfTrustPage, KindOfTrust.Deed)
          .flatMap(answers => extractDeedOfVariation(details, answers))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

        case (Some(TypeOfTrust.IntervivosSettlementTrust), _) => answers
          .set(KindOfTrustPage, KindOfTrust.Intervivos)
          .flatMap(_.set(HoldoverReliefYesNoPage, details.interVivos))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

        case (Some(TypeOfTrust.EmployeeRelated), _) => answers
          .set(KindOfTrustPage, KindOfTrust.Employees)
          .flatMap(answers => extractEfrbs(details, answers))
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

        case (Some(TypeOfTrust.FlatManagementTrust), _) => answers
          .set(KindOfTrustPage, KindOfTrust.FlatManagement)
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

        case (Some(TypeOfTrust.HeritageTrust), _) => answers
          .set(KindOfTrustPage, KindOfTrust.HeritageMaintenanceFund)
          .flatMap(_.set(SetUpAfterSettlorDiedYesNoPage, false))

        case (Some(TypeOfTrust.WillTrustOrIntestacyTrust), _) => answers
          .set(SetUpAfterSettlorDiedYesNoPage, true)

        case (None, _) if answers.isTrustMigratingFromNonTaxableToTaxable =>
          Right(answers)

        case (None, _) =>
          logger.warn(s"[$className][extractTrustType][UTR/URN: ${answers.identifier}] failed to extract data due to No trust type for taxable trust")
          Left(FailedToExtractData("No trust type for taxable trust."))
      }
    }
  }

  private def extractDeedOfVariation(details: TrustDetailsType, answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    details.deedOfVariation match {
      case Some(DeedOfVariation.AdditionToWill) =>
        answers.set(SetUpInAdditionToWillTrustYesNoPage, true)
      case Some(_) =>
        answers.set(SetUpInAdditionToWillTrustYesNoPage, false)
          .flatMap(_.set(HowDeedOfVariationCreatedPage, details.deedOfVariation))
      case _ =>
        Right(answers)
    }
  }

  private def extractEfrbs(details: TrustDetailsType, answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    details.efrbsStartDate match {
      case Some(date) =>
        answers.set(EfrbsYesNoPage, true)
          .flatMap(_.set(EfrbsStartDatePage, date))
      case _ =>
        answers.set(EfrbsYesNoPage, false)
    }
  }

}
