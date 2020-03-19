/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.make_changes

import base.SpecBase
import controllers.makechanges.MakeChangesRouter
import controllers.makechanges.MakeChangesRouter.{Declaration, UnableToDecide, TaskList, UnavailableSections}
import pages.makechanges.{AddOtherIndividualsYesNoPage, AddProtectorYesNoPage, UpdateBeneficiariesYesNoPage, UpdateSettlorsYesNoPage, UpdateTrusteesYesNoPage}

class MakeChangesRouterSpec extends SpecBase {

  "Make changes router" must {

    "send user to declaration when there are no updates to declare" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswers
        .set(UpdateTrusteesYesNoPage, false).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddProtectorYesNoPage, false).success.value
        .set(AddOtherIndividualsYesNoPage, false).success.value

      filter.decide(userAnswers) mustBe Declaration
    }

    "allow user to navigate to the task list when selection options that are available" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswers
          .set(UpdateTrusteesYesNoPage, true).success.value
          .set(UpdateBeneficiariesYesNoPage, true).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddProtectorYesNoPage, false).success.value
          .set(AddOtherIndividualsYesNoPage, false).success.value

      filter.decide(userAnswers) mustBe TaskList
    }

    "not allow user to navigate to the task list when selection options that are unavailable" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswers
        .set(UpdateTrusteesYesNoPage, true).success.value
        .set(UpdateBeneficiariesYesNoPage, true).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddProtectorYesNoPage, true).success.value
        .set(AddOtherIndividualsYesNoPage, false).success.value

      filter.decide(userAnswers) mustBe UnavailableSections
    }

    "return an error when there is a problem deciding" in {
      val filter = MakeChangesRouter

      val userAnswers = emptyUserAnswers

      filter.decide(userAnswers) mustBe UnableToDecide
    }

  }

}