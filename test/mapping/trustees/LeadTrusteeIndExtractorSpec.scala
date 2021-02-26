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

import java.time.LocalDate
import base.SpecBaseHelpers
import generators.Generators
import mapping.PlaybackExtractionErrors.FailedToExtractData
import mapping.PlaybackExtractor
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, MetaData, UserAnswers}
import org.joda.time.DateTime
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._
import utils.Constants.GB

class LeadTrusteeIndExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val leadTrusteeIndExtractor : PlaybackExtractor[Option[List[Trustees]]] =
    injector.instanceOf[TrusteesExtractor]

  "Lead Trustee Individual Extractor" - {

    "when no lead trustee individual" - {

      "must return an error" in {

        val leadTrustee = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]
      }

    }

    "when there is a lead trustee individual" - {

      "with nino and UK address, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = DateTime.parse("2018-02-01"),
          phoneNumber = "+441234567890",
          email = Some("test@test.com"),
          identification =
            DisplayTrustIdentificationType(
              safeId = Some("8947584-94759745-84758745"),
              nino = Some("NA1111111A"),
              passport = None,
              address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
            ),
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeIndExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(2018,2,1)
        extraction.right.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
        extraction.right.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

      "with nino and International address, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = DateTime.parse("2018-02-01"),
          phoneNumber = "+441234567890",
          email = Some("test@test.com"),
          identification =
            DisplayTrustIdentificationType(
              safeId = Some("8947584-94759745-84758745"),
              nino = Some("NA1111111A"),
              passport = None,
              address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
            ),
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeIndExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(2018,2,1)
        extraction.right.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
        extraction.right.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
        extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

      "with Passport/ID Card and UK address, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = DateTime.parse("2018-02-01"),
          phoneNumber = "+441234567890",
          email = Some("test@test.com"),
          identification =
            DisplayTrustIdentificationType(
              safeId = Some("8947584-94759745-84758745"),
              nino = None,
              passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), "DE")),
              address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
            ),
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeIndExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(2018,2,1)
        extraction.right.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
        extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteePassportIDCardPage(0)) must be(defined)
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

      "with Passport/ID Card and International address, return user answers updated" in {
        val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
          lineNo = Some(s"1"),
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = DateTime.parse("2018-02-01"),
          phoneNumber = "+441234567890",
          email = Some("test@test.com"),
          identification =
            DisplayTrustIdentificationType(
              safeId = Some("8947584-94759745-84758745"),
              nino = None,
              passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), "DE")),
              address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
            ),
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = leadTrusteeIndExtractor.extract(ua, Some(leadTrustee))

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(2018,2,1)
        extraction.right.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
        extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteePassportIDCardPage(0)) must be(defined)
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
        extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

    }

  }

}
