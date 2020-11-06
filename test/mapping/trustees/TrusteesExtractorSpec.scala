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

package mapping.trustees

import java.time.LocalDate

import base.SpecBaseHelpers
import generators.Generators
import mapping.{PassportType, PlaybackExtractor}
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, InternationalAddress, MetaData, PassportOrIdCardDetails, UKAddress, UserAnswers}
import org.joda.time.DateTime
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._

class TrusteesExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateTrusteeCompany(index: Int) = DisplayTrustTrusteeOrgType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = s"Trustee Company $index",
    phoneNumber = index match {
      case 0 => Some("01911112222")
      case 1 => Some("01911112222")
      case _ => None
    },
    email = index match {
      case 0 => Some("email@email.com")
      case 1 => Some("email@email.com")
      case _ => None
    },
    identification = Some(
      DisplayTrustIdentificationOrgType(
        safeId = Some("8947584-94759745-84758745"),
        utr = index match {
          case 1 => Some(s"${index}234567890")
          case _ => None
        },
        address = index match {
          case 0 => Some(AddressType(s"line $index", "line2", None, None, None, "DE"))
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), "GB"))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )

  def generateTrusteeIndividual(index: Int) = DisplayTrustTrusteeIndividualType(
    lineNo = Some(s"$index"),
    bpMatchStatus = Some("01"),
    name = FullName(s"First Name $index", None, s"Last Name $index"),
    dateOfBirth = index match {
      case 1 => Some(DateTime.parse("1970-02-01"))
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
          case 2 => Some(AddressType(s"line $index", "line2", None, None, Some("NE11NE"), "GB"))
          case _ => None
        }
      )
    ),
    entityStart = "2019-11-26"
  )


  val trusteesExtractor : PlaybackExtractor[Option[List[Trustees]]] =
    injector.instanceOf[TrusteesExtractor]

  "Trustees Extractor" - {

    "Trustee Companies" - {

      "when no companies" - {

        "must return user answers" in {

          val trusts = None

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, trusts)

          extraction mustBe 'left

        }

      }

      "when there are companies" - {

        "with minimum data must return user answers updated" in {
          val trust = List(DisplayTrustTrusteeOrgType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = s"Trustee Company 1",
            phoneNumber = None,
            email = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, Some(trust))

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }

        "with full data must return user answers updated" in {
          val trusts = (for (index <- 0 to 2) yield generateTrusteeCompany(index)).toList

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, Some(trusts))

          extraction mustBe 'right

          extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "Trustee Company 0"
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(0)).get mustBe InternationalAddress("line 0", "line2", None, "DE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe false

          extraction.right.value.get(TrusteeOrgNamePage(1)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(1)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(1)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)

          extraction.right.value.get(TrusteeOrgNamePage(2)).get mustBe "Trustee Company 2"
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("2", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeEmailPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeUtrYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(TrusteeAddressPage(2)).get mustBe UKAddress("line 2", "line2", None, None, "NE11NE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)).get mustBe true
        }

      }

    }

    "Trustee Individuals" - {

      "when no individuals" - {

        "must return user answers" in {

          val trusts = None

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, trusts)

          extraction mustBe 'left

        }

      }

      "when there are individuals" - {

        "with minimum data must return user answers updated" in {
          val trust = List(DisplayTrustTrusteeIndividualType(
            lineNo = Some(s"1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            phoneNumber = None,
            identification = None,
            entityStart = "2019-11-26"
          ))

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, Some(trust))

          extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe false
          extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
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
          val trusts = (for(index <- 0 to 2) yield generateTrusteeIndividual(index)).toList

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, Some(trusts))

          extraction mustBe 'right

          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("First Name 0", None, "Last Name 0")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeDateOfBirthPage(0)) mustNot be(defined)
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
          extraction.right.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(1970,2,1)
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

    }

    "Combined Trustee Individuals & Trustee Companies" - {

      "when no trustees" - {

        "must return user answers" in {

          val trusts = None

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, trusts)

          extraction mustBe 'left

        }

      }

      "when there are trustees" - {

        "must return user answers updated" in {

          val trusts = List(
            DisplayTrustTrusteeIndividualType(
              lineNo = Some("01"),
              bpMatchStatus = Some("01"),
              name = FullName("individual", None, "trustee 1"),
              dateOfBirth = Some(DateTime.parse("1970-02-01")),
              phoneNumber = Some("01911112222"),
              identification = Some(DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = None,
                passport = Some(PassportType(
                  number = "KSJDFKSDHF6456545147852369QWER",
                  expirationDate = LocalDate.of(2020, 2, 2),
                  countryOfIssue = "DE"
                )),
                address = Some(AddressType(
                  line1 = "line 1",
                  line2 = "line 2",
                  line3 = None,
                  line4 = None,
                  postCode = Some("NE11NE"),
                  country = "GB"
                ))
              )),
              entityStart = "2019-11-26"
            ),
            DisplayTrustTrusteeIndividualType(
              lineNo = Some("01"),
              bpMatchStatus = Some("01"),
              name = FullName("individual", None, "trustee 2"),
              dateOfBirth = Some(DateTime.parse("1970-02-01")),
              phoneNumber = Some("01911112222"),
              identification = Some(DisplayTrustIdentificationType(
                safeId = Some("8947584-94759745-84758745"),
                nino = Some("1234567890"),
                passport = None,
                address = None
              )),
              entityStart = "2019-11-26"
            ),
            DisplayTrustTrusteeOrgType(
              lineNo = Some("01"),
              bpMatchStatus = Some("01"),
              name = "Trustee Company 1",
              phoneNumber = Some("01911112222"),
              email = Some("email@email.com"),
              identification = Some(
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = None,
                  address = Some(AddressType(s"line1", "line2", None, None, None, "DE"))
                )
              ),
              entityStart = "2019-11-26"
            ),
            DisplayTrustTrusteeOrgType(
              lineNo = Some("01"),
              bpMatchStatus = Some("01"),
              name = "Trustee Company 2",
              phoneNumber = Some("01911112222"),
              email = Some("email@email.com"),
              identification = Some(
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = None
                )
              ),
              entityStart = "2019-11-26"
            )
          )

          val ua = UserAnswers("fakeId", "utr")

          val extraction = trusteesExtractor.extract(ua, Some(trusts))

          extraction mustBe 'right

          extraction.right.value.get(TrusteeNamePage(0)).get mustBe FullName("individual", None, "trustee 1")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeDateOfBirthPage(0)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("01", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(0)).get mustBe false
          extraction.right.value.get(TrusteeNinoPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(0)).get mustBe UKAddress("line 1", "line 2", None, None, "NE11NE")
          extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(0)).get mustBe true
          extraction.right.value.get(TrusteePassportIDCardPage(0)).get mustBe PassportOrIdCardDetails("DE", "KSJDFKSDHF6456545147852369QWER", LocalDate.of(2020, 2, 2))
          extraction.right.value.get(TrusteeSafeIdPage(0)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeNamePage(1)).get mustBe FullName("individual", None, "trustee 2")
          extraction.right.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeDateOfBirthPage(1)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("01", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeNinoYesNoPage(1)).get mustBe true
          extraction.right.value.get(TrusteeNinoPage(1)).get mustBe "1234567890"
          extraction.right.value.get(TrusteeAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(1)).get mustBe "8947584-94759745-84758745"

          extraction.right.value.get(TrusteeOrgNamePage(2)).get mustBe "Trustee Company 1"
          extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("01", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(2)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(2)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(2)).get mustBe false
          extraction.right.value.get(TrusteeUtrPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(2)).get mustBe true
          extraction.right.value.get(TrusteeInternationalAddressPage(2)).get mustBe InternationalAddress("line1", "line2", None, "DE")
          extraction.right.value.get(TrusteeUkAddressPage(2)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(2)).get mustBe false

          extraction.right.value.get(TrusteeOrgNamePage(3)).get mustBe "Trustee Company 2"
          extraction.right.value.get(TrusteeMetaData(3)).get mustBe MetaData("01", Some("01"), "2019-11-26")
          extraction.right.value.get(TrusteeTelephoneNumberPage(3)).get mustBe "01911112222"
          extraction.right.value.get(TrusteeEmailPage(3)).get mustBe "email@email.com"
          extraction.right.value.get(TrusteeUtrYesNoPage(3)).get mustBe true
          extraction.right.value.get(TrusteeUtrPage(3)).get mustBe "1234567890"
          extraction.right.value.get(TrusteeSafeIdPage(3)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(TrusteeAddressYesNoPage(3)) mustNot be(defined)
          extraction.right.value.get(TrusteeInternationalAddressPage(3)) mustNot be(defined)
          extraction.right.value.get(TrusteeUkAddressPage(3)) mustNot be(defined)
          extraction.right.value.get(TrusteeAddressInTheUKPage(3)) mustNot be(defined)

        }

      }

    }

  }

}
