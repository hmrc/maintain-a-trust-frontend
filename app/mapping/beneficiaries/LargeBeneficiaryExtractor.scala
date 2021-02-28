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

package mapping.beneficiaries

import models.HowManyBeneficiaries.{Over1, Over1001, Over101, Over201, Over501}
import models.http.DisplayTrustLargeType
import models.{Address, Description, HowManyBeneficiaries, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.large._

import scala.util.Try

class LargeBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustLargeType] {

  override def metaDataPage(index: Int): QuestionPage[MetaData] = LargeBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = LargeBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = LargeBeneficiaryShareOfIncomePage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = LargeBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = LargeBeneficiaryAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = LargeBeneficiaryAddressPage(index)

  override def utrPage(index: Int): QuestionPage[String] = LargeBeneficiaryUtrPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustLargeType,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(LargeBeneficiaryNamePage(index), entity.organisationName))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
      .flatMap(
        _.set(
          LargeBeneficiaryDescriptionPage(index),
          Description(
            entity.description,
            entity.description1,
            entity.description2,
            entity.description3,
            entity.description4
          )
        )
      )
      .flatMap(answers => extractNumberOfBeneficiaries(entity.numberOfBeneficiary, index, answers))
      .flatMap(_.set(LargeBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractMetaData(entity, index, answers))
  }

  private def extractNumberOfBeneficiaries(numberOfBeneficiary: String,
                                           index: Int,
                                           answers: UserAnswers): Try[UserAnswers] = {
    val setValue = (x: HowManyBeneficiaries) => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), x)
    numberOfBeneficiary.toInt match {
      case x if 0 to 100 contains x => setValue(Over1)
      case x if 101 to 200 contains x => setValue(Over101)
      case x if 201 to 500 contains x => setValue(Over201)
      case x if 501 to 999 contains x => setValue(Over501)
      case _ => setValue(Over1001)
    }
  }
}
