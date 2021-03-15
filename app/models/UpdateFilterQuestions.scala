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

package models

import _root_.pages.makechanges._

case class UpdateFilterQuestions(trustees: Boolean,
                                 beneficiaries: Boolean,
                                 settlors: Boolean,
                                 protectors: Boolean,
                                 natural: Boolean,
                                 nonEeaCompany: Boolean)

object UpdateFilterQuestions {

  def from(userAnswers: UserAnswers): Option[UpdateFilterQuestions] = {
    for {
      t <- userAnswers.get(UpdateTrusteesYesNoPage)
      b <- userAnswers.get(UpdateBeneficiariesYesNoPage)
      s <- userAnswers.get(UpdateSettlorsYesNoPage)
      p <- userAnswers.get(AddOrUpdateProtectorYesNoPage)
      n <- userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage)
      e <- userAnswers.get(AddOrUpdateNonEeaCompanyYesNoPage)
    } yield {
      UpdateFilterQuestions(t, b, s, p, n, e)
    }
  }

}
