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
import mapping.PlaybackExtractionErrors.FailedToExtractData
import models.http._
import models.pages.KindOfBusiness.Trading
import models.pages.{IndividualOrBusiness, KindOfBusiness}
import models.{FullName, MetaData, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.settlors.deceased_settlor._
import pages.settlors.living_settlor._

class SettlorExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val settlorExtractor: SettlorExtractor =
    injector.instanceOf[SettlorExtractor]

  "Settlor Extractor" - {

    "when no settlors" - {

      "must return an error" in {

        val entities = DisplayTrustEntitiesType(None,
          DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          None, DisplayTrustLeadTrusteeType(None, None),
          None, None, None)

        val ua = UserAnswers("fakeId", "utr")

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.left.value mustBe a[FailedToExtractData]

      }

    }

    "when deceased settlor" - {

      "must return user answers updated" in {

        val entities = DisplayTrustEntitiesType(
          naturalPerson = None,
          beneficiary = DisplayTrustBeneficiaryType(
            individualDetails = Nil,
            company = Nil,
            trust = Nil,
            charity = Nil,
            unidentified = Nil,
            large = Nil,
            other = Nil
          ),
          deceased = Some(DisplayTrustWillType(
            lineNo = Some("1"),
            bpMatchStatus = Some("01"),
            name = FullName("First Name", None, "Last Name"),
            dateOfBirth = None,
            dateOfDeath = None,
            identification = None,
            entityStart = "2019-11-26"
          )),
          leadTrustee = DisplayTrustLeadTrusteeType(leadTrusteeInd = None, leadTrusteeOrg = None),
          trustees = None,
          protectors = None,
          settlors = None
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.right.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.right.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfDeathPage) mustNot be(defined)
        extraction.right.value.get(SettlorDateOfBirthYesNoPage).get mustBe false
        extraction.right.value.get(SettlorDateOfBirthPage) mustNot be(defined)
        extraction.right.value.get(SettlorNationalInsuranceYesNoPage).get mustBe false
        extraction.right.value.get(SettlorNationalInsuranceNumberPage) mustNot be(defined)
        extraction.right.value.get(SettlorLastKnownAddressYesNoPage).get mustBe false
        extraction.right.value.get(SettlorLastKnownAddressPage) mustNot be(defined)
        extraction.right.value.get(SettlorPassportIDCardPage) mustNot be(defined)
      }
    }

    "when there are living settlors of different types" - {

      "must return user answers updated" in {

        val entities = DisplayTrustEntitiesType(
          naturalPerson = None,
          beneficiary = DisplayTrustBeneficiaryType(
            individualDetails = Nil,
            company = Nil,
            trust = Nil,
            charity = Nil,
            unidentified = Nil,
            large = Nil,
            other = Nil
          ),
          deceased = None,
          leadTrustee = DisplayTrustLeadTrusteeType(leadTrusteeInd = None, leadTrusteeOrg = None),
          trustees = None,
          protectors = None,
          settlors = Some(DisplayTrustSettlors(
            settlor = List(
              DisplayTrustSettlor(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = FullName("individual", Some("living"), "settlor"),
                dateOfBirth = None,
                identification = None,
                entityStart = "2019-11-26"
              )
            ),
            settlorCompany = List(
              DisplayTrustSettlorCompany(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = s"Company Settlor 1",
                companyType = Some(KindOfBusiness.Trading),
                companyTime = Some(false),
                identification = Some(
                  DisplayTrustIdentificationOrgType(
                    safeId = Some("8947584-94759745-84758745"),
                    utr = Some("1234567890"),
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              ),
              DisplayTrustSettlorCompany(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = s"Company Settlor 2",
                companyType = Some(KindOfBusiness.Trading),
                companyTime = Some(false),
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
          ))
        )

        val ua = UserAnswers("fakeId", "utr")

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.right.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.right.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("individual", Some("living"), "settlor")
        extraction.right.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe false
        extraction.right.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressYesNoPage(0)).get mustBe false
        extraction.right.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
        extraction.right.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.right.value.get(SettlorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(SettlorBusinessNamePage(1)).get mustBe "Company Settlor 1"
        extraction.right.value.get(SettlorUtrYesNoPage(1)).get mustBe true
        extraction.right.value.get(SettlorUtrPage(1)).get mustBe "1234567890"
        extraction.right.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressPage(1)) mustNot be(defined)
        extraction.right.value.get(SettlorCompanyTypePage(1)).get mustBe Trading
        extraction.right.value.get(SettlorCompanyTimePage(1)).get mustBe false
        extraction.right.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.right.value.get(SettlorIndividualOrBusinessPage(2)).get mustBe IndividualOrBusiness.Business
        extraction.right.value.get(SettlorBusinessNamePage(2)).get mustBe "Company Settlor 2"
        extraction.right.value.get(SettlorUtrYesNoPage(2)).get mustBe true
        extraction.right.value.get(SettlorUtrPage(2)).get mustBe "1234567890"
        extraction.right.value.get(SettlorAddressYesNoPage(2)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressUKYesNoPage(2)) mustNot be(defined)
        extraction.right.value.get(SettlorAddressPage(2)) mustNot be(defined)
        extraction.right.value.get(SettlorCompanyTypePage(2)).get mustBe Trading
        extraction.right.value.get(SettlorCompanyTimePage(2)).get mustBe false
        extraction.right.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
        extraction.right.value.get(SettlorMetaData(2)).get mustBe MetaData("1", Some("01"), "2019-11-26")

      }

    }

  }

}
