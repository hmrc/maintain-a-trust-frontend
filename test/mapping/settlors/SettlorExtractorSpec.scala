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

package mapping.settlors

import base.SpecBaseHelpers
import generators.Generators
import models.errors.FailedToExtractData
import models.http._
import models.pages.KindOfBusiness.Trading
import models.pages.{IndividualOrBusiness, KindOfBusiness}
import models.{FullName, MetaData}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.settlors.deceased_settlor._
import pages.settlors.living_settlor._
import utils.Constants.GB

class SettlorExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  private val settlorExtractor: SettlorExtractor =
    injector.instanceOf[SettlorExtractor]

  "Settlor Extractor" - {

    "when no settlors" - {
      "must return original answers" in {

        val entities = DisplayTrustEntitiesType(
          naturalPerson = None,
          beneficiary = DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
          deceased = None,
          leadTrustee = DisplayTrustLeadTrusteeType(None, None),
          trustees = None,
          protectors = None,
          settlors = None
        )

        val ua = emptyUserAnswersForUtr

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
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            identification = None,
            entityStart = "2019-11-26"
          )),
          leadTrustee = DisplayTrustLeadTrusteeType(leadTrusteeInd = None, leadTrusteeOrg = None),
          trustees = None,
          protectors = None,
          settlors = None
        )

        val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
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
                nationality = Some(GB),
                countryOfResidence = Some(GB),
                legallyIncapable = Some(false),
                identification = None,
                entityStart = "2019-11-26"
              )
            ),
            settlorCompany = List(
              DisplayTrustSettlorCompany(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = s"Company Settlor 1",
                countryOfResidence = Some(GB),
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
                countryOfResidence = Some("DE"),
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

        val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("individual", Some("living"), "settlor")
        extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfNationalityPage(0)).get mustBe GB
        extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
        extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe false
        extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
        extraction.value.get(SettlorAddressYesNoPage(0)).get mustBe false
        extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
        extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
        extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
        extraction.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
        extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.value.get(SettlorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
        extraction.value.get(SettlorBusinessNamePage(1)).get mustBe "Company Settlor 1"
        extraction.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorCountryOfResidencePage(1)).get mustBe GB
        extraction.value.get(SettlorUtrYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorUtrPage(1)).get mustBe "1234567890"
        extraction.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
        extraction.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)
        extraction.value.get(SettlorAddressPage(1)) mustNot be(defined)
        extraction.value.get(SettlorCompanyTypePage(1)).get mustBe Trading
        extraction.value.get(SettlorCompanyTimePage(1)).get mustBe false
        extraction.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
        extraction.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.value.get(SettlorIndividualOrBusinessPage(2)).get mustBe IndividualOrBusiness.Business
        extraction.value.get(SettlorBusinessNamePage(2)).get mustBe "Company Settlor 2"
        extraction.value.get(SettlorCountryOfResidenceYesNoPage(2)).get mustBe true
        extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(2)).get mustBe false
        extraction.value.get(SettlorCountryOfResidencePage(2)).get mustBe "DE"
        extraction.value.get(SettlorUtrYesNoPage(2)).get mustBe true
        extraction.value.get(SettlorUtrPage(2)).get mustBe "1234567890"
        extraction.value.get(SettlorAddressYesNoPage(2)) mustNot be(defined)
        extraction.value.get(SettlorAddressUKYesNoPage(2)) mustNot be(defined)
        extraction.value.get(SettlorAddressPage(2)) mustNot be(defined)
        extraction.value.get(SettlorCompanyTypePage(2)).get mustBe Trading
        extraction.value.get(SettlorCompanyTimePage(2)).get mustBe false
        extraction.value.get(SettlorSafeIdPage(2)).get mustBe "8947584-94759745-84758745"
        extraction.value.get(SettlorMetaData(2)).get mustBe MetaData("1", Some("01"), "2019-11-26")

      }
    }

    "when deceased settlor and additional settlors" - {
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
            nationality = Some(GB),
            countryOfResidence = Some(GB),
            identification = None,
            entityStart = "2019-11-26"
          )),
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
                nationality = Some(GB),
                countryOfResidence = Some(GB),
                legallyIncapable = Some(false),
                identification = None,
                entityStart = "2019-11-26"
              )
            ),
            settlorCompany = List(
              DisplayTrustSettlorCompany(
                lineNo = Some(s"1"),
                bpMatchStatus = Some("01"),
                name = s"Company Settlor 1",
                countryOfResidence = Some(GB),
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

        val ua = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

        val extraction = settlorExtractor.extract(ua, entities)

        extraction.value.get(SettlorNamePage).get mustBe FullName("First Name", None, "Last Name")
        extraction.value.get(SettlorDateOfDeathYesNoPage).get mustBe false
        extraction.value.get(SettlorDateOfDeathPage) mustNot be(defined)
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

        extraction.value.get(SettlorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
        extraction.value.get(SettlorIndividualNamePage(0)).get mustBe FullName("individual", Some("living"), "settlor")
        extraction.value.get(SettlorCountryOfNationalityYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfNationalityPage(0)).get mustBe GB
        extraction.value.get(SettlorCountryOfResidenceYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorCountryOfResidencePage(0)).get mustBe GB
        extraction.value.get(SettlorIndividualMentalCapacityYesNoPage(0)).get mustBe true
        extraction.value.get(SettlorIndividualNINOYesNoPage(0)).get mustBe false
        extraction.value.get(SettlorIndividualNINOPage(0)) mustNot be(defined)
        extraction.value.get(SettlorAddressYesNoPage(0)).get mustBe false
        extraction.value.get(SettlorAddressUKYesNoPage(0)) mustNot be(defined)
        extraction.value.get(SettlorAddressPage(0)) mustNot be(defined)
        extraction.value.get(SettlorIndividualPassportIDCardYesNoPage(0)) mustNot be(defined)
        extraction.value.get(SettlorIndividualPassportIDCardPage(0)) mustNot be(defined)
        extraction.value.get(SettlorSafeIdPage(0)) mustNot be(defined)
        extraction.value.get(SettlorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

        extraction.value.get(SettlorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
        extraction.value.get(SettlorBusinessNamePage(1)).get mustBe "Company Settlor 1"
        extraction.value.get(SettlorCountryOfResidenceYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorCountryOfResidencePage(1)).get mustBe GB
        extraction.value.get(SettlorUtrYesNoPage(1)).get mustBe true
        extraction.value.get(SettlorUtrPage(1)).get mustBe "1234567890"
        extraction.value.get(SettlorAddressYesNoPage(1)) mustNot be(defined)
        extraction.value.get(SettlorAddressUKYesNoPage(1)) mustNot be(defined)
        extraction.value.get(SettlorAddressPage(1)) mustNot be(defined)
        extraction.value.get(SettlorCompanyTypePage(1)).get mustBe Trading
        extraction.value.get(SettlorCompanyTimePage(1)).get mustBe false
        extraction.value.get(SettlorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
        extraction.value.get(SettlorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
      }
    }
  }
}
