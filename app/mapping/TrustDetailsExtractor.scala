/*
 * Copyright 2022 HM Revenue & Customs
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

import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import models.UserAnswers
import models.http.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType}
import models.pages.NonResidentType
import models.pages.TrusteesBased._
import pages.trustdetails._
import play.api.Logging
import utils.Constants.GB

import scala.util.{Failure, Success, Try}

class TrustDetailsExtractor extends ConditionalExtractor with Logging {

  def extract(answers: UserAnswers, data: TrustDetailsType): Either[PlaybackExtractionError, UserAnswers] = {
    val updated = answers
      .set(WhenTrustSetupPage, data.startDate)
      .flatMap(_.set(TrustTaxableYesNoPage, data.isTaxable))
      .flatMap(_.set(ExpressTrustYesNoPage, data.expressTrust))
      .flatMap(_.set(TrustUkPropertyYesNoPage, data.trustUKProperty))
      .flatMap(_.set(TrustRecordedOnAnotherRegisterYesNoPage, data.trustRecorded))
      .flatMap(extractGovernedBy(data.lawCountry, _))
      .flatMap(extractAdminBy(data.administrationCountry, _))
      .flatMap(extractResidentialType(data, _))
      .flatMap(_.set(TrustHasBusinessRelationshipInUkYesNoPage, data.trustUKRelation))

    updated match {
      case Success(a) =>
        Right(a)
      case Failure(exception) =>
        logger.warn(s"[TrustDetailsExtractor][extract][UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
        Left(FailedToExtractData(TrustDetailsType.toString))
    }
  }

  private def extractGovernedBy(lawCountry: Option[String],
                                answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      lawCountry match {
        case Some(country) => answers
          .set(GovernedInsideTheUKPage, false)
          .flatMap(_.set(CountryGoverningTrustPage, country))
        case _ => answers
          .set(GovernedInsideTheUKPage, true)
      }
    }
  }

  private def extractAdminBy(administrationCountry: Option[String],
                             answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      administrationCountry match {
        case Some(country) if country != GB => answers
          .set(AdministrationInsideUKPage, false)
          .flatMap(_.set(CountryAdministeringTrustPage, country))
        case _ => answers
          .set(AdministrationInsideUKPage, true)
      }
    }
  }

  private def extractResidentialType(details: TrustDetailsType,
                                     answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      details.residentialStatus match {
        case Some(ResidentialStatusType(Some(uk), None)) => ukTrust(uk, details.settlorsUkBased, answers)
        case Some(ResidentialStatusType(None, Some(nonUK))) => nonUKTrust(nonUK, details.settlorsUkBased, answers)
        case _ => Success(answers)
      }
    }
  }

  private def ukTrust(uk: UkType, settlorsUkBased: Option[Boolean], answers: UserAnswers): Try[UserAnswers] = {

    def extractOffShore(answers: UserAnswers): Try[UserAnswers] = uk.preOffShore match {
      case Some(country) => answers
        .set(TrustResidentOffshorePage, true)
        .flatMap(_.set(TrustPreviouslyResidentPage, country))
      case _ => answers
        .set(TrustResidentOffshorePage, false)
    }

    answers
      .set(EstablishedUnderScotsLawPage, uk.scottishLaw)
      .flatMap(extractOffShore)
      .flatMap(extractWhereTrusteesAndSettlorsBased(true, settlorsUkBased, _))
  }

  private def nonUKTrust(nonUK: NonUKType, settlorsUkBased: Option[Boolean], answers: UserAnswers): Try[UserAnswers] = {

    def nonResidentType(answers: UserAnswers): Try[UserAnswers] = nonUK.trusteeStatus.map(NonResidentType.fromDES) match {
      case Some(value) => answers.set(NonResidentTypePage, value)
      case _ => Success(answers)
    }

    answers
      .set(RegisteringTrustFor5APage, nonUK.sch5atcgga92)
      .flatMap(_.set(InheritanceTaxActPage, nonUK.s218ihta84))
      .flatMap(_.set(AgentOtherThanBarristerPage, nonUK.agentS218IHTA84))
      .flatMap(nonResidentType)
      .flatMap(extractWhereTrusteesAndSettlorsBased(false, settlorsUkBased, _))
  }

  private def extractWhereTrusteesAndSettlorsBased(isUk: Boolean, settlorsUkBased: Option[Boolean], answers: UserAnswers): Try[UserAnswers] = {
    (isUk, settlorsUkBased) match {
      case (true, None) => answers
        .set(WhereTrusteesBasedPage, AllTrusteesUkBased)
      case (true, _) => answers
        .set(WhereTrusteesBasedPage, InternationalAndUkBasedTrustees)
        .flatMap(_.set(SettlorsUkBasedPage, true))
      case (false, None) => answers
        .set(WhereTrusteesBasedPage, NoTrusteesUkBased)
      case (false, _) => answers
        .set(WhereTrusteesBasedPage, InternationalAndUkBasedTrustees)
        .flatMap(_.set(SettlorsUkBasedPage, false))
    }
  }

}
