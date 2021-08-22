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
import models.{FullName, InternationalAddress, PassportOrIdCardDetails, UKAddress}
import pages.correspondence.CorrespondenceAddressPage
import pages.trustees._
import play.twirl.api.Html
import utils.print.sections.trustees.AllTrusteesPrinter

import java.time.LocalDate

class AllTrusteesPrinterSpec extends SpecBase with AnswerSectionMatchers with UserAnswersWriting {

  private val helper: AllTrusteesPrinter = injector.instanceOf[AllTrusteesPrinter]

  "AllTrusteesPrinter" when {

    "when the lead trustee is an UK individual" must {
      "generate lead trustee section" in {

        val (answers, _) = (for {
          _ <- individualUKTrustee(0)
          _ <- TrusteeNamePage(0) is FullName("Wild", Some("Bill"), "Hickock")
          _ <- TrusteeDateOfBirthPage(0) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(0) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(0) is true
          _ <- TrusteeNinoPage(0) is "AA111111A"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is true
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeAddressInTheUKPage(0) is true
          _ <- TrusteeUkAddressPage(0) is UKAddress("Address 1", "Address 2", None, None, "AA11 1AA")
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- TrusteeEmailYesNoPage(0) is true
          _ <- TrusteeEmailPage(0) is "aa@aabb.com"
          _ <- IsThisLeadTrusteePage(0) is true
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 2

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the lead trustee’s name?" -> Html("Wild Bill Hickock"),
          "What is Wild Hickock’s date of birth?" -> Html("23 January 1975"),
          "Do you know Wild Hickock’s country of nationality?" -> Html("Yes"),
          "Does Wild Hickock have UK nationality?" -> Html("Yes"),
          "Does Wild Hickock have a National Insurance number?" -> Html("Yes"),
          "What is Wild Hickock’s National Insurance number?" -> Html("AA 11 11 11 A"),
          "Do you know Wild Hickock’s country of residence?" -> Html("Yes"),
          "Does Wild Hickock have UK residency?" -> Html("Yes"),
          "Does Wild Hickock live in the UK?" -> Html("Yes"),
          "What is Wild Hickock’s address?" -> Html("Address 1<br />Address 2<br />AA11 1AA"),
          "Do you know Wild Hickock’s email address?" -> Html("Yes"),
          "What is Wild Hickock’s email address?" -> Html("aa@aabb.com"),
          "What is Wild Hickock’s telephone number?" -> Html("67676767676")
        )
      }
    }

    "when the lead trustee is a non-UK individual" must {
      "generate a lead trustee section" in {

        val (answers, _) = (for {
          _ <- individualUKTrustee(0)
          _ <- individualNonUkTrustee(0)
          _ <- TrusteeNamePage(0) is FullName("William", None, "Bonny")
          _ <- TrusteeDateOfBirthPage(0) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(0) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfNationalityPage(0) is "FR"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfResidencePage(0) is "FR"
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeInternationalAddressPage(0) is InternationalAddress("Address 1", "Address 2", None, "DE")
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- TrusteeEmailYesNoPage(0) is true
          _ <- TrusteeEmailPage(0) is "aa@aabb.com"
          _ <- IsThisLeadTrusteePage(0) is true
          _ <- TrusteePassportIDCardPage(0) is PassportOrIdCardDetails("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2))
          _ <- TrusteePassportIDCardYesNoPage(0) is true
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 2

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the lead trustee’s name?" -> Html("William Bonny"),
          "What is William Bonny’s date of birth?" -> Html("23 January 1975"),
          "Do you know William Bonny’s country of nationality?" -> Html("Yes"),
          "Does William Bonny have UK nationality?" -> Html("No"),
          "What is William Bonny’s country of nationality?" -> Html("France"),
          "Does William Bonny have a National Insurance number?" -> Html("No"),
          "Does William Bonny live in the UK?" -> Html("No"),
          "What is William Bonny’s address?" -> Html("Address 1<br />Address 2<br />Germany"),
          "Do you know William Bonny’s passport or ID card details?" -> Html("Yes"),
          "What are William Bonny’s passport or ID card details?" -> Html("Germany<br />Number ending QWER<br />2 February 2020"),
          "Do you know William Bonny’s country of residence?" -> Html("Yes"),
          "Does William Bonny have UK residency?" -> Html("No"),
          "What is William Bonny’s country of residence?" -> Html("France"),
          "Do you know William Bonny’s email address?" -> Html("Yes"),
          "What is William Bonny’s email address?" -> Html("aa@aabb.com"),
          "What is William Bonny’s telephone number?" -> Html("67676767676")
        )
      }
    }

    "when the lead trustee is an UK individual with nino and no address" must {
      "generate lead trustee section with correspondence address" in {

        val (answers, _) = (for {
          _ <- individualUKTrustee(0)
          _ <- TrusteeNamePage(0) is FullName("Wild", Some("Bill"), "Hickock")
          _ <- TrusteeDateOfBirthPage(0) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(0) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(0) is true
          _ <- TrusteeNinoPage(0) is "AA111111A"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is true
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeAddressInTheUKPage(0).isRemoved
          _ <- TrusteeUkAddressPage(0).isRemoved
          _ <- CorrespondenceAddressPage is UKAddress("Address 1", "Address 2", None, None, "AA11 1AA")
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- TrusteeEmailYesNoPage(0) is true
          _ <- TrusteeEmailPage(0) is "aa@aabb.com"
          _ <- IsThisLeadTrusteePage(0) is true
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 2

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the lead trustee’s name?" -> Html("Wild Bill Hickock"),
          "What is Wild Hickock’s date of birth?" -> Html("23 January 1975"),
          "Do you know Wild Hickock’s country of nationality?" -> Html("Yes"),
          "Does Wild Hickock have UK nationality?" -> Html("Yes"),
          "Does Wild Hickock have a National Insurance number?" -> Html("Yes"),
          "What is Wild Hickock’s National Insurance number?" -> Html("AA 11 11 11 A"),
          "Do you know Wild Hickock’s country of residence?" -> Html("Yes"),
          "Does Wild Hickock have UK residency?" -> Html("Yes"),
          "What is Wild Hickock’s address?" -> Html("Address 1<br />Address 2<br />AA11 1AA"),
          "Do you know Wild Hickock’s email address?" -> Html("Yes"),
          "What is Wild Hickock’s email address?" -> Html("aa@aabb.com"),
          "What is Wild Hickock’s telephone number?" -> Html("67676767676")
        )
      }
    }

    "when the lead trustee is a company" must {
      "generate a lead trustee section" in {

        val (answers, _) = (for {
          _ <- ukCompanyTrustee(0)
          _ <- TrusteeOrgNamePage(0) is "Lead Trustee Company"
          _ <- TrusteeUtrYesNoPage(0) is true
          _ <- TrusteeUtrPage(0) is "1234567890"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfResidencePage(0) is "FR"
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeAddressInTheUKPage(0) is true
          _ <- TrusteeUkAddressPage(0) is UKAddress("Address 1", "Address 2", None, None, "AA11 1AA")
          _ <- TrusteeEmailYesNoPage(0) is false
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- IsThisLeadTrusteePage(0) is true
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 2

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the business’s name?" -> Html("Lead Trustee Company"),
          "Is this trustee a UK registered company?" -> Html("Yes"),
          "What is Lead Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("1234567890"),
          "Do you know Lead Trustee Company’s country of residence?" -> Html("Yes"),
          "Does Lead Trustee Company have UK residency?" -> Html("No"),
          "What is Lead Trustee Company’s country of residence?" -> Html("France"),
          "Does Lead Trustee Company live in the UK?" -> Html("Yes"),
          "What is Lead Trustee Company’s address?" -> Html("Address 1<br />Address 2<br />AA11 1AA"),
          "Do you know Lead Trustee Company’s email address?" -> Html("No"),
          "What is Lead Trustee Company’s telephone number?" -> Html("67676767676")
        )
      }
    }

    "when the lead trustee is a company with utr and no address" must {
      "generate a lead trustee section with correspondence address" in {

        val (answers, _) = (for {
          _ <- ukCompanyTrustee(0)
          _ <- TrusteeOrgNamePage(0) is "Lead Trustee Company"
          _ <- TrusteeUtrYesNoPage(0) is true
          _ <- TrusteeUtrPage(0) is "1234567890"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is true
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeAddressInTheUKPage(0).isRemoved
          _ <- TrusteeUkAddressPage(0).isRemoved
          _ <- CorrespondenceAddressPage is UKAddress("Address 1", "Address 2", None, None, "AA11 1AA")
          _ <- TrusteeEmailYesNoPage(0) is true
          _ <- TrusteeEmailPage(0) is "aa@aabb.com"
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- IsThisLeadTrusteePage(0) is true
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 2

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the business’s name?" -> Html("Lead Trustee Company"),
          "Is this trustee a UK registered company?" -> Html("Yes"),
          "What is Lead Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("1234567890"),
          "Do you know Lead Trustee Company’s country of residence?" -> Html("Yes"),
          "Does Lead Trustee Company have UK residency?" -> Html("Yes"),
          "What is Lead Trustee Company’s address?" -> Html("Address 1<br />Address 2<br />AA11 1AA"),
          "Do you know Lead Trustee Company’s email address?" -> Html("Yes"),
          "What is Lead Trustee Company’s email address?" -> Html("aa@aabb.com"),
          "What is Lead Trustee Company’s telephone number?" -> Html("67676767676")
        )
      }
    }

    "when the lead trustee is an individual and other trustees" must {
      "generate a trustee section for each trustee" in {

        val (answers, _) = (for {
          _ <- individualUKTrustee(0)
          _ <- TrusteeNamePage(0) is FullName("Wild", Some("Bill"), "Hickock")
          _ <- TrusteeDateOfBirthPage(0) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(0) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfNationalityPage(0) is "FR"
          _ <- TrusteeNinoPage(0) is "AA111111A"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfResidencePage(0) is "FR"
          _ <- TrusteeAddressYesNoPage(0).isRemoved
          _ <- TrusteeAddressInTheUKPage(0) is true
          _ <- TrusteeUkAddressPage(0) is UKAddress("Address 1", "Address 2", None, None, "AA11 1AA")
          _ <- TrusteeTelephoneNumberPage(0) is "67676767676"
          _ <- TrusteeEmailYesNoPage(0) is false
          _ <- IsThisLeadTrusteePage(0) is true
          _ <- ukCompanyTrustee(1)
          _ <- TrusteeOrgNamePage(1) is "Trustee Company"
          _ <- TrusteeUtrYesNoPage(1) is true
          _ <- TrusteeUtrPage(1) is "1234567890"
          _ <- TrusteeCountryOfResidenceYesNoPage(1) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(1) is false
          _ <- TrusteeCountryOfResidencePage(1) is "FR"
          _ <- IsThisLeadTrusteePage(1) is false
          _ <- individualUKTrustee(2)
          _ <- TrusteeNamePage(2) is FullName("Individual", None, "trustee")
          _ <- TrusteeDateOfBirthYesNoPage(2) is true
          _ <- TrusteeDateOfBirthPage(2) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(2) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(2) is false
          _ <- TrusteeCountryOfNationalityPage(2) is "FR"
          _ <- TrusteeNinoYesNoPage(2) is true
          _ <- TrusteeNinoPage(2) is "NH111111A"
          _ <- TrusteeCountryOfResidenceYesNoPage(2) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(2) is false
          _ <- TrusteeCountryOfResidencePage(2) is "FR"
          _ <- TrusteeMentalCapacityYesNoPage(2) is true
          _ <- IsThisLeadTrusteePage(2) is false
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 4

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the lead trustee’s name?" -> Html("Wild Bill Hickock"),
          "What is Wild Hickock’s date of birth?" -> Html("23 January 1975"),
          "Do you know Wild Hickock’s country of nationality?" -> Html("Yes"),
          "Does Wild Hickock have UK nationality?" -> Html("No"),
          "What is Wild Hickock’s country of nationality?" -> Html("France"),
          "Does Wild Hickock have a National Insurance number?" -> Html("Yes"),
          "What is Wild Hickock’s National Insurance number?" -> Html("AA 11 11 11 A"),
          "Do you know Wild Hickock’s country of residence?" -> Html("Yes"),
          "Does Wild Hickock have UK residency?" -> Html("No"),
          "What is Wild Hickock’s country of residence?" -> Html("France"),
          "Does Wild Hickock live in the UK?" -> Html("Yes"),
          "What is Wild Hickock’s address?" -> Html("Address 1<br />Address 2<br />AA11 1AA"),
          "Do you know Wild Hickock’s email address?" -> Html("No"),
          "What is Wild Hickock’s telephone number?" -> Html("67676767676")
        )
        result must containSectionWithHeadingAndValues(messages("answerPage.section.trustee.subheading", 2),
          "What is the business’s name?" -> Html("Trustee Company"),
          "Do you know Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("Yes"),
          "Do you know Trustee Company’s country of residence?" -> Html("Yes"),
          "Does Trustee Company have UK residency?" -> Html("No"),
          "What is Trustee Company’s country of residence?" -> Html("France"),
          "What is Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("1234567890")
        )
        result must containSectionWithHeadingAndValues(messages("answerPage.section.trustee.subheading", 3),
          "What is the trustee’s name?" -> Html("Individual trustee"),
          "Do you know Individual trustee’s date of birth?" -> Html("Yes"),
          "What is Individual trustee’s date of birth?" -> Html("23 January 1975"),
          "Do you know Individual trustee’s country of nationality?" -> Html("Yes"),
          "Does Individual trustee have UK nationality?" -> Html("No"),
          "What is Individual trustee’s country of nationality?" -> Html("France"),
          "Do you know Individual trustee’s National Insurance number?" -> Html("Yes"),
          "What is Individual trustee’s National Insurance number?" -> Html("NH 11 11 11 A"),
          "Do you know Individual trustee’s country of residence?" -> Html("Yes"),
          "Does Individual trustee have UK residency?" -> Html("No"),
          "What is Individual trustee’s country of residence?" -> Html("France"),
          "Does Individual trustee have mental capacity at the time of registration?" -> Html("Yes")
        )

      }
    }

    "when the lead trustee is a company and other trustees" must {
      "generate a trustee section for each trustee" in {

        val (answers, _) = (for {
          _ <- ukCompanyTrustee(0)
          _ <- TrusteeOrgNamePage(0) is "Lead Trustee Company"
          _ <- TrusteeUtrYesNoPage(0) is true
          _ <- TrusteeUtrPage(0) is "1234567890"
          _ <- TrusteeCountryOfResidenceYesNoPage(0) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(0) is false
          _ <- TrusteeCountryOfResidencePage(0) is "FR"
          _ <- IsThisLeadTrusteePage(0) is true
          _ <- ukCompanyTrustee(1)
          _ <- TrusteeOrgNamePage(1) is "Trustee Company"
          _ <- TrusteeUtrYesNoPage(1) is true
          _ <- TrusteeUtrPage(1) is "1234567890"
          _ <- TrusteeCountryOfResidenceYesNoPage(1) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(1) is false
          _ <- TrusteeCountryOfResidencePage(1) is "FR"
          _ <- IsThisLeadTrusteePage(1) is false
          _ <- individualUKTrustee(2)
          _ <- TrusteeNamePage(2) is FullName("Individual", None, "trustee")
          _ <- TrusteeDateOfBirthYesNoPage(2) is true
          _ <- TrusteeDateOfBirthPage(2) is LocalDate.parse("1975-01-23")
          _ <- TrusteeCountryOfNationalityYesNoPage(2) is true
          _ <- TrusteeCountryOfNationalityInTheUkYesNoPage(2) is false
          _ <- TrusteeCountryOfNationalityPage(2) is "FR"
          _ <- TrusteeNinoYesNoPage(2) is true
          _ <- TrusteeNinoPage(2) is "NH111111A"
          _ <- TrusteeCountryOfResidenceYesNoPage(2) is true
          _ <- TrusteeCountryOfResidenceInTheUkYesNoPage(2) is false
          _ <- TrusteeCountryOfResidencePage(2) is "FR"
          _ <- TrusteeMentalCapacityYesNoPage(2) is true
          _ <- IsThisLeadTrusteePage(2) is false
        } yield Unit).run(emptyUserAnswersForUtr).value

        val result = helper.entities(answers)

        result.size mustEqual 4

        result must containHeadingSection(messages("answerPage.section.trustees.heading"))
        result must containSectionWithHeadingAndValues(messages("answerPage.section.leadTrustee.subheading"),
          "What is the business’s name?" -> Html("Lead Trustee Company"),
          "Is this trustee a UK registered company?" -> Html("Yes"),
          "What is Lead Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("1234567890"),
          "Do you know Lead Trustee Company’s country of residence?" -> Html("Yes"),
          "Does Lead Trustee Company have UK residency?" -> Html("No"),
          "What is Lead Trustee Company’s country of residence?" -> Html("France")
        )
        result must containSectionWithHeadingAndValues(messages("answerPage.section.trustee.subheading", 2),
          "What is the business’s name?" -> Html("Trustee Company"),
          "Do you know Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("Yes"),
          "What is Trustee Company’s Unique Taxpayer Reference (UTR) number?" -> Html("1234567890"),
          "Do you know Trustee Company’s country of residence?" -> Html("Yes"),
          "Does Trustee Company have UK residency?" -> Html("No"),
          "What is Trustee Company’s country of residence?" -> Html("France")
        )
        result must containSectionWithHeadingAndValues(messages("answerPage.section.trustee.subheading", 3),
          "What is the trustee’s name?" -> Html("Individual trustee"),
          "Do you know Individual trustee’s date of birth?" -> Html("Yes"),
          "What is Individual trustee’s date of birth?" -> Html("23 January 1975"),
          "Do you know Individual trustee’s country of nationality?" -> Html("Yes"),
          "Does Individual trustee have UK nationality?" -> Html("No"),
          "What is Individual trustee’s country of nationality?" -> Html("France"),
          "Do you know Individual trustee’s National Insurance number?" -> Html("Yes"),
          "What is Individual trustee’s National Insurance number?" -> Html("NH 11 11 11 A"),
          "Do you know Individual trustee’s country of residence?" -> Html("Yes"),
          "Does Individual trustee have UK residency?" -> Html("No"),
          "What is Individual trustee’s country of residence?" -> Html("France"),
          "Does Individual trustee have mental capacity at the time of registration?" -> Html("Yes")
        )

      }
    }
  }

}
