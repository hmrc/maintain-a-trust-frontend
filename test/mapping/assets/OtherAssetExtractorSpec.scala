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
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.other._

class OtherAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val num100 = 100
  def generateOther(index: Int) = DisplayOtherAssetType(
    description = s"Other $index",
    value = Some(num100)
  )

  val assetExtractor : OtherAssetExtractor =
    injector.instanceOf[OtherAssetExtractor]

  "Other Asset Extractor" - {

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

        "with minimum data must return user answers updated" in {

          val businessAssets = List(DisplayOtherAssetType(
            description = "Other 1",
            value = Some(num100.toLong)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.value.get(OtherAssetDescriptionPage(0)).get mustBe "Other 1"
          extraction.value.get(OtherAssetValuePage(0)).get mustBe 100
        }

        "with full data must return user answers updated" in {
          val otherAssets = (for (index <- 0 to 2) yield generateOther(index)).toList

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, otherAssets)

          extraction mustBe Symbol("right")

          extraction.value.get(OtherAssetDescriptionPage(0)).get mustBe "Other 0"
          extraction.value.get(OtherAssetValuePage(0)).get mustBe 100

          extraction.value.get(OtherAssetDescriptionPage(1)).get mustBe "Other 1"
          extraction.value.get(OtherAssetValuePage(1)).get mustBe 100

          extraction.value.get(OtherAssetDescriptionPage(2)).get mustBe "Other 2"
          extraction.value.get(OtherAssetValuePage(2)).get mustBe 100
        }
      }

    }

  }

}
