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
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, InternationalAddress, MetaData, UKAddress, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._
import utils.Constants.GB

import java.time.LocalDate

class IndividualTrusteeExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateTrusteeIndividual(index: Int) = DisplayTrustTrusteeIndividualType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = index match {
      case 1 => Some(LocalDate.parse("1970-02-01"))
      case _ => None
    },
    countryOfNationality = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    countryOfResidence = index match {
      case 0 => Some(GB)
      case 1 => Some("DE")
      case _ => None
    },
    phoneNumber = index match {
      case 0 => Some(s"${index}111144444")
      case _ => None
    },
    identification = Some(
      DisplayTrustIdentificationType(
        safeId = Some("8947584-94759745-84758745"),
        nino = index match {
          case 0 => Some(s"${index}234567890")
          case _ => None
        },
        passport = index match {
          case 2 => Some(PassportType("KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020,2,2), "DE"))
          case _ => None
        },
        address = index match {
          case 1 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), GB))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )


  val trusteesExtractor : IndividualTrusteeExtractor =
    injector.instanceOf[IndividualTrusteeExtractor]

  "Individual Trustee Extractor" - {

    "when no individuals" - {

      "must return user answers" in {

        val trustees = Nil

        val ua = UserAnswers("fakeId", "utr")

        val extraction = trusteesExtractor.extract(ua, trustees)

        extraction mustBe 'left

      }

    }

    "when there are individuals" - {

      "for a taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            countryOfNationality = None,
            countryOfResidence = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeIndividual(index)).toList

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe 'right

          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeNinoPage(0)).get mustBe "0234567890"
          extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeNinoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)).get mustBe false
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeNinoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)).get mustBe true
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(2)).get mustBe true
          extraction.right.value.get(TrusteePassportIDCardPage(2)).get.country mustBe "DE"
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }

      "for a non taxable trust" - {

        "with minimum data must return user answers updated" in {
          val trustees = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            countryOfNationality = None,
            countryOfResidence = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "urn", isTrustTaxable = false)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trustees = (for (index <- 0 to 2) yield generateTrusteeIndividual(index)).toList

          val ua = UserAnswers("fakeId", "urn", isTrustTaxable = false)

          val extraction = trusteesExtractor.extract(ua, trustees)

          extraction mustBe 'right

          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityPage(0)).get mustBe GB
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfNationalityPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeCountryOfResidencePage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeNinoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

        }
      }
    }

  }

}
