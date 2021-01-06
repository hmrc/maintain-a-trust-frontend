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
import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.{AddressType, DisplayTrustCompanyType, DisplayTrustIdentificationOrgType}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.beneficiaries.company._

class CompanyBeneficiaryExtractorSpec extends FreeSpec with MustMatchers
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
    identification = Some(
      DisplayTrustIdentificationOrgType(
        safeId = Some("8947584-94759745-84758745"),
        utr = index match {
          case 1 => Some(s"${index}234567890")
          case _ => None
        },
        address = index match {
          case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), "GB"))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )

  val companyExtractor : PlaybackExtractor[Option[List[DisplayTrustCompanyType]]] =
    injector.instanceOf[CompanyBeneficiaryExtractor]

  "Company Beneficiary Extractor" - {

    "when no companies" - {

      "must return user answers" in {

        val companies = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = companyExtractor.extract(ua, companies)

        extraction mustBe 'left

      }

    }

    "when there are companies" - {

      "with minimum data must return user answers updated" in {
        val company = List(DisplayTrustCompanyType(
          lineNo = Some("1"),
          bpMatchStatus = Some("01"),
          organisationName = s"Company 1",
          beneficiaryDiscretion = None,
          beneficiaryShareOfIncome = None,
          identification = None,
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = companyExtractor.extract(ua, Some(company))

        extraction.right.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 1"
        extraction.right.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)).get mustBe true
        extraction.right.value.get(CompanyBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryAddressYesNoPage(0)).get mustBe false
        extraction.right.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryAddressPage(0)) mustNot be(defined)
      }

      "with full data must return user answers updated" in {
        val companies = (for(index <- 0 to 2) yield generateCompany(index)).toList

        val ua = UserAnswers("fakeId", "utr")

        val extraction = companyExtractor.extract(ua, Some(companies))

        extraction mustBe 'right

        extraction.right.value.get(CompanyBeneficiaryNamePage(0)).get mustBe "Company 0"
        extraction.right.value.get(CompanyBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
        extraction.right.value.get(CompanyBeneficiaryDiscretionYesNoPage(0)).get mustBe false
        extraction.right.value.get(CompanyBeneficiaryShareOfIncomePage(0)).get mustBe "98"
        extraction.right.value.get(CompanyBeneficiaryUtrPage(0)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiarySafeIdPage(0)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(CompanyBeneficiaryAddressYesNoPage(0)).get mustBe true
        extraction.right.value.get(CompanyBeneficiaryAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
        extraction.right.value.get(CompanyBeneficiaryAddressUKYesNoPage(0)).get mustBe false

        extraction.right.value.get(CompanyBeneficiaryNamePage(1)).get mustBe "Company 1"
        extraction.right.value.get(CompanyBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(CompanyBeneficiaryDiscretionYesNoPage(1)).get mustBe true
        extraction.right.value.get(CompanyBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryUtrPage(1)).get mustBe "1234567890"
        extraction.right.value.get(CompanyBeneficiarySafeIdPage(1)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(CompanyBeneficiaryAddressYesNoPage(1)).get mustBe false
        extraction.right.value.get(CompanyBeneficiaryAddressPage(1)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryAddressUKYesNoPage(1)) mustNot be(defined)

        extraction.right.value.get(CompanyBeneficiaryNamePage(2)).get mustBe "Company 2"
        extraction.right.value.get(CompanyBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
        extraction.right.value.get(CompanyBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiaryDiscretionYesNoPage(2)).get mustBe true
        extraction.right.value.get(CompanyBeneficiaryUtrPage(2)) mustNot be(defined)
        extraction.right.value.get(CompanyBeneficiarySafeIdPage(2)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(CompanyBeneficiaryAddressYesNoPage(2)).get mustBe true
        extraction.right.value.get(CompanyBeneficiaryAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
        extraction.right.value.get(CompanyBeneficiaryAddressUKYesNoPage(2)).get mustBe true
      }

    }

  }

}
