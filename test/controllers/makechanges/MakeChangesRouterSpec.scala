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

package controllers.makechanges

import base.SpecBase
import controllers.makechanges.MakeChangesRouter.{Declaration, TaskList, UnableToDecide}
import pages.makechanges._

class MakeChangesRouterSpec extends SpecBase {

  "Make changes router" must {

    "send user to declaration when there are no updates to declare" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswersForUtr
        .set(UpdateTrustDetailsYesNoPage, false).success.value
        .set(UpdateTrusteesYesNoPage, false).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value
        .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value
        .set(AddOrUpdateNonEeaCompanyYesNoPage, false).success.value

      filter.decide(userAnswers) mustBe Declaration
    }

    "allow user to navigate to the task list when selected options are available" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswersForUtr
        .set(UpdateTrustDetailsYesNoPage, false).success.value
        .set(UpdateTrusteesYesNoPage, true).success.value
        .set(UpdateBeneficiariesYesNoPage, true).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value
        .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value
        .set(AddOrUpdateNonEeaCompanyYesNoPage, false).success.value
      filter.decide(userAnswers) mustBe TaskList
    }

    "return an error when there is a problem deciding" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswersForUtr

      filter.decide(userAnswers) mustBe UnableToDecide
    }

  }

}
