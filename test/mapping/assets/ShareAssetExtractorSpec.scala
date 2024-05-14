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
import models.pages.ShareClass._
import models.pages.ShareType._
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.shares._

class ShareAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (num100) = (100L)

  def generateAssets(index: Int): DisplaySharesType = DisplaySharesType(
    numberOfShares = None,
    orgName = "",
    utr = None,
    shareClassDisplay = Some(Ordinary),
    typeOfShare = Some(Quoted),
    value = Some(num100),
    isPortfolio = Some(true)
  )

  val assetExtractor : ShareAssetExtractor =
    injector.instanceOf[ShareAssetExtractor]

  "Share Asset Extractor" - {

    "when no assets" - {

      "must return user answers" in {

        val assets = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, assets)

        extraction mustBe Symbol("right")
        extraction.value.data mustBe ua.data

      }

    }

    "when there are assets" - {

      "for a taxable trust" - {

        "with minimum data for portfolio share asset must return user answers updated" in {

          val shareAssets = List(DisplaySharesType(
            numberOfShares = Some("1000"),
            orgName = "Portfolio Name",
            utr = None,
            shareClassDisplay = Some(Other),
            typeOfShare = Some(Quoted),
            value = Some(num100),
            isPortfolio = Some(true)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

          extraction.value.get(SharesInAPortfolioPage(0)).get mustBe true
          extraction.value.get(ShareNamePage(0)).get mustBe "Portfolio Name"
          extraction.value.get(ShareOnStockExchangePage(0)).get mustBe true
          extraction.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
          extraction.value.get(ShareValueInTrustPage(0)).get mustBe 100L
          extraction.value.get(ShareClassPage(0)) mustNot be(defined)
        }

        "with minimum data for non-portfolio share asset must return user answers updated" in {

          val shareAssets = List(DisplaySharesType(
            numberOfShares = Some("1000"),
            orgName = "Share Name",
            utr = None,
            shareClassDisplay = Some(Ordinary),
            typeOfShare = Some(Quoted),
            value = Some(num100),
            isPortfolio = Some(false)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

          extraction.value.get(SharesInAPortfolioPage(0)).get mustBe false
          extraction.value.get(ShareNamePage(0)).get mustBe "Share Name"
          extraction.value.get(ShareOnStockExchangePage(0)).get mustBe true
          extraction.value.get(ShareClassPage(0)).get mustBe Ordinary
          extraction.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
          extraction.value.get(ShareValueInTrustPage(0)).get mustBe 100L
        }


        "with portfolio share asset and non-portfolio share asset must return user answers updated" in {

          val shareAssets = List(DisplaySharesType(
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
          )

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

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
        }
      }
    }
  }
}
