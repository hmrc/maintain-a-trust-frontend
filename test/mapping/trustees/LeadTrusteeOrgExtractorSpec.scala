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
import mapping.PlaybackExtractionErrors.FailedToExtractData
import mapping.PlaybackExtractor
import models.{MetaData, UserAnswers}
import models.http.{AddressType, DisplayTrustIdentificationOrgType, DisplayTrustLeadTrusteeOrgType, Trustees}
import models.pages.IndividualOrBusiness
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._
import utils.Constants.GB

class LeadTrusteeOrgExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val leadTrusteeOrgExtractor : PlaybackExtractor[Option[List[Trustees]]] =
    injector.instanceOf[TrusteesExtractor]

  "Lead Trustee Organisation Extractor" - {

    "when no lead trustee organisation" - {

      "must return an error" in {

        val leadTrustee = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeOrgExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]

      }

    }

    "when there is a lead trustee organisation" - {

      "which is UK registered, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = "org1",
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

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeOrgExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

      "which is not UK registered, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeOrgType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = "org1",
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

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeOrgExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
        extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
        extraction.right.value.get(TrusteeUkAddressPage(0)) must not be defined
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

    }

  }

}
