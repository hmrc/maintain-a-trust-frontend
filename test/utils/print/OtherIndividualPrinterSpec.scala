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
import pages.individual._
import play.twirl.api.Html
import utils.print.sections.otherindividuals.OtherIndividualsPrinter
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class OtherIndividualPrinterSpec extends SpecBase {

  private val helper: OtherIndividualsPrinter = injector.instanceOf[OtherIndividualsPrinter]

  "OtherIndividualPrinter" must {

    "generate other individuals sections given individuals" in {

      val answers = emptyUserAnswersForUtr
        .set(OtherIndividualNamePage(0), FullName("Joe", None, "Bloggs")).success.value
        .set(OtherIndividualDateOfBirthYesNoPage(0), false).success.value
        .set(OtherIndividualCountryOfNationalityYesNoPage(0), true).success.value
        .set(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0), false).success.value
        .set(OtherIndividualCountryOfNationalityPage(0), "FR").success.value
        .set(OtherIndividualNationalInsuranceYesNoPage(0), true).success.value
        .set(OtherIndividualNationalInsuranceNumberPage(0), "JB123456C").success.value
        .set(OtherIndividualCountryOfResidenceYesNoPage(0), true).success.value
        .set(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(OtherIndividualCountryOfResidencePage(0), "FR").success.value
        .set(OtherIndividualMentalCapacityYesNoPage(0), true).success.value

        .set(OtherIndividualNamePage(1), FullName("John", None, "Doe")).success.value
        .set(OtherIndividualDateOfBirthYesNoPage(1), true).success.value
        .set(OtherIndividualDateOfBirthPage(1), LocalDate.of(1996, 2, 3)).success.value
        .set(OtherIndividualCountryOfNationalityYesNoPage(1), true).success.value
        .set(OtherIndividualCountryOfNationalityInTheUkYesNoPage(1), false).success.value
        .set(OtherIndividualCountryOfNationalityPage(1), "FR").success.value
        .set(OtherIndividualCountryOfResidenceYesNoPage(1), true).success.value
        .set(OtherIndividualCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(OtherIndividualCountryOfResidencePage(1), "FR").success.value
        .set(OtherIndividualAddressYesNoPage(1), false).success.value
        .set(OtherIndividualMentalCapacityYesNoPage(1), true).success.value

        .set(OtherIndividualNamePage(2), FullName("Michael", None, "Finnegan")).success.value
        .set(OtherIndividualDateOfBirthYesNoPage(2), false).success.value
        .set(OtherIndividualCountryOfNationalityYesNoPage(2), true).success.value
        .set(OtherIndividualCountryOfNationalityInTheUkYesNoPage(2), false).success.value
        .set(OtherIndividualCountryOfNationalityPage(2), "FR").success.value
        .set(OtherIndividualCountryOfResidenceYesNoPage(2), true).success.value
        .set(OtherIndividualCountryOfResidenceInTheUkYesNoPage(2), false).success.value
        .set(OtherIndividualCountryOfResidencePage(2), "FR").success.value
        .set(OtherIndividualAddressYesNoPage(2), true).success.value
        .set(OtherIndividualAddressUKYesNoPage(2), true).success.value
        .set(OtherIndividualAddressPage(2), UKAddress("line 1", "line 2", None, None, "NE11NE")).success.value
        .set(OtherIndividualPassportIDCardYesNoPage(2), false).success.value
        .set(OtherIndividualMentalCapacityYesNoPage(2), true).success.value
        
        .set(OtherIndividualNamePage(3), FullName("Paul", None, "Chuckle")).success.value
        .set(OtherIndividualDateOfBirthYesNoPage(3), true).success.value
        .set(OtherIndividualDateOfBirthPage(3), LocalDate.of(1947, 10, 18)).success.value
        .set(OtherIndividualCountryOfNationalityYesNoPage(3), true).success.value
        .set(OtherIndividualCountryOfNationalityInTheUkYesNoPage(3), false).success.value
        .set(OtherIndividualCountryOfNationalityPage(3), "FR").success.value
        .set(OtherIndividualCountryOfResidenceYesNoPage(3), true).success.value
        .set(OtherIndividualCountryOfResidenceInTheUkYesNoPage(3), false).success.value
        .set(OtherIndividualCountryOfResidencePage(3), "FR").success.value
        .set(OtherIndividualAddressYesNoPage(3), true).success.value
        .set(OtherIndividualAddressUKYesNoPage(3), false).success.value
        .set(OtherIndividualAddressPage(3), InternationalAddress("line 1", "line 2", None, "FR")).success.value
        .set(OtherIndividualPassportIDCardYesNoPage(3), true).success.value
        .set(OtherIndividualPassportIDCardPage(3), PassportOrIdCardDetails("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2))).success.value
        .set(OtherIndividualMentalCapacityYesNoPage(3), true).success.value
      
      val result = helper.entities(answers)

      val name1 = "Joe Bloggs"
      val name2 = "John Doe"
      val name3 = "Michael Finnegan"
      val name4 = "Paul Chuckle"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.otherIndividuals.heading"))),
        AnswerSection(
          headingKey = Some("Other individual 1"),
          rows = Seq(
            AnswerRow(label = messages("otherIndividualName.checkYourAnswersLabel"), answer = Html(name1), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirthYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationality.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualNINOYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualNINO.checkYourAnswersLabel", name1), answer = Html("JB 12 34 56 C"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidence.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualMentalCapacityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Other individual 2"),
          rows = Seq(
            AnswerRow(label = messages("otherIndividualName.checkYourAnswersLabel"), answer = Html(name2), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirthYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirth.checkYourAnswersLabel", name2), answer = Html("3 February 1996"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationality.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidence.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddressYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualMentalCapacityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Other individual 3"),
          rows = Seq(
            AnswerRow(label = messages("otherIndividualName.checkYourAnswersLabel"), answer = Html(name3), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirthYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationality.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidence.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddressYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddressUKYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddress.checkYourAnswersLabel", name3), answer = Html("line 1<br />line 2<br />NE11NE"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualPassportIDCardYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualMentalCapacityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Other individual 4"),
          rows = Seq(
            AnswerRow(label = messages("otherIndividualName.checkYourAnswersLabel"), answer = Html(name4), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirthYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualDateOfBirth.checkYourAnswersLabel", name4), answer = Html("18 October 1947"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationalityUkYesNo.checkYourAnswersLabel", name4), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfNationality.checkYourAnswersLabel", name4), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidenceUkYesNo.checkYourAnswersLabel", name4), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualCountryOfResidence.checkYourAnswersLabel", name4), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddressYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddressUKYesNo.checkYourAnswersLabel", name4), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualAddress.checkYourAnswersLabel", name4), answer = Html("line 1<br />line 2<br />France"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualPassportIDCardYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualPassportIDCard.checkYourAnswersLabel", name4), answer = Html("Germany<br />Number ending QWER<br />2 February 2020"), changeUrl = None),
            AnswerRow(label = messages("otherIndividualMentalCapacityYesNo.checkYourAnswersLabel", name4), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        )
      )
    }

  }

}
