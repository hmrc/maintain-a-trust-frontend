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

package pages

import models.UserAnswers
import models.pages.WhatIsNext
import pages.declaration._
import pages.makechanges._
import pages.transition.NeedToPayTaxYesNoPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.libs.json.JsPath

import scala.util.Try

case object WhatIsNextPage extends QuestionPage[WhatIsNext] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "whatIsNext"

  override def cleanup(value: Option[WhatIsNext], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case _ =>
        removeDeclarationData(userAnswers)
        .flatMap(answers => removeMakeChangesData(answers))
        .flatMap(answers => removeCloseTrustData(answers))
        .flatMap(answers => removeTransitionData(answers))
    }
  }

  private def removeDeclarationData(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(AgencyRegisteredAddressUkYesNoPage)
      .flatMap(_.remove(AgencyRegisteredAddressUkPage))
      .flatMap(_.remove(AgencyRegisteredAddressInternationalPage))
      .flatMap(_.remove(AgentDeclarationPage))
      .flatMap(_.remove(IndividualDeclarationPage))
  }

  private def removeMakeChangesData(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(UpdateTrustDetailsYesNoPage)
      .flatMap(_.remove(UpdateTrusteesYesNoPage))
      .flatMap(_.remove(UpdateBeneficiariesYesNoPage))
      .flatMap(_.remove(UpdateSettlorsYesNoPage))
      .flatMap(_.remove(AddOrUpdateProtectorYesNoPage))
      .flatMap(_.remove(AddOrUpdateOtherIndividualsYesNoPage))
      .flatMap(_.remove(AddOrUpdateNonEeaCompanyYesNoPage))
  }

  private def removeCloseTrustData(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.deleteAtPath(pages.close.basePath)
  }

  private def removeTransitionData(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(NeedToPayTaxYesNoPage)
      .flatMap(_.remove(ExpressTrustYesNoPage))
  }
}
