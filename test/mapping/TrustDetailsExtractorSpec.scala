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
import models.http.{NonUKType, ResidentialStatusType, TrustDetailsType, UkType}
import models.pages.DeedOfVariation.{AdditionToWill, ReplacedWill}
import models.pages.NonResidentType
import models.pages.NonResidentType.Domiciled
import models.pages.TrusteesBased._
import models.pages.TypeOfTrust.{DeedOfVariation, WillTrustOrIntestacyTrust}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.trustdetails._

import java.time.LocalDate

class TrustDetailsExtractorSpec extends FreeSpec with MustMatchers with EitherValues with Generators with SpecBaseHelpers {

  val trusteeDetailsExtractor: TrustDetailsExtractor =
    injector.instanceOf[TrustDetailsExtractor]

  "Trust Details Extractor" - {

    "when there is trust details" - {

      "for a taxable trust" - {

        "uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
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
            efrbsStartDate = Some(LocalDate.of(2018, 4, 20)),
            trustRecorded = Some(true),
            trustUKRelation = None
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe false
          extraction.right.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
          extraction.right.value.get(GovernedInsideTheUKPage).get mustBe true
          extraction.right.value.get(CountryGoverningTrustPage) must not be defined
          extraction.right.value.get(AdministrationInsideUKPage).get mustBe true
          extraction.right.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe true
          extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe true
          extraction.right.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.right.value.get(TrustResidentOffshorePage).get mustBe false
          extraction.right.value.get(TrustPreviouslyResidentPage) must not be defined
        }

        "non uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
            trustTaxable = Some(true),
            expressTrust = Some(false),
            trustUKResident = Some(false),
            trustUKProperty = Some(false),
            lawCountry = Some("FR"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(ResidentialStatusType(None, Some(NonUKType(sch5atcgga92 = false, Some(false), Some(true), Some(NonResidentType.toDES(Domiciled)))))),
            typeOfTrust = Some(WillTrustOrIntestacyTrust),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(2018, 4, 20)),
            trustRecorded = Some(false),
            trustUKRelation = Some(true)
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe false
          extraction.right.value.get(WhereTrusteesBasedPage).get mustBe NoTrusteesUkBased
          extraction.right.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.right.value.get(CountryGoverningTrustPage).get mustBe "FR"
          extraction.right.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.right.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.right.value.get(TrustHasBusinessRelationshipInUkYesNoPage).get mustBe true
          extraction.right.value.get(RegisteringTrustFor5APage).get mustBe false
          extraction.right.value.get(NonResidentTypePage).get mustBe Domiciled
          extraction.right.value.get(InheritanceTaxActPage).get mustBe false
          extraction.right.value.get(AgentOtherThanBarristerPage).get mustBe true

        }
      }

      "for a non taxable trust" - {

        "uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
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
            efrbsStartDate = Some(LocalDate.of(2018, 4, 20)),
            trustRecorded = Some(true),
            trustUKRelation = None
          )

