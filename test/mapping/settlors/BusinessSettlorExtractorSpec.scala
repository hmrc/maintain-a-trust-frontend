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

package mapping.settlors

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import models.pages.KindOfBusiness._
import models.pages.{IndividualOrBusiness, KindOfBusiness}
import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.settlors.living_settlor._
import pages.trustdetails.ExpressTrustYesNoPage
import utils.Constants.GB

class BusinessSettlorExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateSettlorCompany(index: Int) = DisplayTrustSettlorCompany(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = s"Company Settlor $index",
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    companyType = index match {
      case 0 => Some(KindOfBusiness.Trading)
      case 1 => Some(KindOfBusiness.Investment)
      case _ => None
    },
    companyTime = index match {
      case 0 => Some(false)
      case 1 => Some(true)
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

  val businessSettlorExtractor: BusinessSettlorExtractor =
    injector.instanceOf[BusinessSettlorExtractor]

  "Business Settlor Extractor" - {

    "when no companies" - {

      "must return user answers" in {

        val settlors = Nil

        val ua = UserAnswers("fakeId", "utr")

        val extraction = businessSettlorExtractor.extract(ua, settlors)

        extraction mustBe 'left

      }

    }

    "when there are companies" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val settlors = List(DisplayTrustSettlorCompany(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = s"Company Settlor 1",
            countryOfResidence = Some("FR"),
            companyType = None,
            companyTime = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr")

          val extraction = businessSettlorExtractor.extract(ua, settlors)

          extraction.right.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(SettlorBusinessNamePage(0)).get mustBe "Company Settlor 1"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTypePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val settlors = List(DisplayTrustSettlorCompany(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = s"Company Settlor 1",
            countryOfResidence = None,
            companyType = None,
            companyTime = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = businessSettlorExtractor.extract(ua, settlors)

          extraction.right.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(SettlorBusinessNamePage(0)).get mustBe "Company Settlor 1"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTypePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val settlors = (for (index <- 0 to 2) yield generateSettlorCompany(index)).toList

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = businessSettlorExtractor.extract(ua, settlors)

          extraction mustBe 'right

          extraction.right.value.get(SettlorBusinessNamePage(0)).get mustBe "Company Settlor 0"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(0)).get mustBe Trading
          extraction.right.value.get(SettlorCompanyTimePage(0)).get mustBe false
          extraction.right.value.get(SettlorUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(SettlorAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(SettlorAddressUKYesNoPage(0)).get mustBe false

          extraction.right.value.get(SettlorBusinessNamePage(1)).get mustBe "Company Settlor 1"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(1)).get mustBe Investment
          extraction.right.value.get(SettlorCompanyTimePage(1)).get mustBe true
          extraction.right.value.get(SettlorUtrYesNoPage(1)).get mustBe true
          extraction.right.value.get(SettlorUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.right.value.get(SettlorBusinessNamePage(2)).get mustBe "Company Settlor 2"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(2)).get mustBe false
          extraction.right.value.get(SettlorUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(SettlorAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(SettlorAddressUKYesNoPage(2)).get mustBe true
        }

      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val settlors = List(DisplayTrustSettlorCompany(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = s"Company Settlor 1",
            countryOfResidence = None,
            companyType = None,
            companyTime = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = businessSettlorExtractor.extract(ua, settlors)

          extraction.right.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(SettlorBusinessNamePage(0)).get mustBe "Company Settlor 1"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTypePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val settlors = (for (index <- 0 to 2) yield generateSettlorCompany(index)).toList

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = businessSettlorExtractor.extract(ua, settlors)

          extraction mustBe 'right

          extraction.right.value.get(SettlorBusinessNamePage(0)).get mustBe "Company Settlor 0"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)

          extraction.right.value.get(SettlorBusinessNamePage(1)).get mustBe "Company Settlor 1"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.right.value.get(SettlorBusinessNamePage(2)).get mustBe "Company Settlor 2"
          extraction.right.value.get(SettlorCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(SettlorCompanyTypePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorCompanyTimePage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(SettlorAddressYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(SettlorAddressUKYesNoPage(2)) mustNot be(defined)
        }

      }
    }
  }
}
