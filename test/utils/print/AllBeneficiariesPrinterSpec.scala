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

package utils.print

import base.SpecBase
import models.HowManyBeneficiaries.Over1
import models.http.PassportType
import models.pages.RoleInCompany
import models.{Description, DetailsType, FullName, UKAddress}
import pages.beneficiaries.charity._
import pages.beneficiaries.classOfBeneficiary._
import pages.beneficiaries.company._
import pages.beneficiaries.individual._
import pages.beneficiaries.large._
import pages.beneficiaries.other._
import pages.beneficiaries.trust._
import play.twirl.api.Html
import utils.print.sections.beneficiaries.AllBeneficiariesPrinter
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class AllBeneficiariesPrinterSpec extends SpecBase {

  private val helper: AllBeneficiariesPrinter = injector.instanceOf[AllBeneficiariesPrinter]

  "AllBeneficiariesPrinter" must {

    "generate charity beneficiaries sections" in {

      val charityBen1Name = "Red Cross Ltd."
      val charityBen2Name = "Bernardos"

      val answers = emptyUserAnswersForUtr
        .set(CharityBeneficiaryNamePage(0), charityBen1Name).success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(CharityBeneficiaryShareOfIncomePage(0), "98").success.value
        .set(CharityBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(CharityBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(CharityBeneficiaryAddressYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressPage(0),
          UKAddress(
            line1 = "line1",
            line2 = "line2",
            line3 = Some("line3"),
            line4 = Some("line4"),
            postcode = "NE981ZZ"
          )
        ).success.value

        .set(CharityBeneficiaryNamePage(1), charityBen2Name).success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(1), true).success.value
        .set(CharityBeneficiaryAddressYesNoPage(1), false).success.value
        .set(CharityBeneficiaryUtrPage(1), "1234567890").success.value
        .set(CharityBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(CharityBeneficiaryCountryOfResidencePage(1), "FR").success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Charity beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("charityBeneficiaryName.checkYourAnswersLabel"), answer = Html("Red Cross Ltd."), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", charityBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryShareOfIncome.checkYourAnswersLabel", charityBen1Name), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", charityBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", charityBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidence.checkYourAnswersLabel", charityBen1Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryAddressYesNo.checkYourAnswersLabel", charityBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryAddressUKYesNo.checkYourAnswersLabel", charityBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryAddress.checkYourAnswersLabel", charityBen1Name), answer = Html("line1<br />line2<br />line3<br />line4<br />NE981ZZ"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Charity beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("charityBeneficiaryName.checkYourAnswersLabel"), answer = Html("Bernardos"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", charityBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", charityBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", charityBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryCountryOfResidence.checkYourAnswersLabel", charityBen2Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryAddressYesNo.checkYourAnswersLabel", charityBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("charityBeneficiaryUtr.checkYourAnswersLabel", charityBen2Name), answer = Html("1234567890"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate individual beneficiaries sections with masked passport" in {

      val answers = emptyUserAnswersForUtr
        .set(IndividualBeneficiaryRoleInCompanyPage(0), RoleInCompany.Director).success.value
        .set(IndividualBeneficiaryNamePage(0), FullName("Michael", None, "Finnegan")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryDateOfBirthPage(0), LocalDate.of(1996, 2, 3)).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryIncomePage(0), "98").success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(0), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(0), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryNationalInsuranceNumberPage(0), "JB123456C").success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(0), true).success.value

        .set(IndividualBeneficiaryRoleInCompanyPage(1), RoleInCompany.Employee).success.value
        .set(IndividualBeneficiaryNamePage(1), FullName("Joe", None, "Bloggs")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(1), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(IndividualBeneficiaryAddressYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(1), true).success.value

        .set(IndividualBeneficiaryRoleInCompanyPage(2), RoleInCompany.NA).success.value
        .set(IndividualBeneficiaryNamePage(2), FullName("Paul", None, "Chuckle")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(2), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(2), "FR").success.value
        .set(IndividualBeneficiaryAddressYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryAddressUKYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryAddressPage(2), UKAddress("line 1", "line 2", None, None, "NE11NE")).success.value
        .set(IndividualBeneficiaryPassportIDCardYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryPassportIDCardPage(2), PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), DetailsType.Combined)).success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(2), true).success.value

      val result = helper.entities(answers)

      val name1 = "Michael Finnegan"
      val name2 = "Joe Bloggs"
      val name3 = "Paul Chuckle"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Individual beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name1), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name1), answer = Html("Director"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirth.checkYourAnswersLabel", name1), answer = Html("3 February 1996"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncome.checkYourAnswersLabel", name1), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceNumber.checkYourAnswersLabel", name1), answer = Html("JB 12 34 56 C"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Individual beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name2), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name2), answer = Html("Employee"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Individual beneficiary 3"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name3), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name3), answer = Html("Not a director or employee"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressUKYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressUK.checkYourAnswersLabel", name3), answer = Html("line 1<br />line 2<br />NE11NE"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryPassportIDCardYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryPassportIDCard.checkYourAnswersLabel", name3), answer = Html("Germany<br />Number ending QWER<br />2 February 2020"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate individual beneficiaries sections with bad nino and unmasked passport" in {

      val answers = emptyUserAnswersForUtr
        .set(IndividualBeneficiaryRoleInCompanyPage(0), RoleInCompany.Director).success.value
        .set(IndividualBeneficiaryNamePage(0), FullName("Michael", None, "Finnegan")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryDateOfBirthPage(0), LocalDate.of(1996, 2, 3)).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryIncomePage(0), "98").success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(0), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(0), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryNationalInsuranceNumberPage(0), "JB123456").success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(0), true).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(0), true).success.value

        .set(IndividualBeneficiaryRoleInCompanyPage(1), RoleInCompany.Employee).success.value
        .set(IndividualBeneficiaryNamePage(1), FullName("Joe", None, "Bloggs")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(1), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(IndividualBeneficiaryAddressYesNoPage(1), false).success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(1), true).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(1), true).success.value

        .set(IndividualBeneficiaryRoleInCompanyPage(2), RoleInCompany.NA).success.value
        .set(IndividualBeneficiaryNamePage(2), FullName("Paul", None, "Chuckle")).success.value
        .set(IndividualBeneficiaryDateOfBirthYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryIncomeYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryCountryOfNationalityInTheUkYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfNationalityPage(2), "FR").success.value
        .set(IndividualBeneficiaryNationalInsuranceYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfResidenceYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryCountryOfResidenceInTheUkYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryCountryOfResidencePage(2), "FR").success.value
        .set(IndividualBeneficiaryAddressYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryAddressUKYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryAddressPage(2), UKAddress("line 1", "line 2", None, None, "NE11NE")).success.value
        .set(IndividualBeneficiaryPassportIDCardYesNoPage(2), true).success.value
        .set(IndividualBeneficiaryPassportIDCardPage(2), PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), DetailsType.Passport)).success.value
        .set(IndividualBeneficiaryVulnerableYesNoPage(2), false).success.value
        .set(IndividualBeneficiaryMentalCapacityYesNoPage(2), true).success.value

      val result = helper.entities(answers)

      val name1 = "Michael Finnegan"
      val name2 = "Joe Bloggs"
      val name3 = "Paul Chuckle"

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Individual beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name1), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name1), answer = Html("Director"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirth.checkYourAnswersLabel", name1), answer = Html("3 February 1996"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncome.checkYourAnswersLabel", name1), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceNumber.checkYourAnswersLabel", name1), answer = Html("JB123456"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name1), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name1), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name1), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Individual beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name2), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name2), answer = Html("Employee"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name2), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressYesNo.checkYourAnswersLabel", name2), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name2), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Individual beneficiary 3"),
          rows = Seq(
            AnswerRow(label = messages("individualBeneficiaryName.checkYourAnswersLabel"), answer = Html(name3), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryRoleInCompany.checkYourAnswersLabel", name3), answer = Html("Not a director or employee"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryDateOfBirthYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryIncomeYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationalityUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfNationality.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryNationalInsuranceYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryCountryOfResidence.checkYourAnswersLabel", name3), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressUKYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryAddressUK.checkYourAnswersLabel", name3), answer = Html("line 1<br />line 2<br />NE11NE"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryPassportIDCardYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryPassportIDCard.checkYourAnswersLabel", name3), answer = Html("Germany<br />KSJDFKSDHF6456545147852369QWER<br />2 February 2020"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryVulnerableYesNo.checkYourAnswersLabel", name3), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("individualBeneficiaryMentalCapacityYesNo.checkYourAnswersLabel", name3), answer = Html("Yes"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate company beneficiaries sections" in {

      val companyBen1Name = "Amazon"
      val companyBen2Name = "Apple"

      val answers = emptyUserAnswersForUtr
        .set(CompanyBeneficiaryNamePage(0), companyBen1Name).success.value
        .set(CompanyBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(CompanyBeneficiaryShareOfIncomePage(0), "98").success.value
        .set(CompanyBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(CompanyBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(CompanyBeneficiaryAddressYesNoPage(0), true).success.value
        .set(CompanyBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(CompanyBeneficiaryAddressPage(0),
          UKAddress(
            line1 = "line1",
            line2 = "line2",
            line3 = Some("line3"),
            line4 = Some("line4"),
            postcode = "NE981ZZ"
          )
        ).success.value

        .set(CompanyBeneficiaryNamePage(1), companyBen2Name).success.value
        .set(CompanyBeneficiaryDiscretionYesNoPage(1), true).success.value
        .set(CompanyBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(CompanyBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(CompanyBeneficiaryAddressYesNoPage(1), false).success.value
        .set(CompanyBeneficiaryUtrPage(1), "1234567890").success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Company beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("companyBeneficiaryName.checkYourAnswersLabel"), answer = Html("Amazon"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", companyBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryShareOfIncome.checkYourAnswersLabel", companyBen1Name), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", companyBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", companyBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidence.checkYourAnswersLabel", companyBen1Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryAddressYesNo.checkYourAnswersLabel", companyBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryAddressUKYesNo.checkYourAnswersLabel", companyBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryAddress.checkYourAnswersLabel", companyBen1Name), answer = Html("line1<br />line2<br />line3<br />line4<br />NE981ZZ"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Company beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("companyBeneficiaryName.checkYourAnswersLabel", companyBen2Name), answer = Html("Apple"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", companyBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", companyBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", companyBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryCountryOfResidence.checkYourAnswersLabel", companyBen2Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryAddressYesNo.checkYourAnswersLabel", companyBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("companyBeneficiaryUtr.checkYourAnswersLabel", companyBen2Name), answer = Html("1234567890"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate trust beneficiaries sections" in {

      val trustBen1Name = "Trust of Adam"
      val trustBen2Name = "Grandchildren of Adam"

      val answers = emptyUserAnswersForUtr
        .set(TrustBeneficiaryNamePage(0), trustBen1Name).success.value
        .set(TrustBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(TrustBeneficiaryShareOfIncomePage(0), "98").success.value
        .set(TrustBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(TrustBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(TrustBeneficiaryAddressYesNoPage(0), true).success.value
        .set(TrustBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(TrustBeneficiaryAddressPage(0),
          UKAddress(
            line1 = "line1",
            line2 = "line2",
            line3 = Some("line3"),
            line4 = Some("line4"),
            postcode = "NE981ZZ"
          )
        ).success.value

        .set(TrustBeneficiaryNamePage(1), trustBen2Name).success.value
        .set(TrustBeneficiaryDiscretionYesNoPage(1), true).success.value
        .set(TrustBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(TrustBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(TrustBeneficiaryAddressYesNoPage(1), false).success.value
        .set(TrustBeneficiaryUtrPage(1), "1234567890").success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Trust beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("trustBeneficiaryName.checkYourAnswersLabel"), answer = Html("Trust of Adam"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", trustBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryShareOfIncome.checkYourAnswersLabel",trustBen1Name), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", trustBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", trustBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidence.checkYourAnswersLabel", trustBen1Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryAddressYesNo.checkYourAnswersLabel",trustBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryAddressUKYesNo.checkYourAnswersLabel",trustBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryAddress.checkYourAnswersLabel",trustBen1Name), answer = Html("line1<br />line2<br />line3<br />line4<br />NE981ZZ"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Trust beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("trustBeneficiaryName.checkYourAnswersLabel"), answer = Html("Grandchildren of Adam"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel",trustBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", trustBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", trustBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidence.checkYourAnswersLabel", trustBen2Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryAddressYesNo.checkYourAnswersLabel",trustBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryUtr.checkYourAnswersLabel",trustBen2Name), answer = Html("1234567890"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate large beneficiaries sections" in {

      val largeBen1Name = "Amazon"
      val largeBen2Name = "Apple"

      val answers = emptyUserAnswersForUtr
        .set(LargeBeneficiaryNamePage(0), largeBen1Name).success.value
        .set(LargeBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(LargeBeneficiaryShareOfIncomePage(0), "98").success.value
        .set(LargeBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(LargeBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(LargeBeneficiaryAddressYesNoPage(0), true).success.value
        .set(LargeBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(LargeBeneficiaryAddressPage(0),
          UKAddress(
            line1 = "line1",
            line2 = "line2",
            line3 = Some("line3"),
            line4 = Some("line4"),
            postcode = "NE981ZZ"
          )
        ).success.value
        .set(LargeBeneficiaryDescriptionPage(0), Description("Description", None, None, None, None)).success.value
        .set(LargeBeneficiaryNumberOfBeneficiariesPage(0), Over1).success.value

        .set(LargeBeneficiaryNamePage(1), largeBen2Name).success.value
        .set(LargeBeneficiaryDiscretionYesNoPage(1), true).success.value
        .set(LargeBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(LargeBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(LargeBeneficiaryAddressYesNoPage(1), false).success.value
        .set(LargeBeneficiaryUtrPage(1), "1234567890").success.value
        .set(LargeBeneficiaryDescriptionPage(1), Description("Description", None, None, None, None)).success.value
        .set(LargeBeneficiaryNumberOfBeneficiariesPage(1), Over1).success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Employment related beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("largeBeneficiaryName.checkYourAnswersLabel"), answer = Html("Amazon"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", largeBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", largeBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidence.checkYourAnswersLabel", largeBen1Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryAddressYesNo.checkYourAnswersLabel", largeBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryAddressUKYesNo.checkYourAnswersLabel", largeBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryAddress.checkYourAnswersLabel", largeBen1Name), answer = Html("line1<br />line2<br />line3<br />line4<br />NE981ZZ"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryDescription.checkYourAnswersLabel", largeBen1Name), answer = Html("Description"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryNumberOfBeneficiaries.checkYourAnswersLabel", largeBen1Name), answer = Html("1 to 100"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Employment related beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("largeBeneficiaryName.checkYourAnswersLabel", largeBen2Name), answer = Html("Apple"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", largeBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", largeBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("trustBeneficiaryCountryOfResidence.checkYourAnswersLabel", largeBen2Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryAddressYesNo.checkYourAnswersLabel", largeBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryUtr.checkYourAnswersLabel", largeBen2Name), answer = Html("1234567890"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryDescription.checkYourAnswersLabel", largeBen2Name), answer = Html("Description"), changeUrl = None),
            AnswerRow(label = messages("largeBeneficiaryNumberOfBeneficiaries.checkYourAnswersLabel", largeBen2Name), answer = Html("1 to 100"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate other beneficiaries sections" in {

      val otherBen1Name = "Dog"
      val otherBen2Name = "Cat"

      val answers = emptyUserAnswersForUtr
        .set(OtherBeneficiaryDescriptionPage(0), otherBen1Name).success.value
        .set(OtherBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(OtherBeneficiaryShareOfIncomePage(0), "98").success.value
        .set(OtherBeneficiaryCountryOfResidenceYesNoPage(0), true).success.value
        .set(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0), false).success.value
        .set(OtherBeneficiaryCountryOfResidencePage(0), "FR").success.value
        .set(OtherBeneficiaryAddressYesNoPage(0), true).success.value
        .set(OtherBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(OtherBeneficiaryAddressPage(0),
          UKAddress(
            line1 = "line1",
            line2 = "line2",
            line3 = Some("line3"),
            line4 = Some("line4"),
            postcode = "NE981ZZ"
          )
        ).success.value

        .set(OtherBeneficiaryDescriptionPage(1), otherBen2Name).success.value
        .set(OtherBeneficiaryDiscretionYesNoPage(1), true).success.value
        .set(OtherBeneficiaryCountryOfResidenceYesNoPage(1), true).success.value
        .set(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(1), false).success.value
        .set(OtherBeneficiaryCountryOfResidencePage(1), "FR").success.value
        .set(OtherBeneficiaryAddressYesNoPage(1), false).success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Other beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("otherBeneficiaryDescription.checkYourAnswersLabel"), answer = Html("Dog"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel", otherBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryShareOfIncome.checkYourAnswersLabel",otherBen1Name), answer = Html("98%"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", otherBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", otherBen1Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidence.checkYourAnswersLabel", otherBen1Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryAddressYesNo.checkYourAnswersLabel",otherBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryAddressUKYesNo.checkYourAnswersLabel",otherBen1Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryAddress.checkYourAnswersLabel",otherBen1Name), answer = Html("line1<br />line2<br />line3<br />line4<br />NE981ZZ"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Other beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("otherBeneficiaryDescription.checkYourAnswersLabel"), answer = Html("Cat"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryShareOfIncomeYesNo.checkYourAnswersLabel",otherBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidenceYesNo.checkYourAnswersLabel", otherBen2Name), answer = Html("Yes"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidenceUkYesNo.checkYourAnswersLabel", otherBen2Name), answer = Html("No"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryCountryOfResidence.checkYourAnswersLabel", otherBen2Name), answer = Html("France"), changeUrl = None),
            AnswerRow(label = messages("otherBeneficiaryAddressYesNo.checkYourAnswersLabel",otherBen2Name), answer = Html("No"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

    "generate class of beneficiaries sections" in {

      val classBenDescription1 = "Grandchildren"
      val classBenDescription2 = "Spouses"

      val answers = emptyUserAnswersForUtr
        .set(ClassOfBeneficiaryDescriptionPage(0), classBenDescription1).success.value
        .set(ClassOfBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(ClassOfBeneficiaryShareOfIncomePage(0), "55").success.value

        .set(ClassOfBeneficiaryDescriptionPage(1), classBenDescription2).success.value
        .set(ClassOfBeneficiaryDiscretionYesNoPage(1), true).success.value

      val result = helper.entities(answers)

      result mustBe Seq(
        AnswerSection(None, Nil, Some(messages("answerPage.section.beneficiaries.heading"))),
        AnswerSection(
          headingKey = Some("Class of beneficiary 1"),
          rows = Seq(
            AnswerRow(label = messages("classBeneficiaryDescription.checkYourAnswersLabel"), answer = Html("Grandchildren"), changeUrl = None)
          ),
          sectionKey = None
        ),
        AnswerSection(
          headingKey = Some("Class of beneficiary 2"),
          rows = Seq(
            AnswerRow(label = messages("classBeneficiaryDescription.checkYourAnswersLabel"), answer = Html("Spouses"), changeUrl = None)
          ),
          sectionKey = None
        )
      )

    }

  }

}
