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

package mapping

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import models.UserAnswers
import models.pages.NonResidentType
import pages.trustdetails._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class TrustDetailsExtractor @Inject() extends PlaybackExtractor[TrustDetailsType] {

  override def extract(answers: UserAnswers, data: TrustDetailsType): Either[PlaybackExtractionError, UserAnswers] =
    {
      val updated = answers
        .set(WhenTrustSetupPage, data.startDate)
        .flatMap(answers => extractGovernedBy(data, answers))
        .flatMap(answers => extractAdminBy(data, answers))
        .flatMap(answers => extractResidentialType(data, answers))

      updated match {
        case Success(a) =>
          Right(a)
        case Failure(exception) =>
          Logger.warn(s"[TrustDetailsExtractor] failed to extract data due to ${exception.getMessage}")
          Left(FailedToExtractData(TrustDetailsType.toString))
      }
    }

  private def extractGovernedBy(details: TrustDetailsType, answers: UserAnswers): Try[UserAnswers] =
    details.lawCountry match {
      case Some(country) => answers.set(GovernedInsideTheUKPage, false)
        .flatMap(_.set(CountryGoverningTrustPage, country))
      case _ => answers.set(GovernedInsideTheUKPage, true)
    }

  private def extractAdminBy(details: TrustDetailsType, answers: UserAnswers): Try[UserAnswers] =
    details.administrationCountry match {
      case Some(country) => answers.set(AdministrationInsideUKPage, false)
        .flatMap(_.set(CountryAdministeringTrustPage, country))
      case _ => answers.set(AdministrationInsideUKPage, true)
    }

  private def extractResidentialType(details: TrustDetailsType, answers: UserAnswers): Try[UserAnswers] =
    details.residentialStatus map {
      case ResidentialStatusType(Some(uk), None) => ukTrust(uk, answers)
      case ResidentialStatusType(None, Some(nonUK)) => nonUKTrust(nonUK, answers)
    } getOrElse Success(answers)

  private def ukTrust(uk: UkType, answers: UserAnswers): Try[UserAnswers] = {
    val extractOffShore = uk.preOffShore match {
      case Some(country) => answers.set(TrustPreviouslyResidentPage, country)
        .flatMap(_.set(TrustResidentOffshorePage, true))
      case _ => answers.set(TrustResidentOffshorePage, false)
    }
    extractOffShore.flatMap(_.set(EstablishedUnderScotsLawPage, uk.scottishLaw))
  }

  private def nonUKTrust(nonUK: NonUKType, answers: UserAnswers): Try[UserAnswers] = {
    val registeringTrustFor5A = answers.set(RegisteringTrustFor5APage, nonUK.sch5atcgga92)

    val inheritanceTax = nonUK.s218ihta84 match {
      case Some(iht) => registeringTrustFor5A.flatMap(_.set(InheritanceTaxActPage, iht))
      case _ => registeringTrustFor5A
    }

    val agentInheritance = nonUK.agentS218IHTA84 match {
      case Some(iht) => inheritanceTax.flatMap(_.set(AgentOtherThanBarristerPage, iht))
      case _ => inheritanceTax
    }

    nonUK.trusteeStatus.map(NonResidentType.fromDES) match {
      case Some(status) => agentInheritance.flatMap(_.set(NonResidentTypePage, status))
      case _ => agentInheritance
    }
  }

}