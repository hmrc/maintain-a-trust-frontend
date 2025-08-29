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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustCompanyType, DisplayTrustIdentificationOrgType}
import models.{InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.beneficiaries.company._
import utils.Constants.{DE, GB}

class CompanyBeneficiaryExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateCompany(index: Int) = DisplayTrustCompanyType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    organisationName = s"Company $index",
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
          case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
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

  val companyExtractor : CompanyBeneficiaryExtractor =
    injector.instanceOf[CompanyBeneficiaryExtractor]

  "Company Beneficiary Extractor" - {

    "when no companies" - {

      "must return user answers" in {

        val companies = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = companyExtractor.extract(ua, companies)

        extraction mustBe Symbol("left")

      }

    }

    "when there are companies" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val company = List(DisplayTrustCompanyType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Company 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = Some("FR"),
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = companyExtractor.extract(ua, company)

          extraction.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 1"
          extraction.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(0)) mustNot be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val company = List(DisplayTrustCompanyType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Company 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = companyExtractor.extract(ua, company)

          extraction.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 1"
          extraction.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(0)).get mustBe false
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val companies = (for (index <- 0 to 2) yield generateCompany(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = companyExtractor.extract(ua, companies)

          extraction mustBe Symbol("right")

          extraction.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 0"
          extraction.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)).get mustBe false
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(0)).get mustBe "98"
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)).get mustBe false

          extraction.value.get(CompanyBeneficiaryNamePage(1)).get mustBe "Company 1"
          extraction.value.get(CompanyBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(1)).get mustBe true
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(CompanyBeneficiaryUtrPage(1)).get mustBe "1234567890"
          extraction.value.get(CompanyBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(1)).get mustBe false
          extraction.value.get(CompanyBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.value.get(CompanyBeneficiaryNamePage(2)).get mustBe "Company 2"
          extraction.value.get(CompanyBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(2)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(2)).get mustBe true
          extraction.value.get(CompanyBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(2)).get mustBe true
        }

      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val company = List(DisplayTrustCompanyType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            organisationName = s"Company 1",
            beneficiaryDiscretion = None,
            beneficiaryShareOfIncome = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = companyExtractor.extract(ua, company)

          extraction.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 1"
          extraction.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val companies = (for (index <- 0 to 2) yield generateCompany(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = companyExtractor.extract(ua, companies)

          extraction mustBe Symbol("right")

          extraction.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 0"
          extraction.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(0)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)

          extraction.value.get(CompanyBeneficiaryNamePage(1)).get mustBe "Company 1"
          extraction.value.get(CompanyBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(CompanyBeneficiaryUtrPage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(1)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.value.get(CompanyBeneficiaryNamePage(2)).get mustBe "Company 2"
          extraction.value.get(CompanyBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(CompanyBeneficiaryDiscretionYesNoPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(CompanyBeneficiaryCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryUtrPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(CompanyBeneficiaryAddressYesNoPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressPage(2)) mustNot be(defined)
          extraction.value.get(CompanyBeneficiaryAddressUKYesNoPage(2)) mustNot be(defined)
        }
      }
    }
  }
}
