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

import models.http.DisplayTrustUnidentifiedType
import models.{MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.classOfBeneficiary._

import scala.util.Try

class ClassOfBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustUnidentifiedType] {

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = ClassOfBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = ClassOfBeneficiaryShareOfIncomePage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustUnidentifiedType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(ClassOfBeneficiaryDescriptionPage(index), entity.description))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap {
        _.set(
          ClassOfBeneficiaryMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus.fold(Some("98"))(x => Some(x)),
            entityStart = entity.entityStart
          )
        )
      }
  }
}
