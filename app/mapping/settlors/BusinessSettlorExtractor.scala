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

package mapping.settlors

import models.UserAnswers
import models.errors.TrustErrors
import models.http.DisplayTrustSettlorCompany
import models.pages.IndividualOrBusiness
import models.pages.Tag.Completed
import pages.QuestionPage
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._

class BusinessSettlorExtractor extends SettlorPlaybackExtractor[DisplayTrustSettlorCompany] {

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = SettlorUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = SettlorUtrPage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = SettlorCountryOfResidencePage(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustSettlorCompany,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(SettlorBusinessNamePage(index), entity.name))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
      .flatMap(answers => extractSettlorCompanyTypeAndTime(entity, index, answers))
      .flatMap(_.set(SettlorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), Completed))
  }

  private def extractSettlorCompanyTypeAndTime(entity: DisplayTrustSettlorCompany, index: Int, answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      answers.set(SettlorCompanyTypePage(index), entity.companyType)
        .flatMap(_.set(SettlorCompanyTimePage(index), entity.companyTime))
    }
  }
}
