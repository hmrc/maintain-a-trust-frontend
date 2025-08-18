/*
 * Copyright 2025 HM Revenue & Customs
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

package utils.print

import base.SpecBase
import models.pages.TrusteesBased.InternationalAndUkBasedTrustees
import models.pages.WhatIsNext.NeedsToPayTax
import models.pages.{DeedOfVariation, KindOfTrust}
import pages.WhatIsNextPage
import pages.settlors.living_settlor.trust_type._
import pages.trustdetails.{SetUpAfterSettlorDiedYesNoPage, _}
import play.twirl.api.Html
import utils.print.sections.TrustDetailsPrinter
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class TrustDetailsPrinterSpec extends SpecBase {

  private val helper: TrustDetailsPrinter = injector.instanceOf[TrustDetailsPrinter]

  private val date: LocalDate = LocalDate.parse("2019-06-01")

  "TrustDetailsPrinter" when {

    "migrating from non-taxable to taxable" must {

      val baseAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, NeedsToPayTax).value
        .set(TrustNamePage, "Trust Ltd.").value
        .set(WhenTrustSetupPage, date).value
        .set(GovernedInsideTheUKPage, false).value
        .set(CountryGoverningTrustPage, "FR").value
        .set(AdministrationInsideUKPage, false).value
        .set(CountryAdministeringTrustPage, "DE").value
        .set(SetUpAfterSettlorDiedYesNoPage, false).value
        .set(KindOfTrustPage, KindOfTrust.Deed).value
        .set(HowDeedOfVariationCreatedPage, DeedOfVariation.ReplacedWill).value
        .set(HoldoverReliefYesNoPage, true).value
        .set(EfrbsYesNoPage, true).value
        .set(EfrbsStartDatePage, date).value
        .set(TrustUkPropertyYesNoPage, true).value
        .set(TrustRecordedOnAnotherRegisterYesNoPage, false).value
        .set(WhereTrusteesBasedPage, InternationalAndUkBasedTrustees).value

      "generate an answer section" when {

        "uk based" in {

          val answers = baseAnswers
            .set(SettlorsUkBasedPage, true).value
            .set(EstablishedUnderScotsLawPage, true).value
            .set(TrustResidentOffshorePage, true).value
            .set(TrustPreviouslyResidentPage, "US").value
            .set(Schedule3aExemptYesNoPage, true).value

          val actualSection = helper.print(answers)

          actualSection mustBe Seq(
            AnswerSection(
              headingKey = None,
              rows = Seq(
                AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
                AnswerRow("When was the trust created?", Html("1 June 2019"), None),
                AnswerRow("Which unique identifier does the trust have?", Html("Unique Taxpayer Reference (UTR)"), None),
                AnswerRow("Unique Taxpayer Reference (UTR)", Html("1234567890"), None),
                AnswerRow("Is the trust governed by UK law?", Html("No"), None),
                AnswerRow("What country governs the trust?", Html("France"), None),
                AnswerRow("Does the trust’s general administration take place in the UK?", Html("No"), None),
                AnswerRow("In what country is the trust administered?", Html("Germany"), None),
                AnswerRow("Was the trust set up after the settlor died?", Html("No"), None),
                AnswerRow("What kind of trust did the settlor create?", Html("A trust through a Deed of Variation or family agreement"), None),
                AnswerRow("Why was the deed of variation created?", Html("To replace a will trust"), None),
                AnswerRow("Was Gift Hold-Over Relief claimed on any of the trust’s assets?", Html("Yes"), None),
                AnswerRow("Is this an employer-financed retirement benefits scheme?", Html("Yes"), None),
                AnswerRow("When did the employer-financed retirement benefits scheme start?", Html("1 June 2019"), None),
                AnswerRow("Has the trust acquired land or property in the UK since 6 October 2020?", Html("Yes"), None),
                AnswerRow("Is the trust registered on the trust register of any countries within the European Economic Area (EEA)?", Html("No"), None),
                AnswerRow("Are the trustees based in the UK?", Html("The trust contains trustees based in and outside the UK"), None),
                AnswerRow("Are any of the settlors based in the UK?", Html("Yes"), None),
                AnswerRow("Was the trust created under Scots law?", Html("Yes"), None),
                AnswerRow("Has the trust ever been based offshore?", Html("Yes"), None),
                AnswerRow("Where was the trust based before?", Html("United States of America"), None),
                AnswerRow("Does the trust have a Schedule 3a data sharing exemption?", Html("Yes"), None)
              ),
              sectionKey = Some("Trust details")
            )
          )
        }

        "non-uk based" in {

          val answers = baseAnswers
            .set(SettlorsUkBasedPage, false).value
            .set(TrustHasBusinessRelationshipInUkYesNoPage, true).value
            .set(RegisteringTrustFor5APage, false).value
            .set(InheritanceTaxActPage, true).value
            .set(AgentOtherThanBarristerPage, true).value
            .set(Schedule3aExemptYesNoPage, false).value

          val actualSection = helper.print(answers)

          actualSection mustBe Seq(
            AnswerSection(
              headingKey = None,
              rows = Seq(
                AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
                AnswerRow("When was the trust created?", Html("1 June 2019"), None),
                AnswerRow("Which unique identifier does the trust have?", Html("Unique Taxpayer Reference (UTR)"), None),
                AnswerRow("Unique Taxpayer Reference (UTR)", Html("1234567890"), None),
                AnswerRow("Is the trust governed by UK law?", Html("No"), None),
                AnswerRow("What country governs the trust?", Html("France"), None),
                AnswerRow("Does the trust’s general administration take place in the UK?", Html("No"), None),
                AnswerRow("In what country is the trust administered?", Html("Germany"), None),
                AnswerRow("Was the trust set up after the settlor died?", Html("No"), None),
                AnswerRow("What kind of trust did the settlor create?", Html("A trust through a Deed of Variation or family agreement"), None),
                AnswerRow("Why was the deed of variation created?", Html("To replace a will trust"), None),
                AnswerRow("Was Gift Hold-Over Relief claimed on any of the trust’s assets?", Html("Yes"), None),
                AnswerRow("Is this an employer-financed retirement benefits scheme?", Html("Yes"), None),
                AnswerRow("When did the employer-financed retirement benefits scheme start?", Html("1 June 2019"), None),
                AnswerRow("Has the trust acquired land or property in the UK since 6 October 2020?", Html("Yes"), None),
                AnswerRow("Is the trust registered on the trust register of any countries within the European Economic Area (EEA)?", Html("No"), None),
                AnswerRow("Are the trustees based in the UK?", Html("The trust contains trustees based in and outside the UK"), None),
                AnswerRow("Are any of the settlors based in the UK?", Html("No"), None),
                AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None),
                AnswerRow("Are you registering the trust because the settlor benefits from the trust’s assets?", Html("No"), None),
                AnswerRow("Are you registering the trust for Inheritance Tax reasons?", Html("Yes"), None),
                AnswerRow("Has an agent who is not a barrister created this trust?", Html("Yes"), None),
                AnswerRow("Does the trust have a Schedule 3a data sharing exemption?", Html("No"), None)
              ),
              sectionKey = Some("Trust details")
            )
          )
        }
      }
    }

    "not migrating from non-taxable to taxable" must {

      "generate an answer section with trust name, created date and utr" in {
        val year2019 = 2019

        val answers = emptyUserAnswersForUtr
          .set(TrustNamePage, "Trust Ltd.").value
          .set(WhenTrustSetupPage, LocalDate.of(year2019, java.time.Month.JUNE, 1)).value
          .set(TrustUkPropertyYesNoPage, true).value
          .set(TrustRecordedOnAnotherRegisterYesNoPage, false).value
          .set(TrustHasBusinessRelationshipInUkYesNoPage, true).value

        val actualSection = helper.print(answers)

        actualSection mustBe Seq(
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
              AnswerRow("When was the trust created?", Html("1 June 2019"), None),
              AnswerRow("Which unique identifier does the trust have?", Html("Unique Taxpayer Reference (UTR)"), None),
              AnswerRow("Unique Taxpayer Reference (UTR)", Html("1234567890"), None),
              AnswerRow("Has the trust acquired land or property in the UK since 6 October 2020?", Html("Yes"), None),
              AnswerRow("Is the trust registered on the trust register of any countries within the European Economic Area (EEA)?", Html("No"), None),
              AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None)
            ),
            sectionKey = Some("Trust details")
          )
        )
      }

      "generate an answer section with trust name, created date and urn" in {

        val answers = emptyUserAnswersForUrn
          .set(TrustNamePage, "Trust Ltd.").value
          .set(WhenTrustSetupPage, date).value
          .set(TrustUkPropertyYesNoPage, true).value
          .set(TrustRecordedOnAnotherRegisterYesNoPage, false).value
          .set(TrustHasBusinessRelationshipInUkYesNoPage, true).value

        val actualSection = helper.print(answers)

        actualSection mustBe Seq(
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
              AnswerRow("When was the trust created?", Html("1 June 2019"), None),
              AnswerRow("Which unique identifier does the trust have?", Html("Unique Reference Number (URN)"), None),
              AnswerRow("Unique Reference Number (URN)", Html("XATRUST12345678"), None),
              AnswerRow("Has the trust acquired land or property in the UK since 6 October 2020?", Html("Yes"), None),
              AnswerRow("Is the trust registered on the trust register of any countries within the European Economic Area (EEA)?", Html("No"), None),
              AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None)
            ),
            sectionKey = Some("Trust details")
          )
        )
      }
    }
  }

}
