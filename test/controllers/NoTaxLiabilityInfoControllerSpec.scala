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

package controllers

import base.SpecBase
import models.UTR
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NoTaxLiabilityInfoView

class NoTaxLiabilityInfoControllerSpec extends SpecBase {

  "NoTaxLiabilityInfo Controller" must {

    "return OK and the correct view for a GET" in {
      val answers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.NoTaxLiabilityInfoController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[NoTaxLiabilityInfoView]

      val utr = "1234567890"

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, UTR)(request, messages).toString

      application.stop()
    }
  }

  ".onSubmit" must {
    "redirect to NoTaxLiabilityInfo " in {

      val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, routes.NoTaxLiabilityInfoController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WhatIsNextController.onPageLoad().url

      application.stop()

    }
  }
}
