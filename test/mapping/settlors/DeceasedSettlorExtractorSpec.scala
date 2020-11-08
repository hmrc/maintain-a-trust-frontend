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

package mapping.settlors

import java.time.LocalDate

import base.SpecBaseHelpers
import generators.Generators
import mapping.PlaybackExtractor
import models.http.{AddressType, DisplayTrustIdentificationType, DisplayTrustWillType, PassportType}
import models.{FullName, MetaData, UserAnswers}
import org.joda.time.DateTime
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.settlors.deceased_settlor._

class DeceasedSettlorExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val deceasedSettlorExtractor : PlaybackExtractor[Option[DisplayTrustWillType]] =
    injector.instanceOf[DeceasedSettlorExtractor]

  "Deceased Settlor Extractor" - {

    "when no Deceased Settlor" - {

      "must return user answers" in {

        val deceasedSettlor = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, deceasedSettlor)

        extraction mustBe 'left

      }

    }

    "when there is a Deceased Settlor" - {

      "with minimum data of name, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
          identification = None,
          entityStart = "2019-11-26"
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }


      "with name and date of death, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = Some(DateTime.parse("2019-02-01")),
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

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
        extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019,2,1)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
        extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "34234234-34234-234234"
      }

      "with name and nino, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
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

        val ua = UserAnswers("fakeId","utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
        extraction.right.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }

      "with name and UK address, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
          identification = Some(
            DisplayTrustIdentificationType(
              safeId = None,
              nino = None,
              passport = None,
              address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), "GB"))
            )
          ),
          entityStart = "2019-11-26"
        )

        val ua = UserAnswers("fakeId","utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
        extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
        extraction.right.value.get(SettlorUKAddressPage) must be(defined)
        extraction.right.value.get(SettlorUKAddressPage).get.postcode mustBe "NE11NE"
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }

      "with name and International address, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
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

        val ua = UserAnswers("fakeId","utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
        extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe false
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) must be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage).get.country mustBe "DE"
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }

      "with name and passport/ID Card, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
          identification = Some(
            DisplayTrustIdentificationType(
              safeId = None,
              nino = None,
              passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), "DE")),
              address = None
            )
          ),
          entityStart = "2019-11-26"
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
        extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage) mustNot be(defined)
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) must be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage).get.country mustBe "DE"
      }

      "with name, date of death, date of birth and nino, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = Some(DateTime.parse("1970-10-15")),
          dateOfDeath = Some(DateTime.parse("2019-02-01")),
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

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe true
        extraction.right.value.get(SettlorDateOfDeathPage).get mustBe LocalDate.of(2019,2,1)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe true
        extraction.right.value.get(SettlorDateOfBirthPage).get mustBe LocalDate.of(1970,10,15)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe true
        extraction.right.value.get(SettlorNationalInsuranceNumberPage).get mustBe "NA1111111A"
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage) mustNot be(defined)
        extraction.right.value.get(SettlorUKAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }

      "with name, UK Address, passport/ID Card, metaData and safeId, must return user answers updated" in {

        val deceasedSettlor = DisplayTrustWillType(
          lineNo = "1",
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          dateOfDeath = None,
          identification = Some(
            DisplayTrustIdentificationType(
              safeId = Some("XK0000100152366"),
              nino = None,
              passport = Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), "DE")),
              address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), "GB"))
            )
          ),
          entityStart = "2019-11-26"
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = deceasedSettlorExtractor.extract(ua, Some(deceasedSettlor))

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe true
        extraction.right.value.get(SettlorLastKnownAddressUKYesNoPage).get mustBe true
        extraction.right.value.get(SettlorUKAddressPage) must be(defined)
        extraction.right.value.get(SettlorInternationalAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) must be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage).get.country mustBe "DE"
        extraction.right.value.get(DeceasedSettlorSafeIdPage).get mustBe "XK0000100152366"
        extraction.right.value.get(DeceasedSettlorMetaData).get mustBe MetaData("1", Some("01"), "2019-11-26")

      }

    }

  }

}
