/*
 * Copyright 2023 HM Revenue & Customs
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

import models.http.DisplayTrustCharityType
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.charity._

import scala.util.Try

class CharityBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustCharityType] {

  override def namePage(index: Int): QuestionPage[String] = CharityBeneficiaryNamePage(index)
  override def safeIdPage(index: Int): QuestionPage[String] = CharityBeneficiarySafeIdPage(index)
  override def metaDataPage(index: Int): QuestionPage[MetaData] = CharityBeneficiaryMetaData(index)

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = CharityBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = CharityBeneficiaryShareOfIncomePage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = CharityBeneficiaryCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = CharityBeneficiaryCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = CharityBeneficiaryCountryOfResidencePage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = CharityBeneficiaryAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = CharityBeneficiaryAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = CharityBeneficiaryAddressPage(index)

  override def utrPage(index: Int): QuestionPage[String] = CharityBeneficiaryUtrPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustCharityType,
                                 index: Int): Try[UserAnswers] = {
    updateUserAnswersForOrgBeneficiary(answers, entity, index)
  }
}
