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

  "AllAssetsPrinter" must {

    "generate assets sections" when {

      val businessName = "Business Name"
      val shareName = "Share Name"

      "migrating from non-taxable to taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).success.value

          .set(MoneyValuePage(0), 4000L).success.value

          .set(PropertyOrLandAddressYesNoPage(0), false).success.value
          .set(PropertyOrLandDescriptionPage(0), "Building land name").success.value
          .set(PropertyOrLandTotalValuePage(0), 2000L).success.value
          .set(TrustOwnAllThePropertyOrLandPage(0), true).success.value

          .set(PropertyOrLandAddressYesNoPage(1), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(1), false).success.value
          .set(PropertyOrLandAddressPage(1), address).success.value
          .set(PropertyOrLandTotalValuePage(1), 2000L).success.value
          .set(TrustOwnAllThePropertyOrLandPage(1), false).success.value
          .set(PropertyLandValueTrustPage(1), 1000L).success.value

          .set(ShareNamePage(0), "Portfolio Name").success.value
          .set(SharesInAPortfolioPage(0),true).success.value
          .set(ShareOnStockExchangePage(0), true).success.value
          .set(ShareQuantityInTrustPage(0), "1000").success.value
          .set(ShareValueInTrustPage(0), 100L).success.value

          .set(ShareClassPage(1), ShareClass.Preference).success.value
          .set(ShareNamePage(1), shareName).success.value
          .set(SharesInAPortfolioPage(1),false).success.value
          .set(ShareOnStockExchangePage(1), false).success.value
          .set(ShareQuantityInTrustPage(1), "2000").success.value
          .set(ShareValueInTrustPage(1), 200L).success.value

          .set(BusinessNamePage(0), businessName).success.value
          .set(BusinessDescriptionPage(0), "Business Description").success.value
          .set(BusinessAddressPage(0), address).success.value
          .set(BusinessValuePage(0), 101L).success.value

          .set(PartnershipDescriptionPage(0), "Partnership Description").success.value
          .set(PartnershipStartDatePage(0), date).success.value

          .set(OtherAssetDescriptionPage(0), "Other Description").success.value
          .set(OtherAssetValuePage(0), 100L).success.value

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
              AnswerRow(label = messages("asset.propertyOrLand.address.checkYourAnswersLabel"), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
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
              AnswerRow(label = messages("asset.business.description.checkYourAnswersLabel", businessName), answer = Html("Business Description"), changeUrl = None),
              AnswerRow(label = messages("asset.business.address.checkYourAnswersLabel", businessName), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
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
          .set(WhatIsNextPage, NeedsToPayTax).success.value
          .set(NonEeaBusinessNamePage(0), name).success.value
          .set(NonEeaBusinessAddressPage(0), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").success.value
          .set(NonEeaBusinessNamePage(1), name).success.value
          .set(NonEeaBusinessAddressPage(1), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.assets.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }

      "not migrating from non-taxable to taxable" in {

        val answers = emptyUserAnswersForUtr
          .set(NonEeaBusinessNamePage(0), name).success.value
          .set(NonEeaBusinessAddressPage(0), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(0), "FR").success.value
          .set(NonEeaBusinessNamePage(1), name).success.value
          .set(NonEeaBusinessAddressPage(1), address).success.value
          .set(NonEeaBusinessGoverningCountryPage(1), "FR").success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.nonEeaBusinesses.heading"))),
          AnswerSection(
            headingKey = Some("Non-EEA Company 1"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Non-EEA Company 2"),
            rows = Seq(
              AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), answer = Html(name), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), answer = Html("Line 1<br />Line 2<br />Germany"), changeUrl = None),
              AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), answer = Html("France"), changeUrl = None)
            ),
            sectionKey = None
          )
        )
      }
    }

  }
}
