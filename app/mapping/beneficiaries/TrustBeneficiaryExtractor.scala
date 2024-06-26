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

import models.errors.TrustErrors
import models.http._
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.trust._

class TrustBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustBeneficiaryTrustType] {

  override def namePage(index: Int): QuestionPage[String] = TrustBeneficiaryNamePage(index)
  override def safeIdPage(index: Int): QuestionPage[String] = TrustBeneficiarySafeIdPage(index)
  override def metaDataPage(index: Int): QuestionPage[MetaData] = TrustBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = TrustBeneficiaryShareOfIncomePage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = TrustBeneficiaryCountryOfResidencePage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = TrustBeneficiaryAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = TrustBeneficiaryAddressPage(index)

  override def utrPage(index: Int): QuestionPage[String] = TrustBeneficiaryUtrPage(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustBeneficiaryTrustType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    updateUserAnswersForOrgBeneficiary(answers, entity, index)
  }
}
