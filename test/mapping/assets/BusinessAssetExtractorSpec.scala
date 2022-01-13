/*
 * Copyright 2022 HM Revenue & Customs
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
import models.InternationalAddress
import models.http._
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.assets.business._

class BusinessAssetExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateBusiness(index: Int) = DisplayBusinessAssetType(
    orgName = s"Business $index",
    utr = None,
    businessDescription = s"Business Asset Description $index",
    address = index match {
      case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "FR"))
      case 1 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
      case _ => Some(AddressType(s"line $index", "line2", None, None, None, "ES"))
    },
    businessValue = Some(101)
  )

  val assetExtractor : BusinessAssetExtractor =
    injector.instanceOf[BusinessAssetExtractor]

  "Business Asset Extractor" - {

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

        "with minimum data must return user answers updated" in {

          val businessAssets = List(DisplayBusinessAssetType(
            orgName = "Business 1",
            utr = None,
            businessDescription = "Business Asset Description",
            address = Some(AddressType(s"line1", "line2", None, None, None, "FR")),
            businessValue = Some(101)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.right.value.get(BusinessNamePage(0)).get mustBe "Business 1"
          extraction.right.value.get(BusinessDescriptionPage(0)).get mustBe "Business Asset Description"
          extraction.right.value.get(BusinessAddressPage(0)).get mustBe InternationalAddress("line1", "line2", None, "FR")
          extraction.right.value.get(BusinessValuePage(0)).get mustBe 101
        }

        "with full data must return user answers updated" in {
          val businessAssets = (for (index <- 0 to 2) yield generateBusiness(index)).toList

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction mustBe 'right

          extraction.right.value.get(BusinessNamePage(0)).get mustBe "Business 0"
          extraction.right.value.get(BusinessDescriptionPage(0)).get mustBe "Business Asset Description 0"
          extraction.right.value.get(BusinessAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "FR")
          extraction.right.value.get(BusinessValuePage(0)).get mustBe 101

          extraction.right.value.get(BusinessNamePage(1)).get mustBe "Business 1"
          extraction.right.value.get(BusinessDescriptionPage(1)).get mustBe "Business Asset Description 1"
          extraction.right.value.get(BusinessAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.right.value.get(BusinessValuePage(1)).get mustBe 101

          extraction.right.value.get(BusinessNamePage(2)).get mustBe "Business 2"
          extraction.right.value.get(BusinessDescriptionPage(2)).get mustBe "Business Asset Description 2"
          extraction.right.value.get(BusinessAddressPage(2)).get mustBe InternationalAddress("line 2", "line2", None, "ES")
          extraction.right.value.get(BusinessValuePage(2)).get mustBe 101
        }
      }

    }

  }

}
