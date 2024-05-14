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

package mapping.assets

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import models.pages.ShareClass.{Ordinary, Other}
import models.pages.ShareType.Quoted
import models.{InternationalAddress, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.business._
import pages.assets.money.MoneyValuePage
import pages.assets.nonEeaBusiness._
import pages.assets.other._
import pages.assets.partnership.{PartnershipDescriptionPage, PartnershipStartDatePage}
import pages.assets.propertyOrLand._
import pages.assets.shares._
import java.time.Month.NOVEMBER

import java.time.LocalDate

class AssetsExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  val assetExtractor: AssetsExtractor = injector.instanceOf[AssetsExtractor]

  private val (num26, num100, num101, num1000, num2000, num4000, year2019) = (26, 100L, 101, 1000L, 2000L, 4000L, 2019)

  "Asset Extractor" - {

    "when no assets" - {
      "must return user answers" in {

        val assets = DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, Some(assets))

        extraction mustBe Symbol("right")
        extraction.value.data mustBe ua.data

      }
    }

    "when there are assets of different type" - {
      "must return user answers updated" in {

        val assets = DisplayTrustAssets(
          monetary = List(AssetMonetaryAmount(
            assetMonetaryAmount = num4000
          )),
          propertyOrLand = List(PropertyLandType(
            buildingLandName = None,
            address = Some(AddressType(s"line1", "line2", None, None, Some("NE1 1AA"), "GB")),
            valueFull = num2000,
            valuePrevious = Some(num1000)
          ),
            PropertyLandType(
              buildingLandName = Some(s"building land name 1"),
              address = None,
              valueFull = num2000,
              valuePrevious = None
            )
          ),
          shares = List(DisplaySharesType(
            numberOfShares = Some("1000"),
            orgName = "Portfolio Name",
            utr = None,
            shareClassDisplay = Some(Other),
            typeOfShare = Some(Quoted),
            value = Some(num100),
            isPortfolio = Some(true)
          ),
            DisplaySharesType(
              numberOfShares = Some("1000"),
              orgName = "Share Name",
              utr = None,
              shareClassDisplay = Some(Ordinary),
              typeOfShare = Some(Quoted),
              value = Some(num100),
              isPortfolio = Some(false)
            )
          ),
          business = List(DisplayBusinessAssetType(
            orgName = "Business 1",
            utr = None,
            businessDescription = "Business Asset Description",
            address = Some(AddressType(s"line1", "line2", None, None, None, "FR")),
            businessValue = Some(num101)
          )),
          partnerShip = List(DisplayTrustPartnershipType(
            utr = None,
            description = "Partnership 1",
            partnershipStart = Some(LocalDate.parse("2019-11-26"))
          )),
          other = List(DisplayOtherAssetType(
            description = "Other 1",
            value = Some(num100)
          )),
          nonEEABusiness = List(DisplayNonEEABusinessType(
            lineNo = Some("1"),
            orgName = s"Non EEA Business 1",
            address = AddressType("line 1", "line2", None, None, None, "FR"),
            govLawCountry = "FR",
            startDate = LocalDate.parse("2019-11-26"),
            endDate = None
          ))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, Some(assets))

        extraction.value.get(MoneyValuePage(0)).get mustBe 4000L

        extraction.value.get(PropertyOrLandDescriptionPage(0)) mustNot be(defined)
        extraction.value.get(PropertyOrLandAddressYesNoPage(0)).get mustBe true
        extraction.value.get(PropertyOrLandAddressUkYesNoPage(0)).get mustBe true
        extraction.value.get(PropertyOrLandAddressPage(0)).get mustBe UKAddress("line1", "line2", None, None, "NE1 1AA")
        extraction.value.get(PropertyOrLandTotalValuePage(0)).get mustBe 2000L
        extraction.value.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe false
        extraction.value.get(PropertyLandValueTrustPage(0)).get mustBe 1000L

        extraction.value.get(PropertyOrLandDescriptionPage(1)).get mustBe "building land name 1"
        extraction.value.get(PropertyOrLandAddressYesNoPage(1)).get mustBe false
        extraction.value.get(PropertyOrLandAddressPage(1)) mustNot be(defined)
        extraction.value.get(PropertyOrLandTotalValuePage(1)).get mustBe 2000L
        extraction.value.get(TrustOwnAllThePropertyOrLandPage(1)).get mustBe true
        extraction.value.get(PropertyLandValueTrustPage(1)) mustNot be(defined)

        extraction.value.get(SharesInAPortfolioPage(0)).get mustBe true
        extraction.value.get(ShareNamePage(0)).get mustBe "Portfolio Name"
        extraction.value.get(ShareOnStockExchangePage(0)).get mustBe true
        extraction.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
        extraction.value.get(ShareValueInTrustPage(0)).get mustBe 100L
        extraction.value.get(ShareClassPage(0)) mustNot be(defined)

        extraction.value.get(SharesInAPortfolioPage(1)).get mustBe false
        extraction.value.get(ShareNamePage(1)).get mustBe "Share Name"
        extraction.value.get(ShareOnStockExchangePage(1)).get mustBe true
        extraction.value.get(ShareClassPage(1)).get mustBe Ordinary
        extraction.value.get(ShareQuantityInTrustPage(1)).get mustBe "1000"
        extraction.value.get(ShareValueInTrustPage(1)).get mustBe 100L

        extraction.value.get(BusinessNamePage(0)).get mustBe "Business 1"
        extraction.value.get(BusinessDescriptionPage(0)).get mustBe "Business Asset Description"
        extraction.value.get(BusinessAddressPage(0)).get mustBe InternationalAddress("line1", "line2", None, "FR")
        extraction.value.get(BusinessValuePage(0)).get mustBe 101

        extraction.value.get(PartnershipDescriptionPage(0)).get mustBe "Partnership 1"
        extraction.value.get(PartnershipStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)

        extraction.value.get(OtherAssetDescriptionPage(0)).get mustBe "Other 1"
        extraction.value.get(OtherAssetValuePage(0)).get mustBe 100

        extraction.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "1"
        extraction.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 1"
        extraction.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 1", "line2", None, "FR")
        extraction.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
        extraction.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
        extraction.value.get(NonEeaBusinessEndDatePage(0)) mustNot be(defined)

      }
    }
  }
}
