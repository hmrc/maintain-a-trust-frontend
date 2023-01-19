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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustOtherType}
import models.{InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.beneficiaries.other._
import utils.Constants.GB

class OtherBeneficiaryExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateOther(index: Int) = DisplayTrustOtherType(
    lineNo = Some(s"$index"),
    bpMatchStatus = index match {
      case 0 => Some("01")
      case _ => None
    },
    description = s"Other $index",
    beneficiaryDiscretion = index match {
      case 0 => Some(false)
      case _ => None
    },
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    beneficiaryShareOfIncome = index match {
      case 0 => Some("98")
      case _ => None
    },
    address = index match {
      case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
      case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
      case _ => None
    },
    entityStart = "2019-11-26"
  )

  val otherExtractor : OtherBeneficiaryExtractor =
    injector.instanceOf[OtherBeneficiaryExtractor]

  "Other Beneficiary Extractor" - {

    "when no others" - {

      "must return user answers" in {

        val others = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = otherExtractor.extract(ua, others)

        extraction mustBe 'left

      }

    }

    "when there are others" - {

      "for a 4mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val other = List(DisplayTrustOtherType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            description = s"Other 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = Some("FR"),
            address = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = otherExtractor.extract(ua, other)

          extraction.right.value.get(OtherBeneficiaryDescriptionPage(0)).get mustBe "Other 1"
          extraction.right.value.get(OtherBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(0)) mustNot be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val other = List(DisplayTrustOtherType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            description = s"Other 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            address = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = otherExtractor.extract(ua, other)

          extraction.right.value.get(OtherBeneficiaryDescriptionPage(0)).get mustBe "Other 1"
          extraction.right.value.get(OtherBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val others = (for (index <- 0 to 2) yield generateOther(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = otherExtractor.extract(ua, others)

          extraction mustBe 'right

          extraction.right.value.get(OtherBeneficiaryDescriptionPage(0)).get mustBe "Other 0"
          extraction.right.value.get(OtherBeneficiaryDescriptionPage(1)).get mustBe "Other 1"
          extraction.right.value.get(OtherBeneficiaryDescriptionPage(2)).get mustBe "Other 2"

          extraction.right.value.get(OtherBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("98"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("98"), "2019-11-26")

          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(1)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(1)).get mustBe true

          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(0)).get mustBe "98"
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(1)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(2)).get mustBe true

          extraction.right.value.get(OtherBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(OtherBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")

          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(2)).get mustBe true
        }

      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val other = List(DisplayTrustOtherType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            description = s"Other 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            address = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = otherExtractor.extract(ua, other)

          extraction.right.value.get(OtherBeneficiaryDescriptionPage(0)).get mustBe "Other 1"
          extraction.right.value.get(OtherBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val others = (for (index <- 0 to 2) yield generateOther(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = otherExtractor.extract(ua, others)

          extraction mustBe 'right

          extraction.right.value.get(OtherBeneficiaryDescriptionPage(0)).get mustBe "Other 0"
          extraction.right.value.get(OtherBeneficiaryDescriptionPage(1)).get mustBe "Other 1"
          extraction.right.value.get(OtherBeneficiaryDescriptionPage(2)).get mustBe "Other 2"

          extraction.right.value.get(OtherBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("98"), "2019-11-26")
          extraction.right.value.get(OtherBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("98"), "2019-11-26")

          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryShareOfIncomePage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(OtherBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(OtherBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressYesNoPage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressPage(2)) mustNot be(defined)

          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(OtherBeneficiaryAddressUKYesNoPage(2)) mustNot be(defined)
        }

      }
    }

  }

}
