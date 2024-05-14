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

package models

import _root_.pages.makechanges._

case class UpdateFilterQuestions(trustDetails: Boolean,
                                 trustees: Boolean,
                                 beneficiaries: Boolean,
                                 settlors: Boolean,
                                 protectors: Boolean,
                                 otherIndividuals: Boolean,
                                 nonEeaCompany: Boolean)

object UpdateFilterQuestions {

  def from(userAnswers: UserAnswers): Option[UpdateFilterQuestions] = {
    for {
      trustDetails <- userAnswers.getWithDefault(UpdateTrustDetailsYesNoPage, false)
      trustees <- userAnswers.get(UpdateTrusteesYesNoPage)
      beneficiaries <- userAnswers.get(UpdateBeneficiariesYesNoPage)
      settlors <- userAnswers.get(UpdateSettlorsYesNoPage)
      protectors <- userAnswers.get(AddOrUpdateProtectorYesNoPage)
      otherIndividuals <- userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage)
      nonEeaCompanies <- userAnswers.getWithDefault(AddOrUpdateNonEeaCompanyYesNoPage, false)
    } yield {
      UpdateFilterQuestions(trustDetails, trustees, beneficiaries, settlors, protectors, otherIndividuals, nonEeaCompanies)
    }
  }

}
