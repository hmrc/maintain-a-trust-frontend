/*
 * Copyright 2024 HM Revenue & Customs
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
import models.InternationalAddress
import models.pages.ShareClass
import models.pages.WhatIsNext.NeedsToPayTax
import pages.WhatIsNextPage
import pages.assets.business._
import pages.assets.money.MoneyValuePage
import pages.assets.nonEeaBusiness._
import pages.assets.other._
import pages.assets.partnership._
import pages.assets.propertyOrLand._
import pages.assets.shares._
import play.twirl.api.Html
import utils.print.sections.assets.AllAssetsPrinter
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class AllAssetsPrinterSpec extends SpecBase {

  private val helper: AllAssetsPrinter = injector.instanceOf[AllAssetsPrinter]

  private val address = InternationalAddress("Line 1", "Line 2", None, "DE")
  private val date: LocalDate = LocalDate.parse("2019-06-01")
  private val (num100, num101, num200, num1000, num2000, num4000) = (100L, 101L, 200L, 1000L, 2000L, 4000L)

  "AllAssetsPrinter" must {

    "generate assets sections" when {

      val businessName = "Business Name"
      val shareName = "Share Name"

      "migrating from non-taxable to taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).value

          .set(MoneyValuePage(0), num4000).value

          .set(PropertyOrLandAddressYesNoPage(0), false).value
          .set(PropertyOrLandDescriptionPage(0), "Building land name").value
          .set(PropertyOrLandTotalValuePage(0), num2000).value
          .set(TrustOwnAllThePropertyOrLandPage(0), true).value

          .set(PropertyOrLandAddressYesNoPage(1), true).value
          .set(PropertyOrLandAddressUkYesNoPage(1), false).value
          .set(PropertyOrLandAddressPage(1), address).value
          .set(PropertyOrLandTotalValuePage(1), num2000).value
          .set(TrustOwnAllThePropertyOrLandPage(1), false).value
          .set(PropertyLandValueTrustPage(1), num1000).value

          .set(ShareNamePage(0), "Portfolio Name").value
          .set(SharesInAPortfolioPage(0),true).value
          .set(ShareOnStockExchangePage(0), true).value
          .set(ShareQuantityInTrustPage(0), "1000").value
          .set(ShareValueInTrustPage(0), num100).value

          .set(ShareClassPage(1), ShareClass.Preference).value
          .set(ShareNamePage(1), shareName).value
          .set(SharesInAPortfolioPage(1),false).value
          .set(ShareOnStockExchangePage(1), false).value
          .set(ShareQuantityInTrustPage(1), "2000").value
          .set(ShareValueInTrustPage(1), num200).value

          .set(BusinessNamePage(0), businessName).value
          .set(BusinessDescriptionPage(0), "Business Description").value
          .set(BusinessAddressPage(0), address).value
          .set(BusinessValuePage(0), num101).value

          .set(PartnershipDescriptionPage(0), "Partnership Description").value
          .set(PartnershipStartDatePage(0), date).value

          .set(OtherAssetDescriptionPage(0), "Other Description").value
          .set(OtherAssetValuePage(0), num100).value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.assets.heading"))),
          AnswerSection(
            headingKey = Some("Money"),
            rows = Seq(
              AnswerRow(label = messages("asset.money.value.checkYourAnswersLabel"), answer = Html("£4000"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Property or land 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.propertyOrLand.addressYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.description.checkYourAnswersLabel"), answer = Html("Building land name"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.totalValue.checkYourAnswersLabel"), answer = Html("£2000"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), answer = Html("Yes"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Property or land 2"),
            rows = Seq(
              AnswerRow(label = messages("asset.propertyOrLand.addressYesNo.checkYourAnswersLabel"), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.addressUkYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages(
                "asset.propertyOrLand.address.checkYourAnswersLabel"
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.totalValue.checkYourAnswersLabel"), answer = Html("£2000"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.valueInTrust.checkYourAnswersLabel"), answer = Html("£1000"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Share 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.shares.inAPortfolioYesNo.checkYourAnswersLabel"), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.portfolioName.checkYourAnswersLabel"), answer = Html("Portfolio Name"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.portfolioOnStockExchangeYesNo.checkYourAnswersLabel"), answer = Html("Yes"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.portfolioQuantityInTrust.checkYourAnswersLabel"), answer = Html("1000"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.portfolioValueInTrust.checkYourAnswersLabel"), answer = Html("£100"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Share 2"),
            rows = Seq(
              AnswerRow(label = messages("asset.shares.inAPortfolioYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.companyName.checkYourAnswersLabel"), answer = Html(shareName), changeUrl = None),
              AnswerRow(label = messages("asset.shares.class.checkYourAnswersLabel", shareName), answer = Html("Preference"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.onStockExchangeYesNo.checkYourAnswersLabel", shareName), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.quantityInTrust.checkYourAnswersLabel", shareName), answer = Html("2000"), changeUrl = None),
              AnswerRow(label = messages("asset.shares.valueInTrust.checkYourAnswersLabel", shareName), answer = Html("£200"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Business 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.business.name.checkYourAnswersLabel"), answer = Html(businessName), changeUrl = None),
              AnswerRow(label = messages(
                "asset.business.description.checkYourAnswersLabel", businessName
              ), answer = Html("Business Description"), changeUrl = None),
              AnswerRow(label = messages(
                "asset.business.address.checkYourAnswersLabel", businessName
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("asset.business.value.checkYourAnswersLabel", businessName), answer = Html("£101"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Partnership 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.partnership.description.checkYourAnswersLabel"), answer = Html("Partnership Description"), changeUrl = None),
              AnswerRow(label = messages("asset.partnership.startDate.checkYourAnswersLabel"), answer = Html("1 June 2019"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Other 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.other.description.checkYourAnswersLabel"), answer = Html("Other Description"), changeUrl = None),
              AnswerRow(label = messages("asset.other.value.checkYourAnswersLabel", "Other Description"), answer = Html("£100"), changeUrl = None)
            ),
            sectionKey = None
          )
        )

      }

    }

    "generate Non-Eea Company section" when {

      val name = "NonEeaBusiness"

      "migrating from non-taxable to taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).value
          .set(NonEeaBusinessNamePage(0), name).value
          .set(NonEeaBusinessAddressPage(0), address).value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").value
          .set(NonEeaBusinessNamePage(1), name).value
          .set(NonEeaBusinessAddressPage(1), address).value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.assets.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages(
                "nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages(
                "nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }

      "not migrating from non-taxable to taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(NonEeaBusinessNamePage(0), name).value
          .set(NonEeaBusinessAddressPage(0), address).value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").value
          .set(NonEeaBusinessNamePage(1), name).value
          .set(NonEeaBusinessAddressPage(1), address).value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.nonEeaBusinesses.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages(
                "nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages(
                "nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name
              ), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }
    }

  }
}
