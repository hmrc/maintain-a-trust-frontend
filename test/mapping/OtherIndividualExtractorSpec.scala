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

package mapping

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, DisplayTrustIdentificationType, NaturalPersonType, PassportType}
import models.{FullName, InternationalAddress, MetaData, UKAddress, UserAnswers}
import org.joda.time.DateTime
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.individual._
import utils.Constants.GB

import java.time.LocalDate

class OtherIndividualExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateIndividual(index: Int) = NaturalPersonType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = index match {
      case 0 => Some(DateTime.parse("1970-02-01"))
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

  val individualExtractor : OtherIndividualExtractor =
    injector.instanceOf[OtherIndividualExtractor]

  "Other Individual Extractor" - {

    "when no individual" - {

      "must return user answers" in {

        val individual = None

        val ua = UserAnswers("fakeId", "utr")

        val extraction = individualExtractor.extract(ua, individual)

        extraction mustBe 'right

      }

    }

    "when there are individuals" - {

      "with minimum data must return user answers updated" in {
        val individual = List(NaturalPersonType(
          lineNo = Some("1"),
          bpMatchStatus = Some("01"),
          name = FullName("First Name", None, "Last Name"),
          dateOfBirth = None,
          identification = None,
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId", "utr")

        val extraction = individualExtractor.extract(ua, Some(individual))

        extraction.right.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe false
        extraction.right.value.get(OtherIndividualDateOfBirthPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualNationalInsuranceYesNoPage(0)).get mustBe false
        extraction.right.value.get(OtherIndividualNationalInsuranceNumberPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualAddressYesNoPage(0)).get mustBe false
        extraction.right.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualSafeIdPage(0)) mustNot be(defined)
      }

      "with full data must return user answers updated" in {
        val individuals = (for(index <- 0 to 2) yield generateIndividual(index)).toList

        val ua = UserAnswers("fakeId", "utr")

        val extraction = individualExtractor.extract(ua, Some(individuals))

        extraction mustBe 'right

        extraction.right.value.get(OtherIndividualNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
        extraction.right.value.get(OtherIndividualNamePage(1)).get mustBe FullName("First Name 1", None, "Last Name 1")
        extraction.right.value.get(OtherIndividualNamePage(2)).get mustBe FullName("First Name 2", None, "Last Name 2")

        extraction.right.value.get(OtherIndividualMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
        extraction.right.value.get(OtherIndividualMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(OtherIndividualMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")

        extraction.right.value.get(OtherIndividualDateOfBirthYesNoPage(0)).get mustBe true
        extraction.right.value.get(OtherIndividualDateOfBirthYesNoPage(1)).get mustBe false
        extraction.right.value.get(OtherIndividualDateOfBirthYesNoPage(2)).get mustBe false

        extraction.right.value.get(OtherIndividualDateOfBirthPage(0)).get mustBe LocalDate.of(1970,2,1)
        extraction.right.value.get(OtherIndividualDateOfBirthPage(1)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualDateOfBirthPage(2)) mustNot be(defined)

        extraction.right.value.get(OtherIndividualNationalInsuranceYesNoPage(0)).get mustBe true
        extraction.right.value.get(OtherIndividualNationalInsuranceYesNoPage(1)).get mustBe false
        extraction.right.value.get(OtherIndividualNationalInsuranceYesNoPage(2)).get mustBe false

        extraction.right.value.get(OtherIndividualNationalInsuranceNumberPage(0)).get mustBe "0234567890"
        extraction.right.value.get(OtherIndividualNationalInsuranceNumberPage(1)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualNationalInsuranceNumberPage(2)) mustNot be(defined)

        extraction.right.value.get(OtherIndividualAddressYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualAddressYesNoPage(1)).get mustBe true
        extraction.right.value.get(OtherIndividualAddressYesNoPage(2)).get mustBe true

        extraction.right.value.get(OtherIndividualAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualAddressUKYesNoPage(1)).get mustBe false
        extraction.right.value.get(OtherIndividualAddressUKYesNoPage(2)).get mustBe true

        extraction.right.value.get(OtherIndividualAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualAddressPage(1)).get mustBe InternationalAddress("line 1", "line2", None, "DE")
        extraction.right.value.get(OtherIndividualAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")

        extraction.right.value.get(OtherIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualPassportIDCardPage(0)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualPassportIDCardYesNoPage(1)).get mustBe false
        extraction.right.value.get(OtherIndividualPassportIDCardPage(1)) mustNot be(defined)
        extraction.right.value.get(OtherIndividualPassportIDCardYesNoPage(2)).get mustBe true
        extraction.right.value.get(OtherIndividualPassportIDCardPage(2)).get.country mustBe "DE"

        extraction.right.value.get(OtherIndividualSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(OtherIndividualSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(OtherIndividualSafeIdPage(2)).get mustBe "8947584-94759745-84758745"

      }

    }

  }

}
