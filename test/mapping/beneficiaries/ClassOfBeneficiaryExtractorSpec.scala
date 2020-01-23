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

package mapping.beneficiaries

import base.SpecBaseHelpers
import generators.Generators
import mapping.PlaybackExtractor
import models.{MetaData, UserAnswers}
import models.http.DisplayTrustUnidentifiedType
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.beneficiaries.classOfBeneficiary._

class ClassOfBeneficiaryExtractorSpec extends FreeSpec with MustMatchers
  with EitherValues with Generators with SpecBaseHelpers {

  def generateClassOfBeneficiary(index: Int) = DisplayTrustUnidentifiedType(
    lineNo = s"$index",
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

  val classOfBeneficiaryExtractor : PlaybackExtractor[Option[List[DisplayTrustUnidentifiedType]]] =
    injector.instanceOf[ClassOfBeneficiaryExtractor]

  "Class Of Beneficiary Extractor" - {

    "when no classes of beneficiaries" - {

      "must return user answers" in {

        val classesOfBeneficiaries = None

        val ua = UserAnswers("fakeId")

        val extraction = classOfBeneficiaryExtractor.extract(ua, classesOfBeneficiaries)

        extraction mustBe 'left

      }

    }

    "when there are charities" - {

      "with minimum data must return user answers updated" in {
        val classOfBeneficiary = List(DisplayTrustUnidentifiedType(
          lineNo = s"1",
          bpMatchStatus = Some("01"),
          description = s"Class Of Beneficiary 1",
          beneficiaryDiscretion = None,
          beneficiaryShareOfIncome = None,
          entityStart = "2019-11-26"
        ))

        val ua = UserAnswers("fakeId")

        val extraction = classOfBeneficiaryExtractor.extract(ua, Some(classOfBeneficiary))

        extraction.right.value.get(ClassOfBeneficiaryDescriptionPage(0)).get mustBe "Class Of Beneficiary 1"
        extraction.right.value.get(ClassOfBeneficiaryMetaData(0)).get mustBe MetaData("1", Some("01"), "2019-11-26")
        extraction.right.value.get(ClassOfBeneficiaryDiscretionYesNoPage(0)).get mustBe true
        extraction.right.value.get(ClassOfBeneficiaryShareOfIncomePage(0)) mustNot be(defined)
      }

      "with full data must return user answers updated" in {
        val charities = (for(index <- 0 to 2) yield generateClassOfBeneficiary(index)).toList

        val ua = UserAnswers("fakeId")

        val extraction = classOfBeneficiaryExtractor.extract(ua, Some(charities))

        extraction mustBe 'right

        extraction.right.value.get(ClassOfBeneficiaryDescriptionPage(0)).get mustBe "Class Of Beneficiary 0"
        extraction.right.value.get(ClassOfBeneficiaryDescriptionPage(1)).get mustBe "Class Of Beneficiary 1"
        extraction.right.value.get(ClassOfBeneficiaryDescriptionPage(2)).get mustBe "Class Of Beneficiary 2"

        extraction.right.value.get(ClassOfBeneficiaryMetaData(0)).get mustBe MetaData("0", Some("01"), "2019-11-26")
        extraction.right.value.get(ClassOfBeneficiaryMetaData(1)).get mustBe MetaData("1", Some("98"), "2019-11-26")
        extraction.right.value.get(ClassOfBeneficiaryMetaData(2)).get mustBe MetaData("2", Some("98"), "2019-11-26")

        extraction.right.value.get(ClassOfBeneficiaryDiscretionYesNoPage(0)).get mustBe false
        extraction.right.value.get(ClassOfBeneficiaryDiscretionYesNoPage(1)).get mustBe true
        extraction.right.value.get(ClassOfBeneficiaryDiscretionYesNoPage(2)).get mustBe true

        extraction.right.value.get(ClassOfBeneficiaryShareOfIncomePage(0)).get mustBe "98"
        extraction.right.value.get(ClassOfBeneficiaryShareOfIncomePage(1)) mustNot be(defined)
        extraction.right.value.get(ClassOfBeneficiaryShareOfIncomePage(2)) mustNot be(defined)
      }

    }

  }

}
