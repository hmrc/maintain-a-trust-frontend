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

package mapping.protectors

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import models.pages.IndividualOrBusiness
import models.{FullName, MetaData, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.protectors._
import pages.protectors.business._
import pages.protectors.individual._
import utils.Constants.GB

import java.time.LocalDate

class ProtectorExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  val protectorExtractor : ProtectorExtractor =
    injector.instanceOf[ProtectorExtractor]

  "Protector Extractor" - {

    "when no protectors" - {

      "must return right given no individual protector and no company protector" in {

        val protector = DisplayTrustProtectorsType(Nil, Nil)

        val ua = UserAnswers("fakeId", "utr")

        val extraction = protectorExtractor.extract(ua, Some(protector))

        extraction mustBe 'right
        extraction.right.value.data mustBe ua.data
      }

      "must return right given no protector" in {

        val ua = UserAnswers("fakeId", "utr")

        val extraction = protectorExtractor.extract(ua, None)

        extraction mustBe 'right
        extraction.right.value.data mustBe ua.data
      }
    }

    "for a taxable trust" - {

    "when there are individual protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = List(
              DisplayTrustProtector(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1970-02-01")),
                countryOfResidence = Some(GB),
                countryOfNationality = Some(GB),
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("8947584-94759745-84758745"),
                    nino = Some(s"1234567890"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              ),
              DisplayTrustProtector(
                lineNo = Some("2"),
                bpMatchStatus = Some("02"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1980-02-01")),
                countryOfResidence = Some("DE"),
                countryOfNationality = Some("DE"),
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("1234567-12345678-12345678"),
                    nino = Some(s"0987654321"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-27"
              )
            ),
            protectorCompany = Nil
          )

          val ua = UserAnswers("fakeId", "utr")

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorDateOfBirthYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorDateOfBirthPage(0)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorNINOPage(0)).get mustBe "1234567890"
          extraction.right.value.get(IndividualProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(1)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorDateOfBirthYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorDateOfBirthPage(1)).get mustBe LocalDate.of(1980, 2, 1)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorNINOPage(1)).get mustBe "0987654321"
          extraction.right.value.get(IndividualProtectorSafeIdPage(1)).get mustBe "1234567-12345678-12345678"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(1)).get mustBe MetaData("2", Some("02"), "2019-11-27")
        }
      }

      "when there are business protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = Nil,
            protectorCompany = List(
              DisplayTrustProtectorBusiness(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = s"Business 1",
                countryOfResidence = Some(GB),
                identification = Some(
                  DisplayTrustIdentificationOrgType(
                    safeId = Some("8947584-94759745-84758745"),
                    utr = Some("1234567890"),
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              ),
              DisplayTrustProtectorBusiness(
                lineNo = Some("2"),
                bpMatchStatus = Some("02"),
                name = s"Business 2",
                countryOfResidence = Some("DE"),
                identification = Some(
                  DisplayTrustIdentificationOrgType(
                    safeId = Some("1234567-12345678-12345678"),
                    utr = Some("0987654321"),
                    address = None
                  )
                ),
                entityStart = "2019-11-27"
              )
            )
          )

          val ua = UserAnswers("fakeId", "utr")

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(0)).get mustBe "Business 1"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(BusinessProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(0)).get mustBe true
          extraction.right.value.get(BusinessProtectorUtrPage(0)).get mustBe "1234567890"
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(1)).get mustBe "Business 2"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(BusinessProtectorSafeIdPage(1)).get mustBe "1234567-12345678-12345678"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(1)).get mustBe true
          extraction.right.value.get(BusinessProtectorUtrPage(1)).get mustBe "0987654321"
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(1)).get mustBe MetaData("2", Some("02"), "2019-11-27")
        }
      }

      "when there are individual and business protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = List(
              DisplayTrustProtector(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1970-02-01")),
                countryOfResidence = Some(GB),
                countryOfNationality = None,
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("8947584-94759745-84758745"),
                    nino = Some(s"1234567890"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              )
            ),
            protectorCompany = List(
              DisplayTrustProtectorBusiness(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = s"Business 1",
                countryOfResidence = None,
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
          )

          val ua = UserAnswers("fakeId", "utr")

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorNINOPage(0)).get mustBe "1234567890"
          extraction.right.value.get(IndividualProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(1)).get mustBe "Business 1"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(1)).get mustBe false
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(1)).get mustBe true
          extraction.right.value.get(BusinessProtectorUtrPage(1)).get mustBe "1234567890"
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }
    }

    "for a non taxable trust" - {

      "when there are individual protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = List(
              DisplayTrustProtector(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1970-02-01")),
                countryOfResidence = Some(GB),
                countryOfNationality = Some(GB),
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("8947584-94759745-84758745"),
                    nino = Some(s"1234567890"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              ),
              DisplayTrustProtector(
                lineNo = Some("2"),
                bpMatchStatus = Some("02"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1980-02-01")),
                countryOfResidence = Some("DE"),
                countryOfNationality = Some("DE"),
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("1234567-12345678-12345678"),
                    nino = Some(s"0987654321"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-27"
              )
            ),
            protectorCompany = Nil
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false)

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorDateOfBirthYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorDateOfBirthPage(0)).get mustBe LocalDate.of(1970, 2, 1)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorNINOPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(1)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorDateOfBirthYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorDateOfBirthPage(1)).get mustBe LocalDate.of(1980, 2, 1)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(1)).get mustBe "DE"
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorNINOPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorSafeIdPage(1)).get mustBe "1234567-12345678-12345678"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(1)).get mustBe MetaData("2", Some("02"), "2019-11-27")
        }
      }

      "when there are business protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = Nil,
            protectorCompany = List(
              DisplayTrustProtectorBusiness(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = s"Business 1",
                countryOfResidence = Some(GB),
                identification = Some(
                  DisplayTrustIdentificationOrgType(
                    safeId = Some("8947584-94759745-84758745"),
                    utr = Some("1234567890"),
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              ),
              DisplayTrustProtectorBusiness(
                lineNo = Some("2"),
                bpMatchStatus = Some("02"),
                name = s"Business 2",
                countryOfResidence = Some("DE"),
                identification = Some(
                  DisplayTrustIdentificationOrgType(
                    safeId = Some("1234567-12345678-12345678"),
                    utr = Some("0987654321"),
                    address = None
                  )
                ),
                entityStart = "2019-11-27"
              )
            )
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false)

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(0)).get mustBe "Business 1"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(BusinessProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorUtrPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(0)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(1)).get mustBe "Business 2"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(1)).get mustBe true
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(1)).get mustBe false
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(1)).get mustBe "DE"
          extraction.right.value.get(BusinessProtectorSafeIdPage(1)).get mustBe "1234567-12345678-12345678"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(1)).get mustBe MetaData("2", Some("02"), "2019-11-27")
        }
      }

      "when there are individual and business protectors" - {

        "must return user answers updated with doesTrustHaveAProtector true" in {
          val protectors = DisplayTrustProtectorsType(
            protector = List(
              DisplayTrustProtector(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = FullName(s"First Name", None, s"Last Name"),
                dateOfBirth = Some(LocalDate.parse("1970-02-01")),
                countryOfResidence = Some(GB),
                countryOfNationality = None,
                identification = Some(
                  DisplayTrustIdentificationType(
                    safeId = Some("8947584-94759745-84758745"),
                    nino = Some(s"1234567890"),
                    passport = None,
                    address = None
                  )
                ),
                entityStart = "2019-11-26"
              )
            ),
            protectorCompany = List(
              DisplayTrustProtectorBusiness(
                lineNo = Some("1"),
                bpMatchStatus = Some("01"),
                name = s"Business 1",
                countryOfResidence = None,
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
          )

          val ua = UserAnswers("fakeId", "utr", isTrustTaxable = false)

          val extraction = protectorExtractor.extract(ua, Some(protectors))

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(0)).get mustBe IndividualOrBusiness.Individual
          extraction.right.value.get(IndividualProtectorNamePage(0)).get mustBe FullName("First Name", None, "Last Name")
          extraction.right.value.get(IndividualProtectorCountryOfNationalityYesNoPage(0)).get mustBe false
          extraction.right.value.get(IndividualProtectorCountryOfNationalityInTheUkYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorCountryOfNationalityPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorCountryOfResidenceYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidenceInTheUkYesNoPage(0)).get mustBe true
          extraction.right.value.get(IndividualProtectorCountryOfResidencePage(0)).get mustBe GB
          extraction.right.value.get(IndividualProtectorNINOYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorNINOPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorSafeIdPage(0)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(IndividualProtectorPassportIDCardYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorPassportIDCardPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorAddressUKYesNoPage(0)) mustNot be(defined)
          extraction.right.value.get(IndividualProtectorMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")

          extraction.right.value.get(ProtectorIndividualOrBusinessPage(1)).get mustBe IndividualOrBusiness.Business
          extraction.right.value.get(BusinessProtectorNamePage(1)).get mustBe "Business 1"
          extraction.right.value.get(BusinessProtectorCountryOfResidenceYesNoPage(1)).get mustBe false
          extraction.right.value.get(BusinessProtectorCountryOfResidenceInTheUkYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorCountryOfResidencePage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorSafeIdPage(1)).get mustBe "8947584-94759745-84758745"
          extraction.right.value.get(BusinessProtectorUtrYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorUtrPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressUKYesNoPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorAddressPage(1)) mustNot be(defined)
          extraction.right.value.get(BusinessProtectorMetaData(1)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        }
      }
    }
  }
}
