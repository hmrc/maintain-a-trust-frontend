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

package mapping.trustees

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import models.pages.IndividualOrBusiness
import models.{InternationalAddress, MetaData, UKAddress}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._
import utils.Constants.GB

class OrganisationTrusteeExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateTrusteeCompany(index: Int) = DisplayTrustTrusteeOrgType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = s"Trustee Company $index",
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    phoneNumber = index match {
      case 0 => Some("01911112222")
      case 1 => Some("01911112222")
      case _ => None
    },
    email = index match {
      case 0 => Some("email@email.com")
      case 1 => Some("email@email.com")
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

  val trusteesExtractor : OrganisationTrusteeExtractor =
    injector.instanceOf[OrganisationTrusteeExtractor]

  "Organisation Trustee Extractor" - {

    "when no companies" - {

      "must return user answers" in {

        val trustees = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = trusteesExtractor.extract(ua, trustees)

        extraction mustBe 'left

      }

    }

    "when there are companies" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val trustees = List(DisplayTrustTrusteeOrgType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = s"Trustee Company 1",
            countryOfResidence = Some("FR"),
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeOrgType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = s"Trustee Company 1",
            countryOfResidence = None,
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeCompany(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe 'right

          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 0"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false

          extraction.right.value.get(TrusteeOrgNamePage(1)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(1)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(1)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)

          extraction.right.value.get(TrusteeOrgNamePage(2)).get mustBe "Trustee Company 2"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)).get mustBe true
        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeOrgType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = s"Trustee Company 1",
            countryOfResidence = None,
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeCompany(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe 'right

          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 0"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)

          extraction.right.value.get(TrusteeOrgNamePage(1)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)

          extraction.right.value.get(TrusteeOrgNamePage(2)).get mustBe "Trustee Company 2"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)) mustNot be(defined)
        }
      }
    }

  }

}
