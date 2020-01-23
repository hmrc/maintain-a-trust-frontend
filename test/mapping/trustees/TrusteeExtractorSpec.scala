/*
 * Copyright 2020 HM Revenue & Customs
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
import mapping.PlaybackExtractionErrors.FailedToExtractData
import mapping.PlaybackExtractor
import models.{MetaData, UserAnswers}
import models.http._
import models.pages.IndividualOrBusiness
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._

class TrusteeExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val trusteeExtractor : PlaybackExtractor[DisplayTrustEntitiesType] =
    injector.instanceOf[TrusteeExtractor]

  "Trustee Extractor" - {

    "when no trustees" - {

      "must return an error" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(None, None, None,None,None,None,None),
          None,
          DisplayTrustLeadTrusteeType(None, None),
          None, None, None)

        val ua = UserAnswers("fakeId")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]

      }

    }

    "when there is only a lead trustee with only a utr identification" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(None, None, None,None,None,None,None),
          None,
          DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = s"1",
              bpMatchStatus = Some("01"),
              name = "org1",
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = None
                ),
              entityStart = "2019-11-26"
            )
            )),
          None,
          None, None)


        val ua = UserAnswers("fakeId")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteesUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteesUkAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteesInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(EmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(LeadTrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteesSafeIdPage(0)) must be(defined)
      }

    }

    "when there is only a lead trustee" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(None, None, None,None,None,None,None),
          None,
          DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = s"1",
              bpMatchStatus = Some("01"),
              name = "org1",
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), "GB"))
                ),
              entityStart = "2019-11-26"
            )
          )),
          None,
          None, None)


        val ua = UserAnswers("fakeId")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteesUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteesUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteesUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteesInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(EmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(LeadTrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteesSafeIdPage(0)) must be(defined)
      }

    }

    "when there is a lead trustee and trustees" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(None, None, None,None,None,None,None),
          None,
          DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = s"1",
              bpMatchStatus = Some("01"),
              name = "org1",
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), "GB"))
                ),
              entityStart = "2019-11-26"
            )
            )),
            Some(List(DisplayTrustTrusteeType(None, Some(DisplayTrustTrusteeOrgType(
              lineNo = s"1",
              bpMatchStatus = Some("01"),
              name = s"Trustee Company 1",
              phoneNumber = None,
              email = None,
              identification = None,
              entityStart = "2019-11-26"
            ))))),
          None, None)


        val ua = UserAnswers("fakeId")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteesUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteesUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteesUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteesInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(EmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(LeadTrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteesSafeIdPage(0)) must be(defined)

        extraction.right.value.get(IsThisLeadTrusteePage(1)).get mustBe false
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(1)).get mustBe "Trustee Company 1"
        extraction.right.value.get(TrusteeUtrYesNoPage(1)).get mustBe false
        extraction.right.value.get(TrusteesUtrPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressYesNoPage(1)).get mustBe false
        extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteesUkAddressPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteesInternationalAddressPage(1)) mustNot be(defined)
        extraction.right.value.get(TelephoneNumberPage(1)) mustNot be(defined)
        extraction.right.value.get(EmailPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteesSafeIdPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")

      }

    }




  }

}
