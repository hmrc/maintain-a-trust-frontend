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
import models.{FullName, InternationalAddress, PassportOrIdCardDetails, UKAddress}
import models.pages.IndividualOrBusiness
import pages.protectors._
import pages.protectors.business._
import pages.protectors.individual._
import play.twirl.api.Html
import utils.countryoptions.CountryOptions
import utils.print.sections.protectors.AllProtectorsPrinter
import viewmodels.{AnswerRow, AnswerSection}

class ProtectorPrintPlaybackHelperSpec extends SpecBase {

  "Playback print helper" must {

    "generate protector sections given individuals" in {

      val answers = emptyUserAnswers
        .set(ProtectorIndividualOrBusinessPage(0), IndividualOrBusiness.Individual).success.value
        .set(IndividualProtectorNamePage(0), FullName("Joe", None, "Bloggs")).success.value
        .set(IndividualProtectorDateOfBirthYesNoPage(0), false).success.value
        .set(IndividualProtectorNINOYesNoPage(0), true).success.value
        .set(IndividualProtectorNINOPage(0), "JB123456C").success.value

        .set(ProtectorIndividualOrBusinessPage(1), IndividualOrBusiness.Individual).success.value
        .set(IndividualProtectorNamePage(1), FullName("John", None, "Doe")).success.value
        .set(IndividualProtectorDateOfBirthYesNoPage(1), true).success.value
        .set(IndividualProtectorDateOfBirthPage(1), LocalDate.of(1996, 2, 3)).success.value
        .set(IndividualProtectorAddressYesNoPage(1), false).success.value

        .set(ProtectorIndividualOrBusinessPage(2), IndividualOrBusiness.Individual).success.value
        .set(IndividualProtectorNamePage(2), FullName("Michael", None, "Finnegan")).success.value
        .set(IndividualProtectorDateOfBirthYesNoPage(2), false).success.value
        .set(IndividualProtectorAddressYesNoPage(2), true).success.value
        .set(IndividualProtectorAddressPage(2), UKAddress("line 1", "line 2", None, None, "NE11NE")).success.value
        .set(IndividualProtectorPassportIDCardYesNoPage(2), false).success.value

        .set(ProtectorIndividualOrBusinessPage(3), IndividualOrBusiness.Individual).success.value
        .set(IndividualProtectorNamePage(3), FullName("Paul", None, "Chuckle")).success.value
        .set(IndividualProtectorDateOfBirthYesNoPage(3), true).success.value
        .set(IndividualProtectorDateOfBirthPage(3), LocalDate.of(1947, 10, 18)).success.value
        .set(IndividualProtectorAddressYesNoPage(3), true).success.value
        .set(IndividualProtectorAddressPage(3), UKAddress("line 1", "line 2", None, None, "DH11DH")).success.value
        .set(IndividualProtectorPassportIDCardYesNoPage(3), true).success.value
        .set(IndividualProtectorPassportIDCardPage(3), PassportOrIdCardDetails("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2))).success.value

      val helper = new AllProtectorsPrinter(countryOptions = injector.instanceOf[CountryOptions], userAnswers = answers)

      val result = helper.allProtectors

      val name1 = "Joe Bloggs"
      val name2 = "John Doe"
      val name3 = "Michael Finnegan"
      val name4 = "Paul Chuckle"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.protectors.heading"))),
        AnswerSection(
          headingKey = Some("Protector 1"),
          rows = Seq(
            AnswerRow(label = messages("individualProtectorName.checkYourAnswersLabel"), answer = Html(name1), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirthYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorNINOYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorNINO.checkYourAnswersLabel", name1), answer = Html("JB 12 34 56 C"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 2"),
          rows = Seq(
            AnswerRow(label = messages("individualProtectorName.checkYourAnswersLabel"), answer = Html(name2), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirthYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirth.checkYourAnswersLabel", name2), answer = Html("3 February 1996"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddressYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 3"),
          rows = Seq(
            AnswerRow(label = messages("individualProtectorName.checkYourAnswersLabel"), answer = Html(name3), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirthYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddressYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddress.checkYourAnswersLabel", name3), answer = Html("line 1<br />line 2<br />NE11NE"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorPassportIDCardYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 4"),
          rows = Seq(
            AnswerRow(label = messages("individualProtectorName.checkYourAnswersLabel"), answer = Html(name4), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirthYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirth.checkYourAnswersLabel", name4), answer = Html("18 October 1947"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddressYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddress.checkYourAnswersLabel", name4), answer = Html("line 1<br />line 2<br />DH11DH"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorPassportIDCardYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorPassportIDCard.checkYourAnswersLabel", name4), answer = Html("Germany<br />KSJDFKSDHF6456545147852369QWER<br />2 February 2020"), changeUrl = None)
          ),
          sectionKey = None
        )
      )
    }

    "generate protector sections given businesses" in {

      val answers = emptyUserAnswers
        .set(ProtectorIndividualOrBusinessPage(0), IndividualOrBusiness.Business).success.value
        .set(BusinessProtectorNamePage(0), "Bernardos").success.value
        .set(BusinessProtectorUtrYesNoPage(0), true).success.value
        .set(BusinessProtectorUtrPage(0), "1234567890").success.value

        .set(ProtectorIndividualOrBusinessPage(1), IndividualOrBusiness.Business).success.value
        .set(BusinessProtectorNamePage(1), "Red Cross Ltd.").success.value
        .set(BusinessProtectorUtrYesNoPage(1), false).success.value
        .set(BusinessProtectorAddressYesNoPage(1), true).success.value
        .set(BusinessProtectorAddressUKYesNoPage(1), false).success.value
        .set(BusinessProtectorAddressPage(1), InternationalAddress(s"line 1", "line 2", None, "DE")).success.value

        .set(ProtectorIndividualOrBusinessPage(2), IndividualOrBusiness.Business).success.value
        .set(BusinessProtectorNamePage(2), "Amazon").success.value
        .set(BusinessProtectorUtrYesNoPage(2), false).success.value
        .set(BusinessProtectorAddressYesNoPage(2), false).success.value

      val helper = new AllProtectorsPrinter(countryOptions = injector.instanceOf[CountryOptions], userAnswers = answers)

      val result = helper.allProtectors

      val company1 = "Bernardos"
      val company2 = "Red Cross Ltd."
      val company3 = "Amazon"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.protectors.heading"))),
        AnswerSection(
          headingKey = Some("Protector 1"),
          rows = Seq(
            AnswerRow(label = messages("companyProtectorName.checkYourAnswersLabel"), answer = Html(company1), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtrYesNo.checkYourAnswersLabel", company1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtr.checkYourAnswersLabel", company1), answer = Html("1234567890"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 2"),
          rows = Seq(
            AnswerRow(label = messages("companyProtectorName.checkYourAnswersLabel"), answer = Html(company2), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtrYesNo.checkYourAnswersLabel", company2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorAddressYesNo.checkYourAnswersLabel", company2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorAddressUkYesNo.checkYourAnswersLabel", company2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorAddress.checkYourAnswersLabel", company2), answer = Html("line 1<br />line 2<br />Germany"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 3"),
          rows = Seq(
            AnswerRow(label = messages("companyProtectorName.checkYourAnswersLabel"), answer = Html(company3), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtrYesNo.checkYourAnswersLabel", company3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorAddressYesNo.checkYourAnswersLabel", company3), answer = Html("No"), changeUrl = None)
          ),
          sectionKey = None
        )
      )
    }

    "generate protector sections given an individual and company" in {

      val answers = emptyUserAnswers
        .set(ProtectorIndividualOrBusinessPage(0), IndividualOrBusiness.Individual).success.value
        .set(IndividualProtectorNamePage(0), FullName("Paul", None, "Chuckle")).success.value
        .set(IndividualProtectorDateOfBirthYesNoPage(0), true).success.value
        .set(IndividualProtectorDateOfBirthPage(0), LocalDate.of(1947, 10, 18)).success.value
        .set(IndividualProtectorAddressYesNoPage(0), true).success.value
        .set(IndividualProtectorAddressPage(0), UKAddress("line 1", "line 2", None, None, "DH11DH")).success.value
        .set(IndividualProtectorPassportIDCardYesNoPage(0), true).success.value
        .set(IndividualProtectorPassportIDCardPage(0), PassportOrIdCardDetails("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2))).success.value

        .set(ProtectorIndividualOrBusinessPage(1), IndividualOrBusiness.Business).success.value
        .set(BusinessProtectorNamePage(1), "Bernardos").success.value
        .set(BusinessProtectorUtrYesNoPage(1), true).success.value
        .set(BusinessProtectorUtrPage(1), "1234567890").success.value

      val helper = new AllProtectorsPrinter(countryOptions = injector.instanceOf[CountryOptions], userAnswers = answers)

      val result = helper.allProtectors

      val name1 = "Paul Chuckle"
      val company1 = "Bernardos"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.protectors.heading"))),
        AnswerSection(
          headingKey = Some("Protector 1"),
          rows = Seq(
            AnswerRow(label = messages("individualProtectorName.checkYourAnswersLabel"), answer = Html(name1), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirthYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorDateOfBirth.checkYourAnswersLabel", name1), answer = Html("18 October 1947"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddressYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorAddress.checkYourAnswersLabel", name1), answer = Html("line 1<br />line 2<br />DH11DH"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorPassportIDCardYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualProtectorPassportIDCard.checkYourAnswersLabel", name1), answer = Html("Germany<br />KSJDFKSDHF6456545147852369QWER<br />2 February 2020"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Protector 2"),
          rows = Seq(
            AnswerRow(label = messages("companyProtectorName.checkYourAnswersLabel"), answer = Html(company1), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtrYesNo.checkYourAnswersLabel", company1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyProtectorUtr.checkYourAnswersLabel", company1), answer = Html("1234567890"), changeUrl = None)
          ),
          sectionKey = None
        )
      )
    }
  }
}