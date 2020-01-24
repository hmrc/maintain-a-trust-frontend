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

package controllers

import base.SpecBase
import models.{UKAddress, UserAnswers}
import pages.beneficiaries.charity._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.PrintPlaybackHelper
import views.html.PlaybackAnswersView

class PlaybackAnswersControllerSpec extends SpecBase {

  val index = 0

  "PlaybackAnswersController" must {

    "return OK and the correct view for a GET" in {

      val playbackAnswers = UserAnswers("internalId")
        .set(CharityBeneficiaryNamePage(0), "Charity Beneficiary 1").success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(0), true).success.value
        .set(CharityBeneficiaryShareOfIncomePage(0), "10").success.value
        .set(CharityBeneficiaryAddressYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressUKYesNoPage(0), true).success.value
        .set(CharityBeneficiaryAddressPage(0), UKAddress("line1", "line2", None, None, "NE11NE")).success.value

        .set(CharityBeneficiaryNamePage(1), "Charity Beneficiary 2").success.value
        .set(CharityBeneficiaryDiscretionYesNoPage(0), false).success.value
        .set(CharityBeneficiaryAddressYesNoPage(0), false).success.value

      val entities = injector.instanceOf[PrintPlaybackHelper].entities(playbackAnswers)

      val trustDetails = injector.instanceOf[PrintPlaybackHelper].trustDetails(playbackAnswers)

      val application = applicationBuilder(Some(playbackAnswers)).build()

      val request = FakeRequest(GET, controllers.routes.PlaybackAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PlaybackAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(entities, trustDetails)(fakeRequest, messages).toString

      application.stop()
    }

  }

}
