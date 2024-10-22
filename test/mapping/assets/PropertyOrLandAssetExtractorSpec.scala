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
import models.{InternationalAddress, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.propertyOrLand._

class PropertyOrLandAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (num2000, num1000) = (2000L, 1000L)

  def generatePropertyOrLand(index: Int): PropertyLandType = PropertyLandType(
    buildingLandName = None,
    address = index match {
      case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "FR"))
      case 1 => Some(AddressType(s"line $index", "line2", None, None, Some("NE1 1AA"), "GB"))
      case _ => Some(AddressType(s"line $index", "line2", None, None, None, "ES"))
    },
    valueFull = num2000,
    valuePrevious = Some(num1000)
  )

  val assetExtractor : PropertyOrLandAssetExtractor =
    injector.instanceOf[PropertyOrLandAssetExtractor]

  "Property or Land Asset Extractor" - {

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

        "with minimum data (no address) must return user answers updated" in {

          val businessAssets = List(PropertyLandType(
            buildingLandName = Some(s"building land name 1"),
            address = None,
            valueFull = num2000,
            valuePrevious = None
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.value.get(PropertyOrLandDescriptionPage(0)).get mustBe "building land name 1"
          extraction.value.get(PropertyOrLandAddressYesNoPage(0)).get mustBe false
          extraction.value.get(PropertyOrLandAddressPage(0)) mustNot be(defined)
          extraction.value.get(PropertyOrLandTotalValuePage(0)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe true
          extraction.value.get(PropertyLandValueTrustPage(0)) mustNot be(defined)
        }

        "with international address must return user answers updated" in {

          val businessAssets = List(PropertyLandType(
            buildingLandName = None,
            address = Some(AddressType(s"line1", "line2", None, None, None, "FR")),
            valueFull = num2000,
            valuePrevious = Some(num1000)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.value.get(PropertyOrLandDescriptionPage(0)) mustNot be(defined)
          extraction.value.get(PropertyOrLandAddressYesNoPage(0)).get mustBe true
          extraction.value.get(PropertyOrLandAddressUkYesNoPage(0)).get mustBe false
          extraction.value.get(PropertyOrLandAddressPage(0)).get mustBe InternationalAddress("line1", "line2", None, "FR")
          extraction.value.get(PropertyOrLandTotalValuePage(0)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe false
          extraction.value.get(PropertyLandValueTrustPage(0)).get mustBe 1000L
        }

        "with uk address must return user answers updated" in {

          val businessAssets = List(PropertyLandType(
            buildingLandName = None,
            address = Some(AddressType(s"line1", "line2", None, None, Some("NE1 1AA"), "GB")),
            valueFull = num2000,
            valuePrevious = Some(num1000)
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

          extraction.value.get(PropertyOrLandDescriptionPage(0)) mustNot be(defined)
          extraction.value.get(PropertyOrLandAddressYesNoPage(0)).get mustBe true
          extraction.value.get(PropertyOrLandAddressUkYesNoPage(0)).get mustBe true
          extraction.value.get(PropertyOrLandAddressPage(0)).get mustBe UKAddress("line1", "line2", None, None, "NE1 1AA")
          extraction.value.get(PropertyOrLandTotalValuePage(0)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe false
          extraction.value.get(PropertyLandValueTrustPage(0)).get mustBe 1000L
        }

        "with uk address property and business name property must return user answers updated" in {

          val businessAssets = List(PropertyLandType(
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
          )

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, businessAssets)

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

        }

        "with full data must return user answers updated" in {
          val propertyOrLandAssets = (for (index <- 0 to 2) yield generatePropertyOrLand(index)).toList

          val ua = emptyUserAnswersForUtr

          val extraction = assetExtractor.extract(ua, propertyOrLandAssets)

          extraction mustBe Symbol("right")

          extraction.value.get(PropertyOrLandDescriptionPage(0)) mustNot be(defined)
          extraction.value.get(PropertyOrLandAddressYesNoPage(0)).get mustBe true
          extraction.value.get(PropertyOrLandAddressUkYesNoPage(0)).get mustBe false
          extraction.value.get(PropertyOrLandAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "FR")
          extraction.value.get(PropertyOrLandTotalValuePage(0)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe false
          extraction.value.get(PropertyLandValueTrustPage(0)).get mustBe 1000L

          extraction.value.get(PropertyOrLandDescriptionPage(1)) mustNot be(defined)
          extraction.value.get(PropertyOrLandAddressYesNoPage(1)).get mustBe true
          extraction.value.get(PropertyOrLandAddressUkYesNoPage(1)).get mustBe true
          extraction.value.get(PropertyOrLandAddressPage(1)).get mustBe UKAddress("line 1", "line2", None, None, "NE1 1AA")
          extraction.value.get(PropertyOrLandTotalValuePage(1)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(1)).get mustBe false
          extraction.value.get(PropertyLandValueTrustPage(1)).get mustBe 1000L

          extraction.value.get(PropertyOrLandDescriptionPage(2)) mustNot be(defined)
          extraction.value.get(PropertyOrLandAddressYesNoPage(2)).get mustBe true
          extraction.value.get(PropertyOrLandAddressUkYesNoPage(2)).get mustBe false
          extraction.value.get(PropertyOrLandAddressPage(2)).get mustBe InternationalAddress("line 2", "line2", None, "ES")
          extraction.value.get(PropertyOrLandTotalValuePage(2)).get mustBe 2000L
          extraction.value.get(TrustOwnAllThePropertyOrLandPage(1)).get mustBe false
          extraction.value.get(PropertyLandValueTrustPage(2)).get mustBe 1000L

        }

        ".updateUserAnswers" - {

          val partOwnedProperty = PropertyLandType(
            buildingLandName = None,
            address = Some(AddressType(s"line1", "line2", None, None, None, "FR")),
            valueFull = num2000,
            valuePrevious = Some(num1000)
          )

          val fullyOwnedProperty = partOwnedProperty.copy(valuePrevious = None)

          val emptyUserAnswers = Right(emptyUserAnswersForUtr)

          "sets part ownership correctly" in {
            val userAnswers = assetExtractor.updateUserAnswers(emptyUserAnswers, partOwnedProperty, 0).value

            userAnswers.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe false
            userAnswers.get(PropertyLandValueTrustPage(0)).get mustBe num1000
            userAnswers.get(PropertyOrLandTotalValuePage(0)).get mustBe num2000
          }

          "sets full ownership correctly" in {
            val userAnswers = assetExtractor.updateUserAnswers(emptyUserAnswers, fullyOwnedProperty, 0).value

            userAnswers.get(TrustOwnAllThePropertyOrLandPage(0)).get mustBe true
            userAnswers.get(PropertyLandValueTrustPage(0)) mustBe None
            userAnswers.get(PropertyOrLandTotalValuePage(0)).get mustBe num2000
          }
        }
      }

    }

  }

}
