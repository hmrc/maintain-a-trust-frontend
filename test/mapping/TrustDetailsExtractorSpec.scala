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

package mapping

import base.SpecBaseHelpers
import generators.Generators
import models.http.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType}
import models.pages.DeedOfVariation.{AdditionToWill, ReplacedWill}
import models.pages.NonResidentType
import models.pages.NonResidentType.Domiciled
import models.pages.TrusteesBased._
import models.pages.TypeOfTrust.{DeedOfVariation, WillTrustOrIntestacyTrust}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.trustdetails._
import java.time.Month._

import java.time.LocalDate

class TrustDetailsExtractorSpec extends AnyFreeSpec with Matchers with EitherValues with Generators with SpecBaseHelpers {

  private val (year2018, year2019, num1, num20) = (2018, 2019, 1, 20)

  val trusteeDetailsExtractor: TrustDetailsExtractor =
    injector.instanceOf[TrustDetailsExtractor]

  "Trust Details Extractor" - {

    "when there is trust details" - {

      "for a taxable trust" - {

        "uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(false),
            trustUKResident = Some(true),
            trustUKProperty = Some(true),
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = Some(ResidentialStatusType(Some(UkType(scottishLaw = true, None)), None)),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(year2018, APRIL, num20)),
            trustRecorded = Some(true),
            trustUKRelation = None
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe false
          extraction.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
          extraction.value.get(GovernedInsideTheUKPage).get mustBe true
          extraction.value.get(CountryGoverningTrustPage) must not be defined
          extraction.value.get(AdministrationInsideUKPage).get mustBe true
          extraction.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe true
          extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe true
          extraction.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.value.get(TrustResidentOffshorePage).get mustBe false
          extraction.value.get(TrustPreviouslyResidentPage) must not be defined
        }

        "non uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(false),
            trustUKResident = Some(false),
            trustUKProperty = Some(false),
            lawCountry = Some("FR"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(
              ResidentialStatusType(
                None,
                Some(NonUKType(sch5atcgga92 = false, Some(false), Some(true), Some(NonResidentType.toDES(Domiciled))))
              )
            ),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(year2018, APRIL, num20)),
            trustRecorded = Some(false),
            trustUKRelation = Some(true)
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe false
          extraction.value.get(WhereTrusteesBasedPage).get mustBe NoTrusteesUkBased
          extraction.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.value.get(CountryGoverningTrustPage).get mustBe "FR"
          extraction.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.value.get(TrustHasBusinessRelationshipInUkYesNoPage).get mustBe true
          extraction.value.get(RegisteringTrustFor5APage).get mustBe false
          extraction.value.get(NonResidentTypePage).get mustBe Domiciled
          extraction.value.get(InheritanceTaxActPage).get mustBe false
          extraction.value.get(AgentOtherThanBarristerPage).get mustBe true

        }
      }

      "for a non taxable trust" - {

        "uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(true),
            trustUKResident = Some(true),
            trustUKProperty = Some(true),
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = Some(ResidentialStatusType(Some(UkType(scottishLaw = true, None)), None)),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(year2018, APRIL, num20)),
            trustRecorded = Some(true),
            trustUKRelation = None
          )

          val ua = emptyUserAnswersForUrn

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.value.get(WhereTrusteesBasedPage) must not be defined
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe true
          extraction.value.get(GovernedInsideTheUKPage) must not be defined
          extraction.value.get(CountryGoverningTrustPage) must not be defined
          extraction.value.get(AdministrationInsideUKPage) must not be defined
          extraction.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.value.get(EstablishedUnderScotsLawPage) must not be defined
          extraction.value.get(TrustResidentOffshorePage) must not be defined
          extraction.value.get(TrustPreviouslyResidentPage) must not be defined
        }

        "non uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(true),
            trustUKResident = Some(false),
            trustUKProperty = Some(false),
            lawCountry = Some("FR"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(
              ResidentialStatusType(
                None, Some(NonUKType(sch5atcgga92 = false, Some(false), Some(true), Some(NonResidentType.toDES(Domiciled))))
              )
            ),
            typeOfTrust = Some(DeedOfVariation),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(year2018, APRIL, num20)),
            trustRecorded = Some(false),
            trustUKRelation = Some(true)
          )

          val ua = emptyUserAnswersForUrn

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.value.get(WhereTrusteesBasedPage) must not be defined
          extraction.value.get(GovernedInsideTheUKPage) must not be defined
          extraction.value.get(CountryGoverningTrustPage) must not be defined
          extraction.value.get(AdministrationInsideUKPage) must not be defined
          extraction.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.value.get(TrustHasBusinessRelationshipInUkYesNoPage).get mustBe true
          extraction.value.get(RegisteringTrustFor5APage) must not be defined
          extraction.value.get(NonResidentTypePage) must not be defined
          extraction.value.get(InheritanceTaxActPage) must not be defined
          extraction.value.get(AgentOtherThanBarristerPage) must not be defined

        }
      }

      "migrating from non-taxable to taxable" - {

        "trustees based in the UK" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(true),
            trustUKResident = Some(true),
            trustUKProperty = Some(false),
            lawCountry = Some("ES"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(ResidentialStatusType(Some(UkType(scottishLaw = true, Some("DE"))), None)),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(AdditionToWill),
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = Some(false),
            trustUKRelation = None,
            schedule3aExempt = Some(true)
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
          extraction.value.get(SettlorsUkBasedPage) must not be defined
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.value.get(CountryGoverningTrustPage).get mustBe "ES"
          extraction.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.value.get(TrustResidentOffshorePage).get mustBe true
          extraction.value.get(TrustPreviouslyResidentPage).get mustBe "DE"
          extraction.value.get(RegisteringTrustFor5APage) must not be defined
          extraction.value.get(Schedule3aExemptYesNoPage).get mustBe true
        }

        "some trustees based in the UK" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(year2019, JUNE, num1),
            trustTaxable = Some(true),
            expressTrust = Some(true),
            trustUKResident = Some(true),
            trustUKProperty = Some(false),
            lawCountry = Some("ES"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(ResidentialStatusType(Some(UkType(scottishLaw = true, Some("DE"))), None)),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(AdditionToWill),
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = Some(false),
            trustUKRelation = None,
            settlorsUkBased = Some(true),
            schedule3aExempt = Some(false)
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
          extraction.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.value.get(WhereTrusteesBasedPage).get mustBe InternationalAndUkBasedTrustees
          extraction.value.get(SettlorsUkBasedPage).get mustBe true
          extraction.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.value.get(CountryGoverningTrustPage).get mustBe "ES"
          extraction.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.value.get(TrustResidentOffshorePage).get mustBe true
          extraction.value.get(TrustPreviouslyResidentPage).get mustBe "DE"
          extraction.value.get(RegisteringTrustFor5APage) must not be defined
          extraction.value.get(Schedule3aExemptYesNoPage).get mustBe false
        }

      }

      "assume taxable if trustTaxable is not defined" in {

        val trust = TrustDetailsType(
          startDate = LocalDate.of(year2019, JUNE, num1),
          trustTaxable = None,
          expressTrust = Some(false),
          trustUKResident = Some(true),
          trustUKProperty = Some(true),
          lawCountry = None,
          administrationCountry = None,
          residentialStatus = Some(ResidentialStatusType(Some(UkType(scottishLaw = true, None)), None)),
          typeOfTrust = Some(WillTrustOrIntestacyTrust),
          deedOfVariation = Some(ReplacedWill),
          interVivos = Some(true),
          efrbsStartDate = Some(LocalDate.of(year2018, APRIL, num20)),
          trustRecorded = None,
          trustUKRelation = None
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trusteeDetailsExtractor.extract(ua, trust)

        extraction.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(year2019, JUNE, num1)
        extraction.value.get(TrustTaxableYesNoPage).get mustBe true
        extraction.value.get(ExpressTrustYesNoPage).get mustBe false
        extraction.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
        extraction.value.get(GovernedInsideTheUKPage).get mustBe true
        extraction.value.get(CountryGoverningTrustPage) must not be defined
        extraction.value.get(AdministrationInsideUKPage).get mustBe true
        extraction.value.get(CountryAdministeringTrustPage) must not be defined
        extraction.value.get(TrustUkPropertyYesNoPage).get mustBe true
        extraction.value.get(TrustRecordedOnAnotherRegisterYesNoPage) must not be defined
        extraction.value.get(EstablishedUnderScotsLawPage).get mustBe true
        extraction.value.get(TrustResidentOffshorePage).get mustBe false
        extraction.value.get(TrustPreviouslyResidentPage) must not be defined
      }

    }

  }

}
