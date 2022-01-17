/*
 * Copyright 2022 HM Revenue & Customs
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

package mapping.trustees

import models.UserAnswers
import models.http.DisplayTrustTrusteeOrgType
import models.pages.IndividualOrBusiness
import models.pages.Tag.Completed
import pages.entitystatus.TrusteeStatus
import pages.trustees._

import scala.util.Try

class OrganisationTrusteeExtractor extends TrusteePlaybackExtractor[DisplayTrustTrusteeOrgType] {

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustTrusteeOrgType,
                                 index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), entity.name))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
      .flatMap(answers => extractTelephoneAndEmail(entity, index, answers))
      .flatMap(_.set(TrusteeSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(TrusteeStatus(index), Completed))
  }

  private def extractTelephoneAndEmail(entity: DisplayTrustTrusteeOrgType,
                               index: Int,
                               answers: UserAnswers): Try[UserAnswers] = {
    extractIfTaxableOrMigratingToTaxable(answers) {
      answers.set(TrusteeTelephoneNumberPage(index), entity.phoneNumber)
        .flatMap(_.set(TrusteeEmailPage(index), entity.email))
    }
  }
}
