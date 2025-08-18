/*
 * Copyright 2025 HM Revenue & Customs
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
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.nonEeaBusiness._

import java.time.Month.{FEBRUARY, JANUARY, NOVEMBER}

class NonEeaBusinessAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (year2019, year2020, num26) = (2019, 2020, 26)

  def generateNonEeaBusiness(index: Int): DisplayNonEEABusinessType = DisplayNonEEABusinessType(
    lineNo = Some(s"$index"),
    orgName = s"Non EEA Business $index",
    govLawCountry = index match {
      case 0 => "FR"
      case 1 => "DE"
      case _ => "ES"
    },
    address = index match {
      case 0 => AddressType(s"line $index", "line2", None, None, None, "FR")
      case 1 => AddressType(s"line $index", "line2", None, None, None, "DE")
      case _ => AddressType(s"line $index", "line2", None, None, None, "ES")
    },
    startDate = LocalDate.parse("2019-11-26"),
    endDate = index match {
      case 0 => Some(LocalDate.parse("2020-01-01"))
      case 1 => Some(LocalDate.parse("2020-02-01"))
      case _ => None
    }
  )

  val assetExtractor : NonEeaBusinessAssetExtractor =
    injector.instanceOf[NonEeaBusinessAssetExtractor]

  "None-EEA business asset Extractor" - {

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
          val nonEeaBusinessAssets = List(DisplayNonEEABusinessType(
            lineNo = Some("1"),
            orgName = s"Non EEA Business 1",
            address = AddressType("line 1", "line2", None, None, None, "FR"),
            govLawCountry = "FR",
            startDate = LocalDate.parse("2019-11-26"),
            endDate = None
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, nonEeaBusinessAssets)

          extraction.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "1"
          extraction.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 1"
          extraction.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 1", "line2", None, "FR")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
          extraction.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val nonEeaBusinessAssets = (for (index <- 0 to 2) yield generateNonEeaBusiness(index)).toList

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, nonEeaBusinessAssets)

          extraction mustBe Symbol("right")

          extraction.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "0"
          extraction.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 0"
          extraction.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "FR")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
          extraction.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(0)).get mustBe LocalDate.of(year2020, JANUARY, 1)

          extraction.value.get(NonEeaBusinessLineNoPage(1)).get mustBe "1"
          extraction.value.get(NonEeaBusinessNamePage(1)).get mustBe "Non EEA Business 1"
          extraction.value.get(NonEeaBusinessAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(1)).get mustBe "DE"
          extraction.value.get(NonEeaBusinessStartDatePage(1)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(1)).get mustBe LocalDate.of(year2020, FEBRUARY, 1)

          extraction.value.get(NonEeaBusinessLineNoPage(2)).get mustBe "2"
          extraction.value.get(NonEeaBusinessNamePage(2)).get mustBe "Non EEA Business 2"
          extraction.value.get(NonEeaBusinessAddressPage(2)).get mustBe InternationalAddress("line 2", "line2", None, "ES")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(2)).get mustBe "ES"
          extraction.value.get(NonEeaBusinessStartDatePage(2)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(2)) mustNot be(defined)
        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val nonEeaBusinessAssets = List(DisplayNonEEABusinessType(
            lineNo = Some("1"),
            orgName = s"Non EEA Business 1",
            address = AddressType("line 1", "line2", None, None, None, "FR"),
            govLawCountry = "FR",
            startDate = LocalDate.parse("2019-11-26"),
            endDate = None
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, nonEeaBusinessAssets)

          extraction.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "1"
          extraction.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 1"
          extraction.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 1", "line2", None, "FR")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
          extraction.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val nonEeaBusinessAssets = (for (index <- 0 to 2) yield generateNonEeaBusiness(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = assetExtractor.extract(ua, nonEeaBusinessAssets)

          extraction mustBe Symbol("right")

          extraction.value.get(NonEeaBusinessLineNoPage(0)).get mustBe "0"
          extraction.value.get(NonEeaBusinessNamePage(0)).get mustBe "Non EEA Business 0"
          extraction.value.get(NonEeaBusinessAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "FR")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(0)).get mustBe "FR"
          extraction.value.get(NonEeaBusinessStartDatePage(0)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(0)).get mustBe LocalDate.of(year2020, JANUARY, 1)

          extraction.value.get(NonEeaBusinessLineNoPage(1)).get mustBe "1"
          extraction.value.get(NonEeaBusinessNamePage(1)).get mustBe "Non EEA Business 1"
          extraction.value.get(NonEeaBusinessAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(1)).get mustBe "DE"
          extraction.value.get(NonEeaBusinessStartDatePage(1)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(1)).get mustBe LocalDate.of(year2020, FEBRUARY, 1)

          extraction.value.get(NonEeaBusinessLineNoPage(2)).get mustBe "2"
          extraction.value.get(NonEeaBusinessNamePage(2)).get mustBe "Non EEA Business 2"
          extraction.value.get(NonEeaBusinessAddressPage(2)).get mustBe InternationalAddress("line 2", "line2", None, "ES")
          extraction.value.get(NonEeaBusinessGoverningCountryPage(2)).get mustBe "ES"
          extraction.value.get(NonEeaBusinessStartDatePage(2)).get mustBe LocalDate.of(year2019, NOVEMBER, num26)
          extraction.value.get(NonEeaBusinessEndDatePage(2)) mustNot be(defined)
        }
      }
    }

  }

}
