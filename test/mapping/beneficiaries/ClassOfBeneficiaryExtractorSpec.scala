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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import models.MetaData
import models.http.DisplayTrustUnidentifiedType
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.beneficiaries.classOfBeneficiary._

class ClassOfBeneficiaryExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateClassOfBeneficiary(index: Int) = DisplayTrustUnidentifiedType(
    lineNo = Some(s"$index"),
    bpMatchStatus = index match {
      case 0 => Some("01")
      case _ => None
    },
    description = s"Class Of Beneficiary $index",
    beneficiaryDiscretion = index match {
      case 0 => Some(false)
      case _ => None
    },
    beneficiaryShareOfIncome = index match {
      case 0 => Some("98")
      case _ => None
    },
    entityStart = "2019-11-26"
  )

  val classOfBeneficiaryExtractor : ClassOfBeneficiaryExtractor =
    injector.instanceOf[ClassOfBeneficiaryExtractor]

  "Class Of Beneficiary Extractor" - {

    "when no classes of beneficiaries" - {

      "must return user answers" in {

        val classesOfBeneficiaries = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = classOfBeneficiaryExtractor.extract(ua, classesOfBeneficiaries)

        extraction mustBe Symbol("left")

      }

    }

    "when there are charities" - {

      "with minimum data must return user answers updated" in {
        val classOfBeneficiary = List(DisplayTrustUnidentifiedType(
          lineNo = Some("1"),
          bpMatchStatus = Some("01"),
          description = s"Class Of Beneficiary 1",
          beneficiaryDiscretion = None,
          beneficiaryShareOfIncome = None,
          entityStart = "2019-11-26"
        ))

        val ua = emptyUserAnswersForUtr

        val extraction = classOfBeneficiaryExtractor.extract(ua, classOfBeneficiary)

        extraction.value.get(ClassOfBeneficiaryDescriptionPage(0)).get mustBe "Class Of Beneficiary 1"
        extraction.value.get(ClassOfBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.value.get(ClassOfBeneficiaryDiscretionYesNoPage(0)).get mustBe true
        extraction.value.get(ClassOfBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
      }

      "with full data must return user answers updated" in {
        val charities = (for(index <- 0 to 2) yield generateClassOfBeneficiary(index)).toList

        val ua = emptyUserAnswersForUtr

        val extraction = classOfBeneficiaryExtractor.extract(ua, charities)

        extraction mustBe Symbol("right")

        extraction.value.get(ClassOfBeneficiaryDescriptionPage(0)).get mustBe "Class Of Beneficiary 0"
        extraction.value.get(ClassOfBeneficiaryDescriptionPage(1)).get mustBe "Class Of Beneficiary 1"
        extraction.value.get(ClassOfBeneficiaryDescriptionPage(2)).get mustBe "Class Of Beneficiary 2"

        extraction.value.get(ClassOfBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
        extraction.value.get(ClassOfBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("98"), "2019-11-26")
        extraction.value.get(ClassOfBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("98"), "2019-11-26")

        extraction.value.get(ClassOfBeneficiaryDiscretionYesNoPage(0)).get mustBe false
        extraction.value.get(ClassOfBeneficiaryDiscretionYesNoPage(1)).get mustBe true
        extraction.value.get(ClassOfBeneficiaryDiscretionYesNoPage(2)).get mustBe true

        extraction.value.get(ClassOfBeneficiaryShareOfIncomePage(0)).get mustBe "98"
        extraction.value.get(ClassOfBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
        extraction.value.get(ClassOfBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
      }

    }

  }

}
