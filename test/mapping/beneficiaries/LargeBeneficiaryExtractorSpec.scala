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
import models.HowManyBeneficiaries.{Over1, Over1001, Over201}
import models.http.{AddressType, DisplayTrustIdentificationOrgType, DisplayTrustLargeType}
import models.{Description, InternationalAddress, MetaData, UKAddress}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.beneficiaries.large._
import utils.Constants.GB

class LargeBeneficiaryExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateLargeBeneficiary(index: Int) = DisplayTrustLargeType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    organisationName = s"Large $index",
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    description = s"Description $index",
    description1 = None,
    description2 = None,
    description3 = None,
    description4 = None,
    numberOfBeneficiary = index match {
      case 0 => "100"
      case 1 => "500"
      case _ => "1000"
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
    beneficiaryDiscretion = index match {
      case 0 => Some(false)
      case _ => None
    },
    beneficiaryShareOfIncome = index match {
      case 0 => Some("98")
      case _ => None
    },
    entityStart = "2019-11-26"
  )

  val largeBeneficiaryExtractor : LargeBeneficiaryExtractor =
    injector.instanceOf[LargeBeneficiaryExtractor]

  "Large Beneficiary Extractor" - {

    "when no large beneficiaries" - {

      "must return user answers" in {

        val largeBeneficiaries = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiaries)

        extraction mustBe 'left

      }

    }

    "when there are large beneficiaries" - {

      "for a 4mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val largeBeneficiary = List(
            DisplayTrustLargeType(
              lineNo = Some("1"),
              bpMatchStatus = Some("01"),
              organisationName = "Large 1",
              countryOfResidence = Some("FR"),
              description = s"Description",
              description1 = Some("Description 1"),
              description2 = None,
              description3 = None,
              description4 = None,
              numberOfBeneficiary = "100",
              identification = Some(
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = None,
                  address = Some(AddressType(s"line 1", "line 2", None, None, Some("NE11NE"), GB))
                )
              ),
              beneficiaryDiscretion = Some(false),
              beneficiaryShareOfIncome = Some("10"),
              entityStart = "2019-11-26"
            )
          )

          val ua = emptyUserAnswersForUtr

          val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiary)

          extraction.right.value.get(LargeBeneficiaryNamePage(0)).get mustBe "Large 1"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(0)).get mustBe Description("Description", Some("Description 1"), None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(0)).get mustBe "10"
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressPage(0)).get mustBe UKAddress("line 1", "line 2", None, None, "NE11NE")
          extraction.right.value.get(LargeBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(0)) must be(defined)
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(0)).get mustBe Over1
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val largeBeneficiary = List(
            DisplayTrustLargeType(
              lineNo = Some("1"),
              bpMatchStatus = Some("01"),
              organisationName = "Large 1",
              countryOfResidence = None,
              description = s"Description",
              description1 = Some("Description 1"),
              description2 = None,
              description3 = None,
              description4 = None,
              numberOfBeneficiary = "100",
              identification = Some(
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = None,
                  address = Some(AddressType(s"line 1", "line 2", None, None, Some("NE11NE"), GB))
                )
              ),
              beneficiaryDiscretion = Some(false),
              beneficiaryShareOfIncome = Some("10"),
              entityStart = "2019-11-26"
            )
          )

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiary)

          extraction.right.value.get(LargeBeneficiaryNamePage(0)).get mustBe "Large 1"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(0)).get mustBe Description("Description", Some("Description 1"), None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(0)).get mustBe "10"
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressPage(0)).get mustBe UKAddress("line 1", "line 2", None, None, "NE11NE")
          extraction.right.value.get(LargeBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(0)) must be(defined)
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(0)).get mustBe Over1
        }

        "with full data must return user answers updated" in {
          val largeBeneficiaries = (for (index <- 0 to 2) yield generateLargeBeneficiary(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiaries)

          extraction mustBe 'right

          extraction.right.value.get(LargeBeneficiaryNamePage(0)).get mustBe "Large 0"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(0)).get mustBe Description("Description 0", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(0)).get mustBe "98"
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(LargeBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(0)).get mustBe Over1

          extraction.right.value.get(LargeBeneficiaryNamePage(1)).get mustBe "Large 1"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(1)).get mustBe Description("Description 1", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(1)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(1)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(LargeBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(1)).get mustBe Over201

          extraction.right.value.get(LargeBeneficiaryNamePage(2)).get mustBe "Large 2"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(2)).get mustBe Description("Description 2", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(2)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(2)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(LargeBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(2)).get mustBe Over1001
        }

      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val largeBeneficiary = List(
            DisplayTrustLargeType(
              lineNo = Some("1"),
              bpMatchStatus = Some("01"),
              countryOfResidence = None,
              organisationName = "Large 1",
              description = s"Description",
              description1 = Some("Description 1"),
              description2 = None,
              description3 = None,
              description4 = None,
              numberOfBeneficiary = "100",
              identification = Some(
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = None,
                  address = Some(AddressType(s"line 1", "line 2", None, None, Some("NE11NE"), GB))
                )
              ),
              beneficiaryDiscretion = Some(false),
              beneficiaryShareOfIncome = Some("10"),
              entityStart = "2019-11-26"
            )
          )

          val ua = emptyUserAnswersForUrn

          val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiary)

          extraction.right.value.get(LargeBeneficiaryNamePage(0)).get mustBe "Large 1"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(0)).get mustBe Description("Description", Some("Description 1"), None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(0)) must be(defined)
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(0)).get mustBe Over1
        }

        "with full data must return user answers updated" in {
          val largeBeneficiaries = (for (index <- 0 to 2) yield generateLargeBeneficiary(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = largeBeneficiaryExtractor.extract(ua, largeBeneficiaries)

          extraction mustBe 'right

          extraction.right.value.get(LargeBeneficiaryNamePage(0)).get mustBe "Large 0"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(0)).get mustBe Description("Description 0", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(0)).get mustBe Over1

          extraction.right.value.get(LargeBeneficiaryNamePage(1)).get mustBe "Large 1"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(1)).get mustBe Description("Description 1", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(1)).get mustBe Over201

          extraction.right.value.get(LargeBeneficiaryNamePage(2)).get mustBe "Large 2"
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(LargeBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryDescriptionPage(2)).get mustBe Description("Description 2", None, None, None, None)
          extraction.right.value.get(LargeBeneficiaryDiscretionYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressUKYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(LargeBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(LargeBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(LargeBeneficiaryNumberOfBeneficiariesPage(2)).get mustBe Over1001
        }

      }
    }

  }

}
