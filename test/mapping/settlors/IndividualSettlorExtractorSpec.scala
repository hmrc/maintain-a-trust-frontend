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
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.KindOfBusiness._
import models.{FullName, InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.settlors.living_settlor._
import utils.Constants.{DE, GB}

import java.time.Month.FEBRUARY
import java.time.LocalDate

class IndividualSettlorExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (year1970, year2020) = (1970, 2020)

  def generateSettlorIndividual(index: Int): DisplayTrustSettlor = DisplayTrustSettlor(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = Some(LocalDate.parse("1970-02-01")),
    nationality = populateCountry(index),
    countryOfResidence = populateCountry(index),
    legallyIncapable = index match {
      case 0 => Some(false)
      case 1 => Some(true)
      case _ => None
    },
    identification = populateIdentification(index),
    entityStart = "2019-11-26"
  )

  private def populateCountry(index: Int): Option[String] =
    index match {
      case 0 => Some(GB)
      case 1 => Some(DE)
      case _ => None
    }

  private def populateIdentification(index: Int): Option[DisplayTrustIdentificationType] =
    Some(
      DisplayTrustIdentificationType(
        safeId = Some("8947584-94759745-84758745"),
        nino = index match {
          case 0 => Some(s"${index}234567890")
          case _ => None
        },
        passport = index match {
          case 2 => Some(PassportType(DE, "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, java.time.Month.FEBRUARY,2)))
          case _ => None
        },
        address = index match {
          case 1 => Some(AddressType(s"line $index", "line2", None, None, None, DE))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    )

  val individualSettlorExtractor: IndividualSettlorExtractor =
    injector.instanceOf[IndividualSettlorExtractor]

  "Living Settlor Extractor" - {

    "when no individuals" - {

      "must return user answers" in {

        val settlors = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = individualSettlorExtractor.extract(ua, settlors)

        extraction mustBe Symbol("left")

      }

    }

    "when there are individuals" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Nationality or Residence pages" in {
          val settlors = List(DisplayTrustSettlor(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = Some("FR"),
            countryOfResidence = Some("FR"),
            legallyIncapable = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = individualSettlorExtractor.extract(ua, settlors)

          extraction.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val settlors = List(DisplayTrustSettlor(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = None,
            countryOfResidence = None,
            legallyIncapable = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = individualSettlorExtractor.extract(ua, settlors)

          extraction.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val settlors = (for (index <- 0 to 2) yield generateSettlorIndividual(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = individualSettlorExtractor.extract(ua, settlors)

          extraction mustBe Symbol("right")

          extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(SettlorIndividualDateOfBirthPage(0)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorIndividualNINOPage(0)).get mustBe "0234567890"
          extraction.value.get(SettlorAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(SettlorIndividualNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(SettlorIndividualDateOfBirthPage(1)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorIndividualNINOPage(1)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(1)).get mustBe true
          extraction.value.get(SettlorAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.value.get(SettlorAddressUKYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorIndividualPassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(SettlorIndividualNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.value.get(SettlorIndividualDateOfBirthPage(2)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(2)).get mustBe false
          extraction.value.get(SettlorIndividualNINOPage(2)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(2)).get mustBe true
          extraction.value.get(SettlorAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.value.get(SettlorAddressUKYesNoPage(2)).get mustBe true
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(2)).get mustBe true
          extraction.value.get(SettlorIndividualPassportIDCardPage(2)).get.countryOfIssue mustBe "DE"
          extraction.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val settlors = List(DisplayTrustSettlor(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = None,
            countryOfResidence = None,
            legallyIncapable = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = individualSettlorExtractor.extract(ua, settlors)

          extraction.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(SettlorIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val settlors = (for (index <- 0 to 2) yield generateSettlorIndividual(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = individualSettlorExtractor.extract(ua, settlors)

          extraction mustBe Symbol("right")

          extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(SettlorIndividualDateOfBirthPage(0)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
          extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(SettlorIndividualNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(SettlorIndividualDateOfBirthPage(1)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(1)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOPage(1)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(1)) mustNot be(defined)
          extraction.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(SettlorIndividualNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.value.get(SettlorIndividualDateOfBirthPage(2)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(SettlorCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(SettlorIndividualNINOYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorIndividualNINOPage(2)) mustNot be(defined)
          extraction.value.get(SettlorAddressYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorAddressPage(2)) mustNot be(defined)
          extraction.value.get(SettlorAddressUKYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(2)) mustNot be(defined)
          extraction.value.get(SettlorIndividualPassportIDCardPage(2)) mustNot be(defined)
          extraction.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }
    }
  }
}
