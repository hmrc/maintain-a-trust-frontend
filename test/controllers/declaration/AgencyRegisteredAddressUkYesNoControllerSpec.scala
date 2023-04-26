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

package controllers.declaration

import base.SpecBase
import forms.YesNoFormProvider
import models.UserAnswers
import models.errors.ServerError
import models.pages.WhatIsNext.MakeChanges
import pages.WhatIsNextPage
import pages.declaration.AgencyRegisteredAddressUkYesNoPage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.declaration.AgencyRegisteredAddressUkYesNoView

class AgencyRegisteredAddressUkYesNoControllerSpec extends SpecBase {

  private val formProvider = new YesNoFormProvider()
  private val form = formProvider.withPrefix("agencyRegisteredAddressUkYesNo")
  private lazy val agencyRegisteredAddressUkYesNoRoute = routes.AgencyRegisteredAddressUkYesNoController.onPageLoad().url
  private lazy val onSubmit: Call = routes.AgencyRegisteredAddressUkYesNoController.onSubmit()

  private val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, MakeChanges).value

  "AgencyRegisteredAddressUkYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressUkYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, onSubmit)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AgencyRegisteredAddressUkYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, agencyRegisteredAddressUkYesNoRoute)

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), onSubmit)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when Yes is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AgencyRegisteredAddressUkController.onPageLoad().url

      application.stop()
    }

    "redirect to the next page when No is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AgencyRegisteredAddressInternationalController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AgencyRegisteredAddressUkYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, onSubmit)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(ServerError()))

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, agencyRegisteredAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

  }
}
