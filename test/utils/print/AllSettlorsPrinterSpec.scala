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

import base.SpecBase
import models.pages.IndividualOrBusiness
import models.pages.KindOfBusiness.Trading
import models.{FullName, InternationalAddress, PassportOrIdCardDetails, UKAddress, UserAnswers}
import pages.settlors.deceased_settlor._
import pages.settlors.living_settlor._
import play.api.libs.json.{Reads, Writes}
import play.twirl.api.Html
import queries.Settable
import utils.print.sections.settlors.AllSettlorsPrinter
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class AllSettlorsPrinterSpec extends SpecBase {

  private val helper: AllSettlorsPrinter = injector.instanceOf[AllSettlorsPrinter]

  "AllSettlorsPrinter" must {

    "generate deceased settlor sections for maximum dataset" in {

      val name = "Adam Smith"

      val answers = emptyUserAnswersForUtr
        .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
        .set(SettlorDateOfDeathYesNoPage, true).success.value
        .set(SettlorDateOfDeathPage, LocalDate.of(2010, 10, 10)).success.value
        .set(SettlorDateOfBirthYesNoPage, true).success.value
        .set(SettlorDateOfBirthPage, LocalDate.of(1991, 8, 27)).success.value
        .set(DeceasedSettlorCountryOfNationalityYesNoPage, true).success.value
        .set(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfNationalityPage, "FR").success.value
        .set(SettlorNationalInsuranceYesNoPage, true).success.value
        .set(SettlorNationalInsuranceNumberPage, "JP121212A").success.value
        .set(DeceasedSettlorCountryOfResidenceYesNoPage, true).success.value
        .set(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfResidencePage, "FR").success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfDeath.checkYourAnswersLabel", name), answer = Html("10 October 2010"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfBirth.checkYourAnswersLabel", name), answer = Html("27 August 1991"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationalityYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationalityUkYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationality.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorNationalInsuranceNumber.checkYourAnswersLabel", name), answer = Html("JP 12 12 12 A"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceUkYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidence.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    // these unit tests are required while the service doesn't permit maintaining the nationality or residency of a deceased settlor
    // if a deceased settlor was registered in 4MLD, we don't want to see any answer rows relating to nationality or residency
    // if a deceased settlor was registered in 5MLD, we want to see the answer rows relating to nationality and residency
    // however, there is no way of discerning between a 4MLD-registered deceased settlor and a 5MLD-registered deceased settlor with no nationality or residency
    // therefore it was decided to only show the nationality and residency questions if at least one of them is known
    // this does mean unfortunately that if a deceased settlor was registered in 5MLD with unknown nationality and residency, the answers won't be shown
    "correctly display nationality and residency questions" when {

      val name = "Adam Smith"

      "nationality and residency unknown" in {

        val answers = emptyUserAnswersForUtr
          .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
          .set(SettlorDateOfDeathYesNoPage, false).success.value
          .set(SettlorDateOfBirthYesNoPage, false).success.value
          .set(DeceasedSettlorCountryOfNationalityYesNoPage, false).success.value
          .set(SettlorNationalInsuranceYesNoPage, true).success.value
          .set(SettlorNationalInsuranceNumberPage, "JP121212A").success.value
          .set(DeceasedSettlorCountryOfResidenceYesNoPage, false).success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceNumber.checkYourAnswersLabel", name), answer = Html("JP 12 12 12 A"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }

      "only nationality unknown" in {

        val answers = emptyUserAnswersForUtr
          .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
          .set(SettlorDateOfDeathYesNoPage, false).success.value
          .set(SettlorDateOfBirthYesNoPage, false).success.value
          .set(DeceasedSettlorCountryOfNationalityYesNoPage, false).success.value
          .set(SettlorNationalInsuranceYesNoPage, true).success.value
          .set(SettlorNationalInsuranceNumberPage, "JP121212A").success.value
          .set(DeceasedSettlorCountryOfResidenceYesNoPage, true).success.value
          .set(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, true).success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfNationalityYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceNumber.checkYourAnswersLabel", name), answer = Html("JP 12 12 12 A"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfResidenceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfResidenceUkYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }

      "only residency unknown" in {

        val answers = emptyUserAnswersForUtr
          .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
          .set(SettlorDateOfDeathYesNoPage, false).success.value
          .set(SettlorDateOfBirthYesNoPage, false).success.value
          .set(DeceasedSettlorCountryOfNationalityYesNoPage, true).success.value
          .set(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage, true).success.value
          .set(SettlorNationalInsuranceYesNoPage, true).success.value
          .set(SettlorNationalInsuranceNumberPage, "JP121212A").success.value
          .set(DeceasedSettlorCountryOfResidenceYesNoPage, false).success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
          AnswerSection(
            headingKey = None,
            rows = Seq(
              AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfNationalityYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfNationalityUkYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("settlorNationalInsuranceNumber.checkYourAnswersLabel", name), answer = Html("JP 12 12 12 A"), changeUrl = None),
              AnswerRow(label = messages("settlorCountryOfResidenceYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }
    }

    "generate deceased settlor sections for minimum dataset" in {

      val name = "Adam Smith"

      val answers = emptyUserAnswersForUtr
        .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
        .set(SettlorDateOfDeathYesNoPage, false).success.value
        .set(SettlorDateOfBirthYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfNationalityYesNoPage, true).success.value
        .set(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfNationalityPage, "FR").success.value
        .set(DeceasedSettlorCountryOfResidenceYesNoPage, true).success.value
        .set(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfResidencePage, "FR").success.value.set(SettlorNationalInsuranceYesNoPage, false).success.value
        .set(SettlorLastKnownAddressYesNoPage, true).success.value
        .set(SettlorLastKnownAddressUKYesNoPage, true).success.value
        .set(SettlorLastKnownAddressPage, UKAddress(
          line1 = "line 1",
          line2 = "line 2",
          line3 = Some("line 3"),
          line4 = Some("line 4"),
          postcode = "NE981ZZ"
        )).success.value
        .set(SettlorPassportIDCardPage,
          PassportOrIdCardDetails("DE", "123456789", LocalDate.of(2021,10,10))
        ).success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationalityYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationalityUkYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationality.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceUkYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidence.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("settlorLastKnownAddressYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorLastKnownAddressUKYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorUKAddress.checkYourAnswersLabel", name), answer = Html("line 1<br />line 2<br />line 3<br />line 4<br />NE981ZZ"), changeUrl = None),
            AnswerRow(label = messages("settlorPassportOrIdCard.checkYourAnswersLabel", name), answer = Html("Germany<br />Number ending 6789<br />10 October 2021"), changeUrl = None)
          ),
          sectionKey = None
        )
      )
    }

    def uaSet[T:Writes](settable: Settable[T], value: T)(implicit reads: Reads[T]) : UserAnswers => UserAnswers = _.set(settable, value).success.value

    "generate Company Settlor Section" in {
      def businessSettlorBase(index: Int) = uaSet(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business) andThen
        uaSet(SettlorBusinessNamePage(index), "International Exports") andThen
        uaSet(SettlorCountryOfResidenceYesNoPage(index), true) andThen
        uaSet(SettlorCountryOfResidenceInTheUkYesNoPage(index), false) andThen
        uaSet(SettlorCountryOfResidencePage(index), "FR")

      def businessSettlorWithUTR(index: Int) = businessSettlorBase(index) andThen
        uaSet(SettlorUtrYesNoPage(index), true) andThen
        uaSet(SettlorUtrPage(index), "UTRUTRUTRUTR")

      def businessSettlorWithUKAddress(index: Int) = businessSettlorBase(index) andThen
        uaSet(SettlorUtrYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), true) andThen
        uaSet(SettlorAddressUKYesNoPage(index), true) andThen
        uaSet(SettlorAddressPage(index), UKAddress("Line1", "Line2", Some("Line3"), None, "POSTCODE"))

      def businessSettlorWithNonUKAddress(index: Int) = businessSettlorBase(index) andThen
        uaSet(SettlorUtrYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), true) andThen
        uaSet(SettlorAddressUKYesNoPage(index), false) andThen
        uaSet(SettlorAddressPage(index), InternationalAddress( "Line1", "Line2", Some("Line3"), "AN"))

      def businessSettlorWithNoIdentification(index: Int) = businessSettlorBase(index) andThen
        uaSet(SettlorUtrYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), false)

      def businessSettlorInEmployeeRelatedTrust(index: Int) = businessSettlorWithUKAddress(index) andThen
        uaSet(SettlorCompanyTypePage(index), Trading) andThen
        uaSet(SettlorCompanyTimePage(index), false)

      val answers = businessSettlorWithUTR(0) andThen
        businessSettlorWithUKAddress(1) andThen
        businessSettlorWithNonUKAddress(2) andThen
        businessSettlorWithNoIdentification(3) andThen
        businessSettlorInEmployeeRelatedTrust(4)

      val result = helper.entities(answers.apply(emptyUserAnswersForUtr))

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.settlors.heading"))),
        AnswerSection(Some("Settlor 1"),Seq(
          AnswerRow("What is the business’s name?", Html("International Exports"), None),
          AnswerRow("Do you know International Exports’s Unique Taxpayer Reference (UTR) number?", Html("Yes"), None),
          AnswerRow("What is International Exports’s Unique Taxpayer Reference (UTR) number?", Html("UTRUTRUTRUTR"), None),
          AnswerRow("Do you know International Exports’s country of residence?", Html("Yes"), None),
          AnswerRow("Does International Exports have UK residency?", Html("No"), None),
          AnswerRow("What is International Exports’s country of residence?", Html("France"), None)
        ), None),
        AnswerSection(Some("Settlor 2"), Seq(
          AnswerRow("What is the business’s name?", Html("International Exports"), None),
          AnswerRow("Do you know International Exports’s Unique Taxpayer Reference (UTR) number?", Html("No"), None),
          AnswerRow("Do you know International Exports’s country of residence?", Html("Yes"), None),
          AnswerRow("Does International Exports have UK residency?", Html("No"), None),
          AnswerRow("What is International Exports’s country of residence?", Html("France"), None),
          AnswerRow("Do you know International Exports’s address?", Html("Yes"), None),
          AnswerRow("Is International Exports’s address in the UK?", Html("Yes"), None),
          AnswerRow("What is International Exports’s address?", Html("Line1<br />Line2<br />Line3<br />POSTCODE"), None)

        ), None),
        AnswerSection(Some("Settlor 3"), Seq(
          AnswerRow("What is the business’s name?", Html("International Exports"), None),
          AnswerRow("Do you know International Exports’s Unique Taxpayer Reference (UTR) number?", Html("No"), None),
          AnswerRow("Do you know International Exports’s country of residence?", Html("Yes"), None),
          AnswerRow("Does International Exports have UK residency?", Html("No"), None),
          AnswerRow("What is International Exports’s country of residence?", Html("France"), None),
          AnswerRow("Do you know International Exports’s address?", Html("Yes"), None),
          AnswerRow("Is International Exports’s address in the UK?", Html("No"), None),
          AnswerRow("What is International Exports’s address?", Html("Line1<br />Line2<br />Line3<br />Dutch Antilles"), None)
        ), None),
        AnswerSection(Some("Settlor 4"), Seq(
          AnswerRow("What is the business’s name?", Html("International Exports"), None),
          AnswerRow("Do you know International Exports’s Unique Taxpayer Reference (UTR) number?", Html("No"), None),
          AnswerRow("Do you know International Exports’s country of residence?", Html("Yes"), None),
          AnswerRow("Does International Exports have UK residency?", Html("No"), None),
          AnswerRow("What is International Exports’s country of residence?", Html("France"), None),
          AnswerRow("Do you know International Exports’s address?", Html("No"), None)
        ), None),
        AnswerSection(Some("Settlor 5"), Seq(
          AnswerRow("What is the business’s name?", Html("International Exports"), None),
          AnswerRow("Do you know International Exports’s Unique Taxpayer Reference (UTR) number?", Html("No"), None),
          AnswerRow("Do you know International Exports’s country of residence?", Html("Yes"), None),
          AnswerRow("Does International Exports have UK residency?", Html("No"), None),
          AnswerRow("What is International Exports’s country of residence?", Html("France"), None),
          AnswerRow("Do you know International Exports’s address?", Html("Yes"), None),
          AnswerRow("Is International Exports’s address in the UK?", Html("Yes"), None),
          AnswerRow("What is International Exports’s address?", Html("Line1<br />Line2<br />Line3<br />POSTCODE"), None),
          AnswerRow("What kind of business is International Exports?", Html("Trading"), None),
          AnswerRow("At the date of each contribution to the trust, had the business been in existence for at least 2 years?", Html("No"), None)

        ), None)
      )
    }

    "generate Individual Settlor Section" in {

      def baseIndividualSettlor(index: Int) =
        uaSet(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual) andThen
          uaSet(SettlorIndividualNamePage(index), FullName("Joe", None,  "Bloggs")) andThen
          uaSet(SettlorIndividualDateOfBirthYesNoPage(index), true) andThen
          uaSet(SettlorIndividualDateOfBirthPage(index), LocalDate.parse("1934-12-12")) andThen
          uaSet(SettlorCountryOfNationalityYesNoPage(index), true) andThen
          uaSet(SettlorCountryOfNationalityInTheUkYesNoPage(index), false) andThen
          uaSet(SettlorCountryOfNationalityPage(index), "FR") andThen
          uaSet(SettlorCountryOfResidenceYesNoPage(index), true) andThen
          uaSet(SettlorCountryOfResidenceInTheUkYesNoPage(index), false) andThen
          uaSet(SettlorCountryOfResidencePage(index), "FR") andThen
          uaSet(SettlorIndividualMentalCapacityYesNoPage(index), true)

      def individualSettlorWithNino(index: Int) = baseIndividualSettlor(index) andThen
        uaSet(SettlorIndividualNINOYesNoPage(index), true) andThen
        uaSet(SettlorIndividualNINOPage(index), "AA000000A")

      def individualSettlorWithUKAddressAndNoPassportOrIdCard(index: Int) = baseIndividualSettlor(index) andThen
        uaSet(SettlorIndividualNINOYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), true) andThen
        uaSet(SettlorAddressUKYesNoPage(index), true) andThen
        uaSet(SettlorAddressPage(index), UKAddress("Line1", "Line2", Some("Line3"), None, "POSTCODE")) andThen
        uaSet(SettlorIndividualPassportIDCardYesNoPage(index), false)

      def individualSettlorWithInternationalAddressAndIdCard(index: Int) = baseIndividualSettlor(index) andThen
        uaSet(SettlorIndividualNINOYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), true) andThen
        uaSet(SettlorAddressUKYesNoPage(index), false) andThen
        uaSet(SettlorAddressPage(index), InternationalAddress("Line1", "Line2", Some("Line3"), "DE")) andThen
        uaSet(SettlorIndividualPassportIDCardYesNoPage(index), true) andThen
        uaSet(SettlorIndividualPassportIDCardPage(index), PassportOrIdCardDetails("DE", "1234567890", LocalDate.of(2020, 1, 1)))

      def individualSettlorWithNoId(index: Int) = baseIndividualSettlor(index) andThen
        uaSet(SettlorIndividualNINOYesNoPage(index), false) andThen
        uaSet(SettlorAddressYesNoPage(index), false)

      val answers = individualSettlorWithNino(0) andThen
        individualSettlorWithUKAddressAndNoPassportOrIdCard(1) andThen
        individualSettlorWithInternationalAddressAndIdCard(2) andThen
        individualSettlorWithNoId(3)

      val result = helper.entities(answers.apply(emptyUserAnswersForUtr))

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.settlors.heading"))),
        AnswerSection(Some("Settlor 1"),Seq(
          AnswerRow("What is the settlor’s name?", Html("Joe Bloggs"), None),
          AnswerRow("Do you know Joe Bloggs’s date of birth?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s date of birth?", Html("12 December 1934"), None),
          AnswerRow("Do you know Joe Bloggs’s country of nationality?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK nationality?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of nationality?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s National Insurance number?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s National Insurance number?", Html("AA 00 00 00 A"), None),
          AnswerRow("Do you know Joe Bloggs’s country of residence?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK residency?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of residence?", Html("France"), None),
          AnswerRow("Does Joe Bloggs have mental capacity at the time of registration?", Html("Yes"), None)
        ), None),
        AnswerSection(Some("Settlor 2"),Seq(
          AnswerRow("What is the settlor’s name?", Html("Joe Bloggs"), None),
          AnswerRow("Do you know Joe Bloggs’s date of birth?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s date of birth?", Html("12 December 1934"), None),
          AnswerRow("Do you know Joe Bloggs’s country of nationality?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK nationality?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of nationality?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s National Insurance number?", Html("No"), None),
          AnswerRow("Do you know Joe Bloggs’s country of residence?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK residency?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of residence?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s address?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs live in the UK?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s address?", Html("Line1<br />Line2<br />Line3<br />POSTCODE"), None),
          AnswerRow("Do you know Joe Bloggs’s passport or ID card details?", Html("No"), None),
          AnswerRow("Does Joe Bloggs have mental capacity at the time of registration?", Html("Yes"), None)
        ), None),
        AnswerSection(Some("Settlor 3"),Seq(
          AnswerRow("What is the settlor’s name?", Html("Joe Bloggs"), None),
          AnswerRow("Do you know Joe Bloggs’s date of birth?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s date of birth?", Html("12 December 1934"), None),
          AnswerRow("Do you know Joe Bloggs’s country of nationality?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK nationality?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of nationality?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s National Insurance number?", Html("No"), None),
          AnswerRow("Do you know Joe Bloggs’s country of residence?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK residency?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of residence?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s address?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs live in the UK?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s address?", Html("Line1<br />Line2<br />Line3<br />Germany"), None),
          AnswerRow("Do you know Joe Bloggs’s passport or ID card details?", Html("Yes"), None),
          AnswerRow("What are Joe Bloggs’s passport or ID card details?", Html("Germany<br />Number ending 7890<br />1 January 2020"), None),
          AnswerRow("Does Joe Bloggs have mental capacity at the time of registration?", Html("Yes"), None)
        ), None),
        AnswerSection(Some("Settlor 4"),Seq(
          AnswerRow("What is the settlor’s name?", Html("Joe Bloggs"), None),
          AnswerRow("Do you know Joe Bloggs’s date of birth?", Html("Yes"), None),
          AnswerRow("What is Joe Bloggs’s date of birth?", Html("12 December 1934"), None),
          AnswerRow("Do you know Joe Bloggs’s country of nationality?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK nationality?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of nationality?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s National Insurance number?", Html("No"), None),
          AnswerRow("Do you know Joe Bloggs’s country of residence?", Html("Yes"), None),
          AnswerRow("Does Joe Bloggs have UK residency?", Html("No"), None),
          AnswerRow("What is Joe Bloggs’s country of residence?", Html("France"), None),
          AnswerRow("Do you know Joe Bloggs’s address?", Html("No"), None),
          AnswerRow("Does Joe Bloggs have mental capacity at the time of registration?", Html("Yes"), None)
        ), None)
      )
    }

    "generate sections with deceased settlor and additional settlors" in {

      val name = "Adam Smith"

      val answers = emptyUserAnswersForUtr

        .set(SettlorNamePage, FullName("Adam", None, "Smith")).success.value
        .set(SettlorDateOfDeathYesNoPage, false).success.value
        .set(SettlorDateOfBirthYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfNationalityYesNoPage, false).success.value
        .set(SettlorNationalInsuranceYesNoPage, false).success.value
        .set(DeceasedSettlorCountryOfResidenceYesNoPage, true).success.value
        .set(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage, true).success.value
        .set(SettlorLastKnownAddressYesNoPage, false).success.value

        .set(SettlorIndividualOrBusinessPage(0), IndividualOrBusiness.Business).success.value
        .set(SettlorBusinessNamePage(0), "Amazon").success.value
        .set(SettlorUtrYesNoPage(0), false).success.value
        .set(SettlorCountryOfResidenceYesNoPage(0), false).success.value
        .set(SettlorAddressYesNoPage(0), false).success.value

        .set(SettlorIndividualOrBusinessPage(1), IndividualOrBusiness.Individual).success.value
        .set(SettlorIndividualNamePage(1), FullName("Joe", None,  "Bloggs")).success.value
        .set(SettlorIndividualDateOfBirthYesNoPage(1), false).success.value
        .set(SettlorCountryOfNationalityYesNoPage(1), false).success.value
        .set(SettlorIndividualNINOYesNoPage(1), false).success.value
        .set(SettlorCountryOfResidenceYesNoPage(1), false).success.value
        .set(SettlorAddressYesNoPage(1), false).success.value
        .set(SettlorIndividualMentalCapacityYesNoPage(1), true).success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.deceasedSettlor.heading"))),
        AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = messages("settlorName.checkYourAnswersLabel"), answer = Html("Adam Smith"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfDeathYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorDateOfBirthYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfNationalityYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorNationalInsuranceYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorCountryOfResidenceUkYesNo.checkYourAnswersLabel", name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("settlorLastKnownAddressYesNo.checkYourAnswersLabel", name), answer = Html("No"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(None, Nil, Some(messages("answerPage.section.settlors.heading"))),
        AnswerSection(
          headingKey = Some("Settlor 1"),
          rows = Seq(
            AnswerRow("What is the business’s name?", Html("Amazon"), None),
            AnswerRow("Do you know Amazon’s Unique Taxpayer Reference (UTR) number?", Html("No"), None),
            AnswerRow("Do you know Amazon’s country of residence?", Html("No"), None),
            AnswerRow("Do you know Amazon’s address?", Html("No"), None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Settlor 2"),
          rows = Seq(
            AnswerRow("What is the settlor’s name?", Html("Joe Bloggs"), None),
            AnswerRow("Do you know Joe Bloggs’s date of birth?", Html("No"), None),
            AnswerRow("Do you know Joe Bloggs’s country of nationality?", Html("No"), None),
            AnswerRow("Do you know Joe Bloggs’s National Insurance number?", Html("No"), None),
            AnswerRow("Do you know Joe Bloggs’s country of residence?", Html("No"), None),
            AnswerRow("Do you know Joe Bloggs’s address?", Html("No"), None),
            AnswerRow("Does Joe Bloggs have mental capacity at the time of registration?", Html("Yes"), None)
          ),
          sectionKey = None
        )
      )
    }
  }

}
