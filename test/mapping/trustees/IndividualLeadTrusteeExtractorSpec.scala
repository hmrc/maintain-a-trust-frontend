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

package mapping.trustees

import base.SpecBaseHelpers
import generators.Generators
import models.errors.FailedToExtractData
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, MetaData}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.trustees._
import utils.Constants.GB
import java.time.Month._

import java.time.LocalDate

class IndividualLeadTrusteeExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val leadTrusteeIndExtractor: IndividualLeadTrusteeExtractor =
    injector.instanceOf[IndividualLeadTrusteeExtractor]

  private val (year2018, year2020, num1, num2) = (2018, 2020, 1, 2)

  "Lead Trustee Individual Extractor" - {

    "when no lead trustee individual" - {

      "must return an error" in {

        val leadTrustee = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]
      }

    }

    "when there is a lead trustee individual" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Nationality or Residence pages" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some("FR"),
            countryOfResidence = Some("FR"),
            legallyIncapable = None,
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

          val ua = emptyUserAnswersForUtr

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
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

        "with nino and UK address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with nino and International address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some("DE"),
            countryOfResidence = Some("DE"),
            legallyIncapable = Some(true),
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe "DE"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe "DE"
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with Passport/ID Card and UK address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, num2))),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) must be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with Passport/ID Card and International address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = None,
                passport = Some(
                  PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2))
                ),
                address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) must be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

      }

      "for a 5mld non taxable trust" - {

        "with nino and UK address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
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

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with nino and International address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some("DE"),
            countryOfResidence = Some("DE"),
            legallyIncapable = Some(false),
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

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe "DE"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe "DE"
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "NA1111111A"
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with Passport/ID Card and UK address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, num2))),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) must be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.value.get(TrusteeUkAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
          extraction.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

        "with Passport/ID Card and International address, return user answers updated" in {
          val leadTrustee = List(DisplayTrustLeadTrusteeIndType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = LocalDate.parse("2018-02-01"),
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            legallyIncapable = Some(false),
            phoneNumber = "+441234567890",
            email = Some("test@test.com"),
            identification =
              DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, num2))),
                address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
              ),
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = leadTrusteeIndExtractor.extract(ua, leadTrustee)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe true
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(year2018, FEBRUARY, num1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeAUKCitizenPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) must be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false
          extraction.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)) must be(defined)
          extraction.value.get(TrusteeInternationalAddressPage(0)).get.country mustBe "DE"
          extraction.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
          extraction.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeSafeIdPage(0)) must be(defined)
        }

      }
    }
  }

}
