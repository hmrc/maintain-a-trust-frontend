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
import models.InternationalAddress
import models.pages.WhatIsNext.NeedsToPayTax
import pages.WhatIsNextPage
import pages.assets.business._
import pages.assets.nonEeaBusiness._
import pages.assets.propertyOrLand._
import play.twirl.api.Html
import utils.print.sections.assets.AllAssetsPrinter
import viewmodels.{AnswerRow, AnswerSection}

class AllAssetsPrinterSpec extends SpecBase {

  private val helper: AllAssetsPrinter = injector.instanceOf[AllAssetsPrinter]
  private val name = "NonEeaBusiness"
  private val address = InternationalAddress("Line 1", "Line 2", None, "DE")

  "AllAssetsPrinter" must {

    "generate assets assets sections" when {

      "migrating from non-taxable to taxable" in {

        val businessName = "Business Name"

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, NeedsToPayTax).success.value
          .set(BusinessNamePage(0), businessName).success.value
          .set(BusinessDescriptionPage(0), "Business Description").success.value
          .set(BusinessAddressPage(0), InternationalAddress("line1", "line2", None, "FR")).success.value
          .set(BusinessValuePage(0), 101L).success.value

          .set(PropertyOrLandAddressYesNoPage(0), false).success.value
          .set(PropertyOrLandDescriptionPage(0), "Building land name").success.value
          .set(PropertyOrLandTotalValuePage(0), 2000L).success.value
          .set(TrustOwnAllThePropertyOrLandPage(0), true).success.value

          // TODO - Do we need this
//          .set(PropertyOrLandAddressUkYesNoPage(0), None).success.value
//          .set(PropertyOrLandAddressPage(0), None).success.value
//          .set(PropertyLandValueTrustPage(0), None).success.value

          .set(PropertyOrLandAddressYesNoPage(1), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(1), false).success.value
          .set(PropertyOrLandAddressPage(1), InternationalAddress("line1", "line2", None, "FR")).success.value
          .set(PropertyOrLandTotalValuePage(1), 2000L).success.value
          .set(TrustOwnAllThePropertyOrLandPage(1), false).success.value
          .set(PropertyLandValueTrustPage(1), 1000L).success.value

          // TODO - Do we need this
//          .set(PropertyOrLandDescriptionPage(1), None).success.value

        val result = helper.entities(answers)

        result mustBe Seq(
          AnswerSection(None, Nil, Some(messages("answerPage.section.assets.heading"))),
          AnswerSection(
            headingKey = Some("Business 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.business.name.checkYourAnswersLabel"), answer = Html(businessName), changeUrl = None),
              AnswerRow(label = messages("asset.business.description.checkYourAnswersLabel", businessName), answer = Html("Business Description"), changeUrl = None),
              AnswerRow(label = messages("asset.business.address.checkYourAnswersLabel", businessName), answer = Html("line1<br />line2<br />France"), changeUrl = None),
              AnswerRow(label = messages("asset.business.value.checkYourAnswersLabel", businessName), answer = Html("£101"), changeUrl = None)
            ),
            sectionKey = None
          ),
          AnswerSection(
            headingKey = Some("Property or land 1"),
            rows = Seq(
              AnswerRow(label = messages("asset.propertyOrLand.description.checkYourAnswersLabel"), answer = Html("Building land name"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.addressYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
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
              AnswerRow(label = messages("asset.propertyOrLand.address.checkYourAnswersLabel"), answer = Html("line1<br />line2<br />France"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.totalValue.checkYourAnswersLabel"), answer = Html("£2000"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), answer = Html("No"), changeUrl = None),
              AnswerRow(label = messages("asset.propertyOrLand.valueInTrust.checkYourAnswersLabel"), answer = Html("£1000"), changeUrl = None)
            ),
            sectionKey = None
          )
        )

      }

    }

    "generate Non-Eea Company section" when {

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
