/*
 * Copyright 2022 HM Revenue & Customs
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
import models.http._
import models.pages.WhatIsNext.NeedsToPayTax
import models.pages.{DeedOfVariation, KindOfTrust, TypeOfTrust}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.WhatIsNextPage
import pages.settlors.living_settlor.trust_type._
import pages.trustdetails._

import java.time.LocalDate

class TrustTypeExtractorSpec extends AnyFreeSpec with Matchers with EitherValues with Generators with SpecBaseHelpers {

  val trustTypeExtractor: TrustTypeExtractor = injector.instanceOf[TrustTypeExtractor]
  
  "Trust Type Extractor" - {

    "when no trust type" - {

      "taxable" - {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = None,
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        "must throw error" in {

          val ua = emptyUserAnswersForUtr

          val extraction = trustTypeExtractor.extract(ua, trust)

          extraction mustBe 'left

        }
      }

      "non-taxable" - {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = Some(false),
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = None,
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = None
        )
        
        "must return user answers" in {

          val ua = emptyUserAnswersForUrn

          val extraction = trustTypeExtractor.extract(ua, trust)

          extraction.right.value mustBe ua

        }
      }

      "migrating from non-taxable to taxable" - {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = Some(false),
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = None,
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = None
        )

        "must return user answers" in {

          val ua = emptyUserAnswersForUrn
            .set(WhatIsNextPage, NeedsToPayTax).success.value

          val extraction = trustTypeExtractor.extract(ua, trust)

          extraction.right.value mustBe ua

        }
      }
    }

    "when there is a trust type of 'Will Trust or Intestacy Trust' in addition to a Will Trust" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.WillTrustOrIntestacyTrust),
            deedOfVariation = Some(DeedOfVariation.AdditionToWill),
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Deed
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage).get mustBe true
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Deed of Variation Trust or Family Arrangement' in addition to a Will Trust" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.DeedOfVariation),
            deedOfVariation = Some(DeedOfVariation.AdditionToWill),
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Deed
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage).get mustBe true
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Deed of Variation Trust or Family Arrangement' not in addition to a Will Trust" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.DeedOfVariation),
            deedOfVariation = Some(DeedOfVariation.ReplacedWill),
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Deed
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage).get mustBe false
        extraction.right.value.get(HowDeedOfVariationCreatedPage).get mustBe DeedOfVariation.ReplacedWill
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Inter vivos Settlement'" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.IntervivosSettlementTrust),
            deedOfVariation = None,
            interVivos = Some(true),
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Intervivos
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage).get mustBe true
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Employment Related'" - {

      "with efrbs start date must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.EmployeeRelated),
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = Some(LocalDate.parse("1970-02-01")),
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Employees
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage).get mustBe true
        extraction.right.value.get(EfrbsStartDatePage).get mustBe LocalDate.parse("1970-02-01")

      }

      "with no efrbs start date must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.EmployeeRelated),
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.Employees
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage).get mustBe false
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Flat Management Company or Sinking Fund'" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.FlatManagementTrust),
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.FlatManagement
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Heritage Maintenance Fund'" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.HeritageTrust),
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe false
        extraction.right.value.get(KindOfTrustPage).get mustBe KindOfTrust.HeritageMaintenanceFund
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

    "when there is a trust type of 'Will Trust or Intestacy Trust'" - {

      "with minimum data must return user answers updated" in {

        val trust = DisplayTrust(
          details = TrustDetailsType(
            startDate = LocalDate.parse("1970-02-01"),
            trustTaxable = None,
            expressTrust = None,
            trustUKResident = None,
            trustUKProperty = None,
            lawCountry = None,
            administrationCountry = None,
            residentialStatus = None,
            typeOfTrust = Some(TypeOfTrust.WillTrustOrIntestacyTrust),
            deedOfVariation = None,
            interVivos = None,
            efrbsStartDate = None,
            trustRecorded = None,
            trustUKRelation = None
          ),
          entities = DisplayTrustEntitiesType(
            None,
            DisplayTrustBeneficiaryType(Nil, Nil, Nil, Nil, Nil, Nil, Nil),
            None,
            DisplayTrustLeadTrusteeType(None, None),
            None,
            None,
            None
          ),
          assets = Some(DisplayTrustAssets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        )

        val ua = emptyUserAnswersForUtr

        val extraction = trustTypeExtractor.extract(ua, trust)

        extraction.right.value.get(SetUpAfterSettlorDiedYesNoPage).get mustBe true
        extraction.right.value.get(KindOfTrustPage) mustNot be(defined)
        extraction.right.value.get(SetUpInAdditionToWillTrustYesNoPage) mustNot be(defined)
        extraction.right.value.get(HowDeedOfVariationCreatedPage) mustNot be(defined)
        extraction.right.value.get(HoldoverReliefYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsYesNoPage) mustNot be(defined)
        extraction.right.value.get(EfrbsStartDatePage) mustNot be(defined)

      }

    }

  }
}
