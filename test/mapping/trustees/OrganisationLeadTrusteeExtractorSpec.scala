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

package mapping.trustees

import base.SpecBaseHelpers
import generators.Generators
import models.MetaData
import models.errors.FailedToExtractData
import models.http.{AddressType, DisplayTrustIdentificationOrgType, DisplayTrustLeadTrusteeOrgType}
import models.pages.IndividualOrBusiness
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.trustees._
import utils.Constants.GB

class OrganisationLeadTrusteeExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val leadTrusteeOrgExtractor : OrganisationLeadTrusteeExtractor =
    injector.instanceOf[OrganisationLeadTrusteeExtractor]

  "Lead Trustee Organisation Extractor" - {

    "when no lead trustee organisation" - {

      "must return an error" in {

        val leadTrustee = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]

      }

    }

    "when there is a lead trustee organisation" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = "org1",
            countryOfResidence = Some("FR"),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationOrgType(
                safeId = Some("8947584-94759745-84758745"),
                utr = Some("1234567890"),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "which is UK registered, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = "org1",
            countryOfResidence = Some(GB),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationOrgType(
                safeId = Some("8947584-94759745-84758745"),
                utr = Some("1234567890"),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "which is not UK registered, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = "org1",
            countryOfResidence = Some("DE"),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationOrgType(
                safeId = Some("8947584-94759745-84758745"),
                utr = None,
                address = Some(AddressType("line 1", "line2", None, None, None, "FR"))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe "DE"
          extraction.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) must not be defined
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

      }

      "for a non taxable trust" - {

        "which is UK registered, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = "org1",
            countryOfResidence = Some(GB),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationOrgType(
                safeId = Some("8947584-94759745-84758745"),
                utr = Some("1234567890"),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "which is not UK registered, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = "org1",
            countryOfResidence = Some("DE"),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationOrgType(
                safeId = Some("8947584-94759745-84758745"),
                utr = None,
                address = Some(AddressType("line 1", "line2", None, None, None, "FR"))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe "DE"
          extraction.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) must not be defined
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

      }
    }
  }

}
