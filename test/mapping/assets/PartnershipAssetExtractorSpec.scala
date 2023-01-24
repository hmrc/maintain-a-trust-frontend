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

package mapping.assets

import java.time.LocalDate

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.partnership._

class PartnershipAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generatePartnership(index: Int) = DisplayTrustPartnershipType(
    utr = None,
    description = s"Partnership $index",
    partnershipStart = Some(LocalDate.parse("2019-11-26"))
  )

  val assetExtractor : PartnershipAssetExtractor =
    injector.instanceOf[PartnershipAssetExtractor]

  "Partnership Asset Extractor" - {

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

          val businessAssets = List(DisplayTrustPartnershipType(
            utr = None,
            description = "Partnership 1",
            partnershipStart = Some(LocalDate.parse("2019-11-26"))
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.right.value.get(PartnershipDescriptionPage(0)).get mustBe "Partnership 1"
          extraction.right.value.get(PartnershipStartDatePage(0)).get mustBe LocalDate.of(2019, 11, 26)
        }

        "with full data must return user answers updated" in {
          val businessAssets = (for (index <- 0 to 2) yield generatePartnership(index)).toList

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction mustBe 'right

          extraction.right.value.get(PartnershipDescriptionPage(0)).get mustBe "Partnership 0"
          extraction.right.value.get(PartnershipStartDatePage(0)).get mustBe LocalDate.of(2019, 11, 26)

          extraction.right.value.get(PartnershipDescriptionPage(1)).get mustBe "Partnership 1"
          extraction.right.value.get(PartnershipStartDatePage(1)).get mustBe LocalDate.of(2019, 11, 26)

          extraction.right.value.get(PartnershipDescriptionPage(2)).get mustBe "Partnership 2"
          extraction.right.value.get(PartnershipStartDatePage(2)).get mustBe LocalDate.of(2019, 11, 26)
        }
      }

    }

  }

}
