/*
 * Copyright 2025 HM Revenue & Customs
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

import models.errors.TrustErrors
import models.http.DisplayTrustUnidentifiedType
import models.{MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.classOfBeneficiary._

class ClassOfBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustUnidentifiedType] {

  override def metaDataPage(index: Int): QuestionPage[MetaData] = ClassOfBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = ClassOfBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = ClassOfBeneficiaryShareOfIncomePage(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustUnidentifiedType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(ClassOfBeneficiaryDescriptionPage(index), entity.description))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
  }

  override def bpMatchStatus(entity: DisplayTrustUnidentifiedType): Option[String] = {
    entity.bpMatchStatus.fold(Some("98"))(Some(_))
  }
}
