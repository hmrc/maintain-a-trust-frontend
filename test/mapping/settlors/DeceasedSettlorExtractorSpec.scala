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
import models.http.{AddressType, DisplayTrustIdentificationType, DisplayTrustWillType, PassportType}
import models.{FullName, InternationalAddress, MetaData, UKAddress, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.settlors.deceased_settlor._
import pages.trustdetails.ExpressTrustYesNoPage
import utils.Constants.GB

import java.time.LocalDate

class DeceasedSettlorExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val deceasedSettlorExtractor : DeceasedSettlorExtractor =
    injector.instanceOf[DeceasedSettlorExtractor]

  "Deceased Settlor Extractor" - {

    "when no Deceased Settlor" - {

      "must return user answers" in {

        val deceasedSettlor = Nil

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, deceasedSettlor)

        extraction mustBe 'left

      }

    }

    "when there is a Deceased Settlor" - {

      "for a 4mld taxable trust" - {

        "with minimum data of name, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = Some("FR"),
            countryOfResidence = Some("FR"),
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr")

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data of name, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }


        "with name and date of death, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            countryOfNationality = Some(GB),
            countryOfResidence = Some(GB),
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("34234234-34234-234234"),
                nino = None,
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019, 2, 1)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe GB
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe GB
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "34234234-34234-234234"
        }

        "with name and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = Some("DE"),
            countryOfResidence = Some("DE"),
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = Some("NA1111111A"),
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe "DE"
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe "DE"
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
          extraction.right.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and UK address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = None,
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
          extraction.right.value.get(SettlorLastKnownAddressPage).get mustBe UKAddress("line 1", "line2", None, None, "NE11NE")
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and International address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = None,
                address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe false
          extraction.right.value.get(SettlorLastKnownAddressPage).get mustBe InternationalAddress("Int line 1", "Int line2", None, "DE")
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and passport/ID Card, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2), "DE")),
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) must be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage).get.country mustBe "DE"
        }

        "with name, date of death, date of birth and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = Some(LocalDate.parse("1970-10-15")),
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = Some("NA1111111A"),
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019, 2, 1)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfBirthPage).get mustBe LocalDate.of(1970, 10, 15)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
          extraction.right.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, UK Address, passport/ID Card, metaData and safeId, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("XK0000100152366"),
                nino = None,
                passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2), "DE")),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, false).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
          extraction.right.value.get(SettlorLastKnownAddressPage) must be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) must be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage).get.country mustBe "DE"
          extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "XK0000100152366"
          extraction.right.value.get(DeceasedSettlorMetaData).get mustBe MetaData("1", Some("01"), "2019-11-26")

        }

      }

      "for a non taxable trust" - {

        "with minimum data of name, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }


        "with name and date of death, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            countryOfNationality = Some(GB),
            countryOfResidence = Some(GB),
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("34234234-34234-234234"),
                nino = None,
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019, 2, 1)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe GB
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe GB
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "34234234-34234-234234"
        }

        "with name and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = Some("DE"),
            countryOfResidence = Some("DE"),
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = Some("NA1111111A"),
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe "DE"
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe "DE"
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and UK address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = None,
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and International address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = None,
                address = Some(AddressType("Int line 1", "Int line2", None, None, None, "DE"))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and passport/ID Card, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2), "DE")),
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, date of death, date of birth and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = Some(LocalDate.parse("1970-10-15")),
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = Some("NA1111111A"),
                passport = None,
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019, 2, 1)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe true
          extraction.right.value.get(SettlorDateOfBirthPage).get mustBe LocalDate.of(1970, 10, 15)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, UK Address, passport/ID Card, metaData and safeId, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            countryOfNationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("XK0000100152366"),
                nino = None,
                passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2), "DE")),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false, is5mldEnabled = true)
            .set(ExpressTrustYesNoPage, true).success.value

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.right.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "XK0000100152366"
          extraction.right.value.get(DeceasedSettlorMetaData).get mustBe MetaData("1", Some("01"), "2019-11-26")

        }

      }
    }
  }

}
