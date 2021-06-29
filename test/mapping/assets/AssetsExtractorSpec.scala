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

import java.time.LocalDate

import base.SpecBaseHelpers
import generators.Generators
import models.InternationalAddress
import models.http._
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.assets.business._
import pages.assets.nonEeaBusiness.{NonEeaBusinessAddressPage, NonEeaBusinessEndDatePage, NonEeaBusinessGoverningCountryPage, NonEeaBusinessLineNoPage, NonEeaBusinessNamePage, NonEeaBusinessStartDatePage}

class AssetsExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val assetExtractor : AssetsExtractor =
    injector.instanceOf[AssetsExtractor]

  "Asset Extractor" - {

    "when no assets" - {

      "must return user answers" in {

        val assets = DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, Some(assets))

        extraction mustBe 'right
        extraction.right.value.data mustBe ua.data

      }

    }

    "when there are assets of different type" - {

        "must return user answers updated" in {

          val assets = DisplayTrustAssets(
            monetary = Nil,
            propertyOrLand = Nil,
            shares = Nil,
            business = List(DisplayBusinessAssetType(
              orgName = "Business 1",
              utr = None,
              businessDescription = "Business Asset Description",
              address = Some(AddressType(s"line1", "line2", None, None, None, "FR")),
              businessValue = Some(101)
            )),
            partnerShip = Nil,
            other = Nil,
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

          extraction.right.value.get(BusinessNamePage(0)).get mustBe "Business 1"
          extraction.right.value.get(BusinessDescriptionPage(0)).get mustBe "Business Asset Description"
          extraction.right.value.get(BusinessAddressPage(0)).get mustBe InternationalAddress("line1", "line2", None, "FR")
          extraction.right.value.get(BusinessValuePage(0)).get mustBe 101

          extraction.right.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "1"
          extraction.right.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 1"
          extraction.right.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 1", "line2", None, "FR")
          extraction.right.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
          extraction.right.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(2019, 11, 26)
          extraction.right.value.get(NonEeaBusinessEndDatePage(0)) mustNot be(defined)

        }

    }

  }

}
