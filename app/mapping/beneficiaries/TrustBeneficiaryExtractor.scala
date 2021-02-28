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

import models.http._
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.trust._

import scala.util.Try

class TrustBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustBeneficiaryTrustType] {

  override def metaDataPage(index: Int): QuestionPage[MetaData] = TrustBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = TrustBeneficiaryShareOfIncomePage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = TrustBeneficiaryAddressPage(index)

  override def utrPage(index: Int): QuestionPage[String] = TrustBeneficiaryUtrPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustBeneficiaryTrustType,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(TrustBeneficiaryNamePage(index), entity.organisationName))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(_.set(TrustBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
      .flatMap(answers => extractMetaData(entity, index, answers))
  }
}
