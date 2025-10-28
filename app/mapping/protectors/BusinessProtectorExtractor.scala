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

package mapping.protectors

import models.errors.TrustErrors
import models.http.DisplayTrustProtectorBusiness
import models.pages.IndividualOrBusiness
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.protectors._
import pages.protectors.business._

class BusinessProtectorExtractor extends ProtectorPlaybackExtractor[DisplayTrustProtectorBusiness] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = BusinessProtectorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = BusinessProtectorAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = BusinessProtectorAddressPage(index)

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = BusinessProtectorUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = BusinessProtectorUtrPage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = BusinessProtectorCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = BusinessProtectorCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = BusinessProtectorCountryOfResidencePage(index)

  override def metaDataPage(index: Int): QuestionPage[MetaData] = BusinessProtectorMetaData(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustProtectorBusiness,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(BusinessProtectorNamePage(index), entity.name))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(_.set(BusinessProtectorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
  }
}
