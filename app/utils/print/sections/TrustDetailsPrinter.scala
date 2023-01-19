/*
 * Copyright 2023 HM Revenue & Customs
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

package utils.print.sections

import models.{URN, UTR, UserAnswers}
import pages.settlors.living_settlor.trust_type._
import pages.trustdetails._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class TrustDetailsPrinter @Inject()(converter: AnswerRowConverter) extends PrinterHelper {

  def print(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    val utrOrUrnRow = userAnswers.identifierType match {
      case UTR => converter.identifier(userAnswers, "uniqueTaxReference")
      case URN => converter.identifier(userAnswers, "uniqueReferenceNumber")
    }

    val rows: Seq[Option[AnswerRow]] = if (userAnswers.isTrustMigratingFromNonTaxableToTaxable) {
      Seq(
        converter.stringQuestion(TrustNamePage, userAnswers, "trustName"),
        converter.dateQuestion(WhenTrustSetupPage, userAnswers, "whenTrustSetup"),

        converter.whichIdentifier(userAnswers),
        utrOrUrnRow,

        converter.yesNoQuestion(GovernedInsideTheUKPage, userAnswers, "governedByUkLaw"),
        converter.countryQuestion(CountryGoverningTrustPage, userAnswers, "governingCountry"),
        converter.yesNoQuestion(AdministrationInsideUKPage, userAnswers, "administeredInUk"),
        converter.countryQuestion(CountryAdministeringTrustPage, userAnswers, "administrationCountry"),

        converter.yesNoQuestion(SetUpAfterSettlorDiedYesNoPage, userAnswers, "setUpAfterSettlorDied"),
        converter.enumQuestion(KindOfTrustPage, userAnswers, "typeOfTrust", "kindOfTrust"),
        converter.enumQuestion(HowDeedOfVariationCreatedPage, userAnswers, "whyDeedOfVariationCreated", "deedOfVariation"),
        converter.yesNoQuestion(HoldoverReliefYesNoPage, userAnswers, "holdoverReliefClaimed"),
        converter.yesNoQuestion(EfrbsYesNoPage, userAnswers, "efrbsYesNo"),
        converter.dateQuestion(EfrbsStartDatePage, userAnswers, "efrbsStartDate"),

        converter.yesNoQuestion(TrustUkPropertyYesNoPage, userAnswers, "trustUkPropertyYesNo"),
        converter.yesNoQuestion(TrustRecordedOnAnotherRegisterYesNoPage, userAnswers, "trustRecordedOnAnotherRegisterYesNo"),

        converter.enumQuestion(WhereTrusteesBasedPage, userAnswers, "whereTrusteesBased", "whereTrusteesBased"),
        converter.yesNoQuestion(SettlorsUkBasedPage, userAnswers, "settlorsUkBased"),

        converter.yesNoQuestion(EstablishedUnderScotsLawPage, userAnswers, "establishedUnderScotsLaw"),
        converter.yesNoQuestion(TrustResidentOffshorePage, userAnswers, "previouslyResidentOffshore"),
        converter.countryQuestion(TrustPreviouslyResidentPage, userAnswers, "previouslyResidentOffshoreCountry"),

        converter.yesNoQuestion(TrustHasBusinessRelationshipInUkYesNoPage, userAnswers, "trustHasBusinessRelationshipInUkYesNo"),
        converter.yesNoQuestion(RegisteringTrustFor5APage, userAnswers, "settlorBenefitsFromAssets"),
        converter.yesNoQuestion(InheritanceTaxActPage, userAnswers, "forPurposeOfSection218"),
        converter.yesNoQuestion(AgentOtherThanBarristerPage, userAnswers, "agentCreatedTrust"),
        converter.yesNoQuestion(Schedule3aExemptYesNoPage, userAnswers, "schedule3aExemptYesNo")
      )
    } else {
      Seq(
        converter.stringQuestion(TrustNamePage, userAnswers, "trustName"),
        converter.dateQuestion(WhenTrustSetupPage, userAnswers, "whenTrustSetup"),

        converter.whichIdentifier(userAnswers),
        utrOrUrnRow,

        converter.yesNoQuestion(TrustUkPropertyYesNoPage, userAnswers, "trustUkPropertyYesNo"),
        converter.yesNoQuestion(TrustRecordedOnAnotherRegisterYesNoPage, userAnswers, "trustRecordedOnAnotherRegisterYesNo"),
        converter.yesNoQuestion(TrustHasBusinessRelationshipInUkYesNoPage, userAnswers, "trustHasBusinessRelationshipInUkYesNo"),
        converter.yesNoQuestion(Schedule3aExemptYesNoPage, userAnswers, "schedule3aExemptYesNo")
      )
    }

    Seq(answerSectionWithRows(rows, userAnswers.isTrustMigratingFromNonTaxableToTaxable))
  }

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("trustsDetails")

}
