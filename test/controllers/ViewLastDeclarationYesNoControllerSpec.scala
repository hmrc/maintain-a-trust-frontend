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

package controllers

import base.SpecBase
import forms.YesNoFormProvider
import models.errors.ServerError
import models.{UTR, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.ViewLastDeclarationYesNoPage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ViewLastDeclarationYesNoView

class ViewLastDeclarationYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form: Form[Boolean] = formProvider.withPrefix("viewLastDeclarationYesNo")
  private val utr: String = "1234567890"

  private lazy val viewLastDeclarationYesNoRoute: String = routes.ViewLastDeclarationYesNoController.onPageLoad().url

  override val emptyUserAnswersForUtr: UserAnswers = super.emptyUserAnswersForUtr

  "ViewLastDeclarationYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, viewLastDeclarationYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ViewLastDeclarationYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, utr, UTR)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(ViewLastDeclarationYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, viewLastDeclarationYesNoRoute)

      val view = application.injector.instanceOf[ViewLastDeclarationYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), utr, UTR)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when YES is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, viewLastDeclarationYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.print.routes.PrintLastDeclaredAnswersController.onPageLoad().url

      application.stop()
    }

    "redirect to the next page when NO is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, viewLastDeclarationYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.WhatIsNextController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, viewLastDeclarationYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ViewLastDeclarationYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, utr, UTR)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(ServerError()))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, viewLastDeclarationYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, viewLastDeclarationYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