          val ua = emptyUserAnswersForUrn

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.right.value.get(WhereTrusteesBasedPage) must not be defined
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe true
          extraction.right.value.get(GovernedInsideTheUKPage) must not be defined
          extraction.right.value.get(CountryGoverningTrustPage) must not be defined
          extraction.right.value.get(AdministrationInsideUKPage) must not be defined
          extraction.right.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.right.value.get(EstablishedUnderScotsLawPage) must not be defined
          extraction.right.value.get(TrustResidentOffshorePage) must not be defined
          extraction.right.value.get(TrustPreviouslyResidentPage) must not be defined
        }

        "non uk" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
            trustTaxable = Some(true),
            expressTrust = Some(true),
            trustUKResident = Some(false),
            trustUKProperty = Some(false),
            lawCountry = Some("FR"),
            administrationCountry = Some("IT"),
            residentialStatus = Some(ResidentialStatusType(None, Some(NonUKType(sch5atcgga92 = false, Some(false), Some(true), Some(NonResidentType.toDES(Domiciled)))))),
            typeOfTrust = Some(DeedOfVariation),
            deedOfVariation = Some(ReplacedWill),
            interVivos = Some(true),
            efrbsStartDate = Some(LocalDate.of(2018, 4, 20)),
            trustRecorded = Some(false),
            trustUKRelation = Some(true)
          )

          val ua = emptyUserAnswersForUrn

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.right.value.get(WhereTrusteesBasedPage) must not be defined
          extraction.right.value.get(GovernedInsideTheUKPage) must not be defined
          extraction.right.value.get(CountryGoverningTrustPage) must not be defined
          extraction.right.value.get(AdministrationInsideUKPage) must not be defined
          extraction.right.value.get(CountryAdministeringTrustPage) must not be defined
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.right.value.get(TrustHasBusinessRelationshipInUkYesNoPage).get mustBe true
          extraction.right.value.get(RegisteringTrustFor5APage) must not be defined
          extraction.right.value.get(NonResidentTypePage) must not be defined
          extraction.right.value.get(InheritanceTaxActPage) must not be defined
          extraction.right.value.get(AgentOtherThanBarristerPage) must not be defined

        }
      }

      "migrating from non-taxable to taxable" - {

        "trustees based in the UK" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
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
            trustUKRelation = None
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.right.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
          extraction.right.value.get(SettlorsUkBasedPage) must not be defined
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.right.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.right.value.get(CountryGoverningTrustPage).get mustBe "ES"
          extraction.right.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.right.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.right.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.right.value.get(TrustResidentOffshorePage).get mustBe true
          extraction.right.value.get(TrustPreviouslyResidentPage).get mustBe "DE"
          extraction.right.value.get(RegisteringTrustFor5APage) must not be defined
        }

        "some trustees based in the UK" in {

          val trust = TrustDetailsType(
            startDate = LocalDate.of(2019, 6, 1),
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
            settlorsUkBased = Some(true)
          )

          val ua = emptyUserAnswersForUtr

          val extraction = trusteeDetailsExtractor.extract(ua, trust)

          extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
          extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
          extraction.right.value.get(ExpressTrustYesNoPage).get mustBe true
          extraction.right.value.get(WhereTrusteesBasedPage).get mustBe InternationalAndUkBasedTrustees
          extraction.right.value.get(SettlorsUkBasedPage).get mustBe true
          extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe false
          extraction.right.value.get(GovernedInsideTheUKPage).get mustBe false
          extraction.right.value.get(CountryGoverningTrustPage).get mustBe "ES"
          extraction.right.value.get(AdministrationInsideUKPage).get mustBe false
          extraction.right.value.get(CountryAdministeringTrustPage).get mustBe "IT"
          extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage).get mustBe false
          extraction.right.value.get(EstablishedUnderScotsLawPage).get mustBe true
          extraction.right.value.get(TrustResidentOffshorePage).get mustBe true
          extraction.right.value.get(TrustPreviouslyResidentPage).get mustBe "DE"
          extraction.right.value.get(RegisteringTrustFor5APage) must not be defined
        }

      }

      "assume taxable if trustTaxable is not defined" in {

        val trust = TrustDetailsType(
          startDate = LocalDate.of(2019, 6, 1),
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
          efrbsStartDate = Some(LocalDate.of(2018, 4, 20)),
          trustRecorded = None,
          trustUKRelation = None
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trusteeDetailsExtractor.extract(ua, trust)

        extraction.right.value.get(WhenTrustSetupPage).get mustBe LocalDate.of(2019, 6, 1)
        extraction.right.value.get(TrustTaxableYesNoPage).get mustBe true
        extraction.right.value.get(ExpressTrustYesNoPage).get mustBe false
        extraction.right.value.get(WhereTrusteesBasedPage).get mustBe AllTrusteesUkBased
        extraction.right.value.get(GovernedInsideTheUKPage).get mustBe true
        extraction.right.value.get(CountryGoverningTrustPage) must not be defined
        extraction.right.value.get(AdministrationInsideUKPage).get mustBe true
        extraction.right.value.get(CountryAdministeringTrustPage) must not be defined
        extraction.right.value.get(TrustUkPropertyYesNoPage).get mustBe true
        extraction.right.value.get(TrustRecordedOnAnotherRegisterYesNoPage) must not be defined
        extraction.right.value.get(EstablishedUnderScotsLawPage).get mustBe true
        extraction.right.value.get(TrustResidentOffshorePage).get mustBe false
        extraction.right.value.get(TrustPreviouslyResidentPage) must not be defined
      }

    }

  }

}
