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

package mapping.settlors

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustIdentificationType, DisplayTrustWillType, PassportType}
import models.{FullName, InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.settlors.deceased_settlor._
import utils.Constants.GB
import java.time.Month._

import java.time.LocalDate

class DeceasedSettlorExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  val deceasedSettlorExtractor: DeceasedSettlorExtractor =
    injector.instanceOf[DeceasedSettlorExtractor]

  private val (year1970, year2019, year2020, num15) = (1970, 2019, 2020, 15)

  "Deceased Settlor Extractor" - {

    "when no Deceased Settlor" - {

      "must return user answers" in {

        val deceasedSettlor = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = deceasedSettlorExtractor.extract(ua, deceasedSettlor)

        extraction mustBe Symbol("left")

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
            nationality = Some("FR"),
            countryOfResidence = Some("FR"),
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUtr

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
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
            nationality = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }


        "with name and date of death, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            nationality = Some(GB),
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(year2019, FEBRUARY, 1)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe GB
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe GB
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorSafeIdPage).get mustBe "34234234-34234-234234"
        }

        "with name and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = Some("DE"),
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe "DE"
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe "DE"
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
          extraction.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and UK address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
          extraction.value.get(SettlorLastKnownAddressPage).get mustBe UKAddress("line 1", "line2", None, None, "NE11NE")
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and International address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe false
          extraction.value.get(SettlorLastKnownAddressPage).get mustBe InternationalAddress("Int line 1", "Int line2", None, "DE")
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and passport/ID Card, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2))),
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) must be(defined)
          extraction.value.get(SettlorPassportIDCardPage).get.countryOfIssue mustBe "DE"
        }

        "with name, date of death, date of birth and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = Some(LocalDate.parse("1970-10-15")),
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            nationality = None,
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

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(year2019, FEBRUARY, 1)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfBirthPage).get mustBe LocalDate.of(year1970, OCTOBER, num15)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
          extraction.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, UK Address, passport/ID Card, metaData and safeId, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("XK0000100152366"),
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2))),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
          extraction.value.get(SettlorLastKnownAddressPage) must be(defined)
          extraction.value.get(SettlorPassportIDCardPage) must be(defined)
          extraction.value.get(SettlorPassportIDCardPage).get.countryOfIssue mustBe "DE"
          extraction.value.get(DeceasedSettlorSafeIdPage).get mustBe "XK0000100152366"
          extraction.value.get(DeceasedSettlorMetaData).get mustBe MetaData("1", Some("01"), "2019-11-26")

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
            nationality = None,
            countryOfResidence = None,
            identification = None,
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }


        "with name and date of death, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            nationality = Some(GB),
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

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(year2019, FEBRUARY, 1)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe GB
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe GB
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorSafeIdPage).get mustBe "34234234-34234-234234"
        }

        "with name and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = Some("DE"),
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

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage).get mustBe "DE"
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe true
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidencePage).get mustBe "DE"
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and UK address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
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

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and International address, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
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

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name and passport/ID Card, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = None,
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2))),
                address = None
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, date of death, date of birth and nino, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = Some(LocalDate.parse("1970-10-15")),
            dateOfDeath = Some(LocalDate.parse("2019-02-01")),
            nationality = None,
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

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(year2019, FEBRUARY, 1)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe true
          extraction.value.get(SettlorDateOfBirthPage).get mustBe LocalDate.of(year1970, OCTOBER, num15)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        }

        "with name, UK Address, passport/ID Card, metaData and safeId, must return user answers updated" in {

          val deceasedSettlor = DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            nationality = None,
            countryOfResidence = None,
            identification = Some(
              DisplayTrustIdentificationType(
                safeId = Some("XK0000100152366"),
                nino = None,
                passport = Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2))),
                address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
              )
            ),
            entityStart = "2019-11-26"
          )

          val ua = emptyUserAnswersForUrn

          val extraction = deceasedSettlorExtractor.extract(ua, List(deceasedSettlor))

          extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
          extraction.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
          extraction.value.get(SettlorDateOfBirthPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfNationalityInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfNationalityPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidenceYesNoPage).get mustBe false
          extraction.value.get(DeceasedSettlorCountryOfResidenceInTheUkYesNoPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorCountryOfResidencePage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
          extraction.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
          extraction.value.get(SettlorPassportIDCardPage) mustNot be(defined)
          extraction.value.get(DeceasedSettlorSafeIdPage).get mustBe "XK0000100152366"
          extraction.value.get(DeceasedSettlorMetaData).get mustBe MetaData("1", Some("01"), "2019-11-26")

        }

      }
    }
  }

}
