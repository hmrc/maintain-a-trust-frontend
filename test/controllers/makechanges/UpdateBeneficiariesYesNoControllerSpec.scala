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

package controllers.makechanges

import base.SpecBase
import forms.YesNoFormProvider
import models.UserAnswers
import models.errors.ServerError
import models.pages.WhatIsNext
import pages.WhatIsNextPage
import pages.makechanges.UpdateBeneficiariesYesNoPage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.UpdateBeneficiariesYesNoView

class UpdateBeneficiariesYesNoControllerSpec extends SpecBase {

  private lazy val updateBeneficiariesYesNoRoute: String = routes.UpdateBeneficiariesYesNoController.onPageLoad().url

  "UpdateBeneficiariesYesNoController" when {

    "making changes" must {

      val prefix: String = "updateBeneficiaries"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateBeneficiariesYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(UpdateBeneficiariesYesNoPage, true).value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateBeneficiariesYesNoRoute)

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateBeneficiariesYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateSettlorsYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateBeneficiariesYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }
    }

    "closing" must {

      val prefix: String = "updateBeneficiariesClosing"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.CloseTrust).value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateBeneficiariesYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(UpdateBeneficiariesYesNoPage, true).value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateBeneficiariesYesNoRoute)

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateBeneficiariesYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateSettlorsYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateBeneficiariesYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateBeneficiariesYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(ServerError()))

      val userAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, updateBeneficiariesYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

  }
}
