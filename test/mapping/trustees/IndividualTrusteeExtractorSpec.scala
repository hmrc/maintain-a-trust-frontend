/*
 * Copyright 2024 HM Revenue & Customs
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
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, InternationalAddress, MetaData, UKAddress}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.trustees._
import utils.Constants.{DE, GB}

import java.time.Month.FEBRUARY
import java.time.LocalDate

class IndividualTrusteeExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val (year1970, year2020) = (1970, 2020)

  def generateTrusteeIndividual(index: Int): DisplayTrustTrusteeIndividualType = DisplayTrustTrusteeIndividualType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = index match {
      case 1 => Some(LocalDate.parse("1970-02-01"))
      case _ => None
    },
    nationality = populateCountry(index),
    countryOfResidence = populateCountry(index),
    legallyIncapable = index match {
      case 0 => Some(false)
      case 1 => Some(true)
      case _ => None
    },
    phoneNumber = index match {
      case 0 => Some(s"${index}111144444")
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
  private def populateIdentification(index: Int) =
    Some(
      DisplayTrustIdentificationType(
        safeId = Some("8947584-94759745-84758745"),
        nino = index match {
          case 0 => Some(s"${index}234567890")
          case _ => None
        },
        passport = index match {
          case 2 => Some(PassportType(DE, "KSJDFKSDHF6456545147852369QWER", LocalDate.of(year2020, FEBRUARY, 2)))
          case _ => None
        },
        address = index match {
          case 1 => Some(AddressType(s"line $index", "line2", None, None, None, DE))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    )

  val trusteesExtractor : IndividualTrusteeExtractor =
    injector.instanceOf[IndividualTrusteeExtractor]

  "Individual Trustee Extractor" - {

    "when no individuals" - {

      "must return user answers" in {

        val trustees = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = trusteesExtractor.extract(ua, trustees)

        extraction mustBe Symbol("left")

      }

    }

    "when there are individuals" - {

      "for a 4mld taxable trust" - {

        "should not populate Country Of Nationality or Residence pages" in {
          val trustees = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = Some("FR"),
            countryOfResidence = Some("FR"),
            legallyIncapable = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeNinoYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }

      "for a 5mld taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = None,
            countryOfResidence = None,
            legallyIncapable = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeNinoYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeIndividual(index)).toList

          val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe Symbol("right")

          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeNinoPage(0)).get mustBe "0234567890"
          extraction.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrusteeNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(TrusteeMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeNinoPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.value.get(TrusteeAddressInTheUKPage(1)).get mustBe false
          extraction.value.get(TrusteePassportIDCardYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrusteeNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeNinoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(2)).get mustBe true
          extraction.value.get(TrusteeAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.value.get(TrusteeAddressInTheUKPage(2)).get mustBe true
          extraction.value.get(TrusteePassportIDCardYesNoPage(2)).get mustBe true
          extraction.value.get(TrusteePassportIDCardPage(2)).get.countryOfIssue mustBe "DE"
          extraction.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            nationality = None,
            countryOfResidence = None,
            legallyIncapable = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = emptyUserAnswersForUrn

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeNinoYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeIndividual(index)).toList

          val ua = emptyUserAnswersForUrn

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe Symbol("right")

          extraction.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.value.get(TrusteeMentalCapacityYesNoPage(0)).get mustBe true
          extraction.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrusteeNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(year1970, FEBRUARY, 1)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.value.get(TrusteeMentalCapacityYesNoPage(1)).get mustBe false
          extraction.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeNinoPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.value.get(TrusteeNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.value.get(TrusteeDateOfBirthYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeDateOfBirthPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.value.get(TrusteeMentalCapacityYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.value.get(TrusteeNinoYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeNinoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeAddressYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeAddressPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeAddressInTheUKPage(2)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardYesNoPage(2)) mustNot be(defined)
          extraction.value.get(TrusteePassportIDCardPage(2)) mustNot be(defined)
          extraction.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }
    }

  }

}
