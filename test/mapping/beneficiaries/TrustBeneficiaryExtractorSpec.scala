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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustBeneficiaryTrustType, DisplayTrustIdentificationOrgType}
import models.{InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.beneficiaries.trust._
import utils.Constants.{DE, GB}

class TrustBeneficiaryExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateTrust(index: Int) = DisplayTrustBeneficiaryTrustType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    organisationName = s"Trust $index",
    beneficiaryDiscretion = index match {
      case 0 => Some(false)
      case _ => None
    },
    beneficiaryShareOfIncome = index match {
      case 0 => Some("98")
      case _ => None
    },
    countryOfResidence = populateCountry(index),
    identification = Some(
      DisplayTrustIdentificationOrgType(
        safeId = Some("8947584-94759745-84758745"),
        utr = index match {
          case 1 => Some(s"${index}234567890")
          case _ => None
        },
        address = index match {
          case 0 => Some(AddressType(s"line $index", "line2", None, None, None, DE))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )

  private def populateCountry(index: Int): Option[String] =
    index match {
      case 0 => Some(GB)
      case 1 => Some(DE)
      case _ => None
    }

  val trustExtractor : TrustBeneficiaryExtractor =
    injector.instanceOf[TrustBeneficiaryExtractor]

  "Trust Beneficiary Extractor" - {

    "when no trusts" - {

      "must return user answers" in {

        val trusts = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = trustExtractor.extract(ua, trusts)

        extraction mustBe Symbol("left")

      }

    }

    "when there are trusts" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val trust = List(DisplayTrustBeneficiaryTrustType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Trust 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = Some("FR"),
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = trustExtractor.extract(ua, trust)

          extraction.value.get(TrustBeneficiaryNamePage(0)).get mustBe "Trust 1"
          extraction.value.get(TrustBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiarySafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(0)) mustNot be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trust = List(DisplayTrustBeneficiaryTrustType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Trust 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trustExtractor.extract(ua, trust)

          extraction.value.get(TrustBeneficiaryNamePage(0)).get mustBe "Trust 1"
          extraction.value.get(TrustBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiarySafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val trusts = (for (index <- 0 to 2) yield generateTrust(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trustExtractor.extract(ua, trusts)

          extraction mustBe Symbol("right")

          extraction.value.get(TrustBeneficiaryNamePage(0)).get mustBe "Trust 0"
          extraction.value.get(TrustBeneficiaryNamePage(1)).get mustBe "Trust 1"
          extraction.value.get(TrustBeneficiaryNamePage(2)).get mustBe "Trust 2"

          extraction.value.get(TrustBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")

          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(1)).get mustBe true
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(2)).get mustBe true

          extraction.value.get(TrustBeneficiaryShareOfIncomePage(0)).get mustBe "98"
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(1)).get mustBe "1234567890"
          extraction.value.get(TrustBeneficiaryUtrPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(TrustBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(TrustBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrustBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(1)).get mustBe false
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(2)).get mustBe true

          extraction.value.get(TrustBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.value.get(TrustBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")

          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(2)).get mustBe true
        }

      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trust = List(DisplayTrustBeneficiaryTrustType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Trust 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = trustExtractor.extract(ua, trust)

          extraction.value.get(TrustBeneficiaryNamePage(0)).get mustBe "Trust 1"
          extraction.value.get(TrustBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiarySafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val trusts = (for (index <- 0 to 2) yield generateTrust(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = trustExtractor.extract(ua, trusts)

          extraction mustBe Symbol("right")

          extraction.value.get(TrustBeneficiaryNamePage(0)).get mustBe "Trust 0"
          extraction.value.get(TrustBeneficiaryNamePage(1)).get mustBe "Trust 1"
          extraction.value.get(TrustBeneficiaryNamePage(2)).get mustBe "Trust 2"

          extraction.value.get(TrustBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrustBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")

          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryDiscretionYesNoPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryShareOfIncomePage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(TrustBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryUtrPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(TrustBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(TrustBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrustBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressYesNoPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressPage(2)) mustNot be(defined)

          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrustBeneficiaryAddressUKYesNoPage(2)) mustNot be(defined)
        }

      }
    }

  }

}
