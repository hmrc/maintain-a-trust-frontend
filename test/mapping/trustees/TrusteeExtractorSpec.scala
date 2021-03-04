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
import mapping.PlaybackExtractionErrors.FailedToExtractData
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, MetaData, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustees._
import utils.Constants.GB

class TrusteeExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val trusteeExtractor : TrusteeExtractor =
    injector.instanceOf[TrusteeExtractor]

  "Trustee Extractor" - {

    "when no trustees" - {

      "must return an error" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          None,
          DisplayTrustLeadTrusteeType(None, None),
          None, None, None
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.left.value mustBe a[FailedToExtractData]

      }

    }

    "when there is only a lead trustee with only a utr identification" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          None,
          DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = Some(s"1"),
              bpMatchStatus = Some("01"),
              name = "org1",
              countryOfResidence = Some(GB),
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = None
                ),
              entityStart = "2019-11-26"
            )
            )),
          None,
          None, None
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

    }

    "when there is only a lead trustee" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          None,
          DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = Some(s"1"),
              bpMatchStatus = Some("01"),
              name = "org1",
              countryOfResidence = Some(GB),
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
                ),
              entityStart = "2019-11-26"
            )
            )),
          None,
          None, None
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)
      }

    }

    "when there is a lead trustee and trustees" - {

      "return user answers updated" in {

        val leadTrustee = DisplayTrustEntitiesType(None,
          beneficiary = DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          deceased = None,
          leadTrustee = DisplayTrustLeadTrusteeType(None,
            Some(DisplayTrustLeadTrusteeOrgType(
              lineNo = Some("1"),
              bpMatchStatus = Some("01"),
              name = "org1",
              countryOfResidence = Some(GB),
              phoneNumber = "+441234567890",
              email = Some("test@test.com"),
              identification =
                DisplayTrustIdentificationOrgType(
                  safeId = Some("8947584-94759745-84758745"),
                  utr = Some("1234567890"),
                  address = Some(AddressType("line 1", "line2", None, None, Some("NE11NE"), GB))
                ),
              entityStart = "2019-11-26"
            )
            )),
          trustees = Some(List(
            DisplayTrustTrusteeType(
              trusteeInd = None,
              trusteeOrg = Some(DisplayTrustTrusteeOrgType(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = s"Trustee Company 1",
                countryOfResidence = Some(GB),
                phoneNumber = None,
                email = None,
                identification = None,
                entityStart = "2019-11-26"
              ))
            ),
            DisplayTrustTrusteeType(
              trusteeInd = Some(DisplayTrustTrusteeIndividualType(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = FullName("First Name", None, "Last Name"),
                dateOfBirth = None,
                countryOfNationality = Some(GB),
                countryOfResidence = Some(GB),
                legallyIncapable = Some(false),
                phoneNumber = None,
                identification = None,
                entityStart = "2019-11-26"
              )),
              trusteeOrg = None
            )
          )),
          protectors = None,
          settlors = None
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = trusteeExtractor.extract(ua, leadTrustee)

        extraction.right.value.get(IsThisLeadTrusteePage(0)).get mustBe true
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(0)).get mustBe "org1"
        extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidencePage(0)).get mustBe GB
        extraction.right.value.get(TrusteeUtrYesNoPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUtrPage(0)).get mustBe "1234567890"
        extraction.right.value.get(TrusteeAddressInTheUKPage(0)).get mustBe true
        extraction.right.value.get(TrusteeUkAddressPage(0)) must be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(0)).get.postcode mustBe "NE11NE"
        extraction.right.value.get(TrusteeInternationalAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(0)).get mustBe "+441234567890"
        extraction.right.value.get(TrusteeEmailPage(0)).get mustBe "test@test.com"
        extraction.right.value.get(TrusteeMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(TrusteeSafeIdPage(0)) must be(defined)

        extraction.right.value.get(IsThisLeadTrusteePage(1)).get mustBe false
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(TrusteeNamePage(1)).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(TrusteeDateOfBirthYesNoPage(1)).get mustBe false
        extraction.right.value.get(TrusteeDateOfBirthPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeCountryOfNationalityYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfNationalityInTheUkYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfNationalityPage(1)).get mustBe GB
        extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe GB
        extraction.right.value.get(TrusteeMentalCapacityYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeNinoYesNoPage(1)).get mustBe false
        extraction.right.value.get(TrusteeNinoPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressYesNoPage(1)).get mustBe false
        extraction.right.value.get(TrusteeAddressInTheUKPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteePassportIDCardYesNoPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteePassportIDCardPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeSafeIdPage(1)) mustNot be(defined)
        extraction.right.value.get(TrusteeMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.right.value.get(IsThisLeadTrusteePage(2)).get mustBe false
        extraction.right.value.get(TrusteeIndividualOrBusinessPage(2)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(TrusteeOrgNamePage(2)).get mustBe "Trustee Company 1"
        extraction.right.value.get(TrusteeCountryOfResidenceYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidenceInTheUkYesNoPage(1)).get mustBe true
        extraction.right.value.get(TrusteeCountryOfResidencePage(1)).get mustBe GB
        extraction.right.value.get(TrusteeUtrYesNoPage(2)).get mustBe false
        extraction.right.value.get(TrusteeUtrPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeAddressYesNoPage(2)).get mustBe false
        extraction.right.value.get(TrusteeAddressInTheUKPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeUkAddressPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeInternationalAddressPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeTelephoneNumberPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeEmailPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeSafeIdPage(2)) mustNot be(defined)
        extraction.right.value.get(TrusteeMetaData(2)).get mustBe MetaData("1", Some("01"), "2019-11-26")

      }

    }

  }

}
