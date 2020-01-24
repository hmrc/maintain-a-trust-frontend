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

package utils.print

import java.time.LocalDate

import base.SpecBase
import models.pages.{DeedOfVariation, KindOfTrust}
import pages.settlors.SetUpAfterSettlorDiedYesNoPage
import pages.settlors.living_settlor.trust_type._
import pages.trustdetails._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class TrustDetailsAndTypePrintPlaybackHelperSpec extends SpecBase {

  "Playback print helper" must {

    "generate trust type for trust setup after settlor died" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value
        .set(GovernedInsideTheUKPage, true).success.value
        .set(AdministrationInsideUKPage, true).success.value
        .set(EstablishedUnderScotsLawPage, true).success.value
        .set(TrustResidentOffshorePage, true).success.value
        .set(TrustPreviouslyResidentPage, "DE").success.value

        .set(SetUpAfterSettlorDiedYesNoPage, true).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustName.checkYourAnswersLabel"),
              answer = Html("Trust Ltd."),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("whenTrustSetup.checkYourAnswersLabel"),
              answer = Html("1 June 2019"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("governedInsideTheUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("administrationInsideUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is resident in the UK"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("establishedUnderScotsLaw.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentOffshore.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustPreviouslyResident.checkYourAnswersLabel"),
              answer = Html("Germany"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust through a deed of variation or family agreement given trust was set up in addition to a will trust" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value
        .set(GovernedInsideTheUKPage, false).success.value
        .set(CountryGoverningTrustPage, "DE").success.value
        .set(AdministrationInsideUKPage, false).success.value
        .set(CountryAdministeringTrustPage, "DE").success.value
        .set(RegisteringTrustFor5APage, false).success.value
        .set(InheritanceTaxActPage, true).success.value
        .set(AgentOtherThanBarristerPage, true).success.value

        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.Deed).success.value
        .set(SetUpInAdditionToWillTrustYesNoPage, true).success.value
        .set(HowDeedOfVariationCreatedPage, DeedOfVariation.AdditionToWill).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustName.checkYourAnswersLabel"),
              answer = Html("Trust Ltd."),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("whenTrustSetup.checkYourAnswersLabel"),
              answer = Html("1 June 2019"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("governedInsideTheUK.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("countryGoverningTrust.checkYourAnswersLabel"),
              answer = Html("Germany"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("administrationInsideUK.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("countryAdministeringTrust.checkYourAnswersLabel"),
              answer = Html("Germany"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("registeringTrustFor5A.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("inheritanceTaxAct.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("agentOtherThanBarrister.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Deed")),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("setupInAdditionToWillTrustYesNo.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust through a deed of variation or family agreement given trust was not set up in addition to a will trust" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value
        .set(GovernedInsideTheUKPage, true).success.value
        .set(AdministrationInsideUKPage, true).success.value
        .set(EstablishedUnderScotsLawPage, false).success.value
        .set(TrustResidentOffshorePage, false).success.value

        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.Deed).success.value
        .set(SetUpInAdditionToWillTrustYesNoPage, false).success.value
        .set(HowDeedOfVariationCreatedPage, DeedOfVariation.ReplacedWill).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustName.checkYourAnswersLabel"),
              answer = Html("Trust Ltd."),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("whenTrustSetup.checkYourAnswersLabel"),
              answer = Html("1 June 2019"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("governedInsideTheUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("administrationInsideUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is resident in the UK"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("establishedUnderScotsLaw.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentOffshore.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Deed")),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("setupInAdditionToWillTrustYesNo.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("howDeedOfVariationCreated.checkYourAnswersLabel"),
              answer = Html(messages("deedOfVariation.replaceWillTrust")),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust created during their lifetime to gift or transfer assets" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value
        .set(GovernedInsideTheUKPage, true).success.value
        .set(AdministrationInsideUKPage, true).success.value
        .set(RegisteringTrustFor5APage, false).success.value
        .set(InheritanceTaxActPage, false).success.value

        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.Intervivos).success.value
        .set(HoldoverReliefYesNoPage, true).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustName.checkYourAnswersLabel"),
              answer = Html("Trust Ltd."),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("whenTrustSetup.checkYourAnswersLabel"),
              answer = Html("1 June 2019"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("governedInsideTheUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("administrationInsideUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("registeringTrustFor5A.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("inheritanceTaxAct.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Lifetime")),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("holdoverReliefYesNo.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust for a building or building with tenants" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(TrustNamePage, "Trust Ltd.").success.value
        .set(WhenTrustSetupPage, LocalDate.of(2019,6,1)).success.value
        .set(GovernedInsideTheUKPage, true).success.value
        .set(AdministrationInsideUKPage, true).success.value
        .set(RegisteringTrustFor5APage, true).success.value

        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.FlatManagement).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustName.checkYourAnswersLabel"),
              answer = Html("Trust Ltd."),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("whenTrustSetup.checkYourAnswersLabel"),
              answer = Html("1 June 2019"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("governedInsideTheUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("administrationInsideUK.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("registeringTrustFor5A.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            )
          ),
          sectionKey = Some(messages("answerPage.section.trustsDetails.heading"))
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Building")),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust for the repair of historic buildings" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.HeritageMaintenanceFund).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust details")
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Repair")),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust for the employees of a company given it is an efrbs" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.Employees).success.value
        .set(EfrbsYesNoPage, true).success.value
        .set(EfrbsStartDatePage, LocalDate.of(1970, 2, 1)).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust details")
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Employees")),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("employerFinancedRetirementBenefitsSchemeYesNo.checkYourAnswersLabel"),
              answer = Html("Yes"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("employerFinancedRetirementBenefitsSchemeStartDate.checkYourAnswersLabel"),
              answer = Html("1 February 1970"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

    "generate trust type for trust for the employees of a company given it is not an efrbs" in {

      val helper = injector.instanceOf[PrintPlaybackHelper]

      val answers = emptyUserAnswers
        .set(SetUpAfterSettlorDiedYesNoPage, false).success.value
        .set(KindOfTrustPage, KindOfTrust.Employees).success.value
        .set(EfrbsYesNoPage, false).success.value

      val result = helper.trustDetails(answers)

      result mustBe Seq(
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("trustResidentInUK.checkYourAnswersLabel"),
              answer = Html("Based on your answers, the trust is not resident in the UK"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust details")
        ),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(
              label = messages("setUpAfterSettlorDied.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("kindOfTrust.checkYourAnswersLabel"),
              answer = Html(messages("kindOfTrust.Employees")),
              changeUrl = None
            ),
            AnswerRow(
              label = messages("employerFinancedRetirementBenefitsSchemeYesNo.checkYourAnswersLabel"),
              answer = Html("No"),
              changeUrl = None
            )
          ),
          sectionKey = Some("Trust type")
        )
      )

    }

  }

}
