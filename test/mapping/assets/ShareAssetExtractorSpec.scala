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

package mapping.assets

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import models.pages.ShareClass._
import models.pages.ShareType._
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.assets.shares._

class ShareAssetExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateAssets(index: Int): DisplaySharesType = DisplaySharesType(
    numberOfShares = None,
    orgName = "",
    utr = None,
    shareClassDisplay = Some(Ordinary),
    typeOfShare = Some(Quoted),
    value = Some(100L),
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

        extraction mustBe 'right
        extraction.right.value.data mustBe ua.data

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
            value = Some(100L),
            isPortfolio = Some(true)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

          extraction.right.value.get(SharesInAPortfolioPage(0)).get mustBe true
          extraction.right.value.get(ShareNamePage(0)).get mustBe "Portfolio Name"
          extraction.right.value.get(ShareOnStockExchangePage(0)).get mustBe true
          extraction.right.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
          extraction.right.value.get(ShareValueInTrustPage(0)).get mustBe 100L
          extraction.right.value.get(ShareClassPage(0)) mustNot be(defined)
        }

        "with minimum data for non-portfolio share asset must return user answers updated" in {

          val shareAssets = List(DisplaySharesType(
            numberOfShares = Some("1000"),
            orgName = "Share Name",
            utr = None,
            shareClassDisplay = Some(Ordinary),
            typeOfShare = Some(Quoted),
            value = Some(100L),
            isPortfolio = Some(false)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

          extraction.right.value.get(SharesInAPortfolioPage(0)).get mustBe false
          extraction.right.value.get(ShareNamePage(0)).get mustBe "Share Name"
          extraction.right.value.get(ShareOnStockExchangePage(0)).get mustBe true
          extraction.right.value.get(ShareClassPage(0)).get mustBe Ordinary
          extraction.right.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
          extraction.right.value.get(ShareValueInTrustPage(0)).get mustBe 100L
        }


        "with portfolio share asset and non-portfolio share asset must return user answers updated" in {

          val shareAssets = List(DisplaySharesType(
            numberOfShares = Some("1000"),
            orgName = "Portfolio Name",
            utr = None,
            shareClassDisplay = Some(Other),
            typeOfShare = Some(Quoted),
            value = Some(100L),
            isPortfolio = Some(true)
          ),
            DisplaySharesType(
              numberOfShares = Some("1000"),
              orgName = "Share Name",
              utr = None,
              shareClassDisplay = Some(Ordinary),
              typeOfShare = Some(Quoted),
              value = Some(100L),
              isPortfolio = Some(false)
            )
          )

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, shareAssets)

          extraction.right.value.get(SharesInAPortfolioPage(0)).get mustBe true
          extraction.right.value.get(ShareNamePage(0)).get mustBe "Portfolio Name"
          extraction.right.value.get(ShareOnStockExchangePage(0)).get mustBe true
          extraction.right.value.get(ShareQuantityInTrustPage(0)).get mustBe "1000"
          extraction.right.value.get(ShareValueInTrustPage(0)).get mustBe 100L
          extraction.right.value.get(ShareClassPage(0)) mustNot be(defined)

          extraction.right.value.get(SharesInAPortfolioPage(1)).get mustBe false
          extraction.right.value.get(ShareNamePage(1)).get mustBe "Share Name"
          extraction.right.value.get(ShareOnStockExchangePage(1)).get mustBe true
          extraction.right.value.get(ShareClassPage(1)).get mustBe Ordinary
          extraction.right.value.get(ShareQuantityInTrustPage(1)).get mustBe "1000"
          extraction.right.value.get(ShareValueInTrustPage(1)).get mustBe 100L

        }


      }

    }

  }

}
