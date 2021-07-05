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

package utils.print

import java.time.LocalDate
import base.SpecBase
import models.pages.{DeedOfVariation, KindOfTrust}
import models.pages.WhatIsNext.NeedsToPayTax
import pages.WhatIsNextPage
import pages.settlors.living_settlor.trust_type.{EfrbsStartDatePage, EfrbsYesNoPage, HoldoverReliefYesNoPage, HowDeedOfVariationCreatedPage, KindOfTrustPage}
import pages.trustdetails.{SetUpAfterSettlorDiedYesNoPage, _}
import play.twirl.api.Html
import utils.print.sections.TrustDetailsPrinter
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsPrinterSpec extends SpecBase {

  private val helper: TrustDetailsPrinter = injector.instanceOf[TrustDetailsPrinter]

  private val date: LocalDate = LocalDate.parse("2019-06-01")

  "TrustDetailsPrinter" when {

    "migrating from non-taxable to taxable" must {

      val baseAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, NeedsToPayTax).success.value

      "generate an answer section" in {

        val answers = baseAnswers
          .set(TrustNamePage, "Trust Ltd.").success.value
          .set(WhenTrustSetupPage, date).success.value
          .set(GovernedInsideTheUKPage, false).success.value
          .set(CountryGoverningTrustPage, "FR").success.value
          .set(AdministrationInsideUKPage, false).success.value
          .set(CountryAdministeringTrustPage, "DE").success.value
          .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
          .set(KindOfTrustPage, KindOfTrust.Deed).success.value
          .set(HowDeedOfVariationCreatedPage, DeedOfVariation.ReplacedWill).success.value
          .set(HoldoverReliefYesNoPage, true).success.value
          .set(EfrbsYesNoPage, true).success.value
          .set(EfrbsStartDatePage, date).success.value
          .set(TrustUkPropertyYesNoPage, true).success.value
          .set(TrustRecordedOnAnotherRegisterYesNoPage, false).success.value
          .set(TrustHasBusinessRelationshipInUkYesNoPage, true).success.value

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
              AnswerRow("Does the trust own UK land or property?", Html("Yes"), None),
              AnswerRow("Is the trust registered on the trust register of any other countries within the European Economic Area (EEA)?", Html("No"), None),
              AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None)
            ),
            sectionKey = Some("Trust details")
          )
        )
      }
    }

    "not migrating from non-taxable to taxable" must {

      "generate an answer section with trust name, created date and utr" in {

        val answers = emptyUserAnswersForUtr
          .set(TrustNamePage, "Trust Ltd.").success.value
          .set(WhenTrustSetupPage, LocalDate.of(2019, 6, 1)).success.value
          .set(TrustUkPropertyYesNoPage, true).success.value
          .set(TrustRecordedOnAnotherRegisterYesNoPage, false).success.value
          .set(TrustHasBusinessRelationshipInUkYesNoPage, true).success.value

        val actualSection = helper.print(answers)

        actualSection mustBe Seq(
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
              AnswerRow("When was the trust created?", Html("1 June 2019"), None),
              AnswerRow("Which unique identifier does the trust have?", Html("Unique Taxpayer Reference (UTR)"), None),
              AnswerRow("Unique Taxpayer Reference (UTR)", Html("1234567890"), None),
              AnswerRow("Does the trust own UK land or property?", Html("Yes"), None),
              AnswerRow("Is the trust registered on the trust register of any other countries within the European Economic Area (EEA)?", Html("No"), None),
              AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None)
            ),
            sectionKey = Some("Trust details")
          )
        )
      }

      "generate an answer section with trust name, created date and urn" in {

        val answers = emptyUserAnswersForUrn
          .set(TrustNamePage, "Trust Ltd.").success.value
          .set(WhenTrustSetupPage, date).success.value
          .set(TrustUkPropertyYesNoPage, true).success.value
          .set(TrustRecordedOnAnotherRegisterYesNoPage, false).success.value
          .set(TrustHasBusinessRelationshipInUkYesNoPage, true).success.value

        val actualSection = helper.print(answers)

        actualSection mustBe Seq(
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow("What is the trust’s name?", Html("Trust Ltd."), None),
              AnswerRow("When was the trust created?", Html("1 June 2019"), None),
              AnswerRow("Which unique identifier does the trust have?", Html("Unique Reference Number (URN)"), None),
              AnswerRow("Unique Reference Number (URN)", Html("XATRUST12345678"), None),
              AnswerRow("Does the trust own UK land or property?", Html("Yes"), None),
              AnswerRow("Is the trust registered on the trust register of any other countries within the European Economic Area (EEA)?", Html("No"), None),
              AnswerRow("Does the trust have an ongoing business relationship in the UK?", Html("Yes"), None)
            ),
            sectionKey = Some("Trust details")
          )
        )
      }
    }
  }

}
