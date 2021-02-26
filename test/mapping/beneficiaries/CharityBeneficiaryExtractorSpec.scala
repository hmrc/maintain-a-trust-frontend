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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import mapping.PlaybackExtractor
import models.http.{AddressType, DisplayTrustCharityType, DisplayTrustIdentificationOrgType}
import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.beneficiaries.charity._
import utils.Constants.GB

class CharityBeneficiaryExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateCharity(index: Int, isTaxable: Boolean) = DisplayTrustCharityType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    organisationName = s"Charity $index",
    beneficiaryDiscretion = index match {
      case 0 => Some(false)
      case _ => None
    },
    beneficiaryShareOfIncome = index match {
      case 0 => Some("98")
      case _ => None
    },
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    identification = Some(
      DisplayTrustIdentificationOrgType(
        safeId = Some("8947584-94759745-84758745"),
        utr = index match {
          case 1 => Some(s"${index}234567890")
          case _ => None
        },
        address = index match {
          case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )

  val charityExtractor: PlaybackExtractor[Option[List[DisplayTrustCharityType]]] =
    injector.instanceOf[CharityBeneficiaryExtractor]

  "Charity Beneficiary Extractor" - {

    "when no charities" - {

      "must return user answers" in {

        val charities = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = charityExtractor.extract(ua, charities)

        extraction mustBe 'left

      }

    }

    "when there are charities" - {

      "for a taxable trust" - {

        "with minimum data must return user answers updated" in {
          val charity = List(DisplayTrustCharityType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Charity 1",
            beneficiaryDiscretion = None,
            countryOfResidence = None,
            beneficiaryShareOfIncome = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr")

          val extraction = charityExtractor.extract(ua, Some(charity))

          extraction.right.value.get(CharityBeneficiaryNamePage(0)).get mustBe "Charity 1"
          extraction.right.value.get(CharityBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val charities = (for (index <- 0 to 2) yield generateCharity(index, isTaxable = true)).toList

          val ua = UserAnswers("fakeId", "utr")

          val extraction = charityExtractor.extract(ua, Some(charities))

          extraction mustBe 'right

          extraction.right.value.get(CharityBeneficiaryNamePage(0)).get mustBe "Charity 0"
          extraction.right.value.get(CharityBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(0)).get mustBe "98"
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(CharityBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(0)).get mustBe false

          extraction.right.value.get(CharityBeneficiaryNamePage(1)).get mustBe "Charity 1"
          extraction.right.value.get(CharityBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(1)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(CharityBeneficiaryUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(CharityBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(1)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.right.value.get(CharityBeneficiaryNamePage(2)).get mustBe "Charity 2"
          extraction.right.value.get(CharityBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(2)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(2)).get mustBe true
        }
      }

      "for a non  taxable trust" - {

        "with minimum data must return user answers updated" in {
          val charity = List(DisplayTrustCharityType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Charity 1",
            beneficiaryDiscretion = None,
            countryOfResidence = None,
            beneficiaryShareOfIncome = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false)

          val extraction = charityExtractor.extract(ua, Some(charity))

          extraction.right.value.get(CharityBeneficiaryNamePage(0)).get mustBe "Charity 1"
          extraction.right.value.get(CharityBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val charities = (for (index <- 0 to 2) yield generateCharity(index, isTaxable = false)).toList

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false)

          val extraction = charityExtractor.extract(ua, Some(charities))

          extraction mustBe 'right

          extraction.right.value.get(CharityBeneficiaryNamePage(0)).get mustBe "Charity 0"
          extraction.right.value.get(CharityBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(CharityBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)

          extraction.right.value.get(CharityBeneficiaryNamePage(1)).get mustBe "Charity 1"
          extraction.right.value.get(CharityBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(CharityBeneficiaryUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.right.value.get(CharityBeneficiaryNamePage(2)).get mustBe "Charity 2"
          extraction.right.value.get(CharityBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(CharityBeneficiaryDiscretionYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(CharityBeneficiaryAddressYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(CharityBeneficiaryAddressUKYesNoPage(2)) mustNot be(defined)
        }
      }
    }

  }

}
