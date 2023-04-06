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

package mapping

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustIdentificationType, NaturalPersonType, PassportType}
import models.{FullName, InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.individual._
import utils.Constants.{DE, GB}

import java.time.LocalDate

class OtherIndividualExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (year1970, year2020) = (1970, 2020)

  def generateIndividual(index: Int) = NaturalPersonType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = index match {
      case 0 => Some(LocalDate.parse("1970-02-01"))
      case _ => None
    },
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
          case 2 => Some(PassportType("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020,2,2)))
          case _ => None
        },
        address = index match {
          case 1 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    )

  val individualExtractor : OtherIndividualExtractor =
    injector.instanceOf[OtherIndividualExtractor]

  "Other Individual Extractor" - {

    "when no individual" - {

      "must return user answers" in {

        val individual = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = individualExtractor.extract(ua, individual)

        extraction mustBe Symbol("right")

      }

    }

    "when there are individuals" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Residence pages" in {
          val individual = List(NaturalPersonType(
            lineNo = Some("1"),
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

          val extraction = individualExtractor.extract(ua, individual)

          extraction.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualSafeIdPage(0)) mustNot be(defined)
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val individual = List(NaturalPersonType(
            lineNo = Some("1"),
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

          val extraction = individualExtractor.extract(ua, individual)

          extraction.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualSafeIdPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val individuals = (for (index <- 0 to 2) yield generateIndividual(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = individualExtractor.extract(ua, individuals)

          extraction mustBe Symbol("right")

          extraction.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(OtherIndividualNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(OtherIndividualNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")

          extraction.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")

          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualDateOfBirthPage(0)).get mustBe LocalDate.of(year1970, 2, 1)
          extraction.value.get(OtherIndividualDateOfBirthPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualDateOfBirthPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(OtherIndividualCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(OtherIndividualCountryOfNationalityPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(OtherIndividualCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(OtherIndividualCountryOfResidencePage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(0)).get mustBe "0234567890"
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(1)).get mustBe true
          extraction.value.get(OtherIndividualAddressYesNoPage(2)).get mustBe true

          extraction.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressUKYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualAddressUKYesNoPage(2)).get mustBe true

          extraction.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.value.get(OtherIndividualAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")

          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualPassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(2)).get mustBe true
          extraction.value.get(OtherIndividualPassportIDCardPage(2)).get.countryOfIssue mustBe "DE"

          extraction.value.get(OtherIndividualSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(OtherIndividualSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(OtherIndividualSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val individual = List(NaturalPersonType(
            lineNo = Some("1"),
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

          val extraction = individualExtractor.extract(ua, individual)

          extraction.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualSafeIdPage(0)) mustNot be(defined)
        }

        "with full data must return user answers updated" in {
          val individuals = (for (index <- 0 to 2) yield generateIndividual(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = individualExtractor.extract(ua, individuals)

          extraction mustBe Symbol("right")

          extraction.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(OtherIndividualNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(OtherIndividualNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")

          extraction.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(OtherIndividualMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")

          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualDateOfBirthYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualDateOfBirthPage(0)).get mustBe LocalDate.of(year1970, 2, 1)
          extraction.value.get(OtherIndividualDateOfBirthPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualDateOfBirthPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(OtherIndividualCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(OtherIndividualCountryOfNationalityPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceYesNoPage(2)).get mustBe false

          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(OtherIndividualCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(OtherIndividualCountryOfResidencePage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(OtherIndividualMentalCapacityYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualNationalInsuranceNumberPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressYesNoPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressUKYesNoPage(1)) mustNot be(defined)

          extraction.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualAddressPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardYesNoPage(2)) mustNot be(defined)
          extraction.value.get(OtherIndividualPassportIDCardPage(2)) mustNot be(defined)

          extraction.value.get(OtherIndividualSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(OtherIndividualSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.value.get(OtherIndividualSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
        }
      }
    }
  }
}
