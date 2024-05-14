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
import cats.data.EitherT
import forms.YesNoFormProvider
import models.UserAnswers
import models.errors.{MongoError, TrustErrors}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ObligedEntityPdfYesNoPage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import views.html.ObligedEntityPdfYesNoView

import scala.concurrent.Future

class ObligedEntityPdfYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form: Form[Boolean] = formProvider.withPrefix("obligedEntityPdfYesNo")

  private val identifier = "1234567890"

  private lazy val obligedEntityPdfYesNoControllerRoute: String = routes.ObligedEntityPdfYesNoController.onPageLoad().url

  override val emptyUserAnswersForUtr: UserAnswers = super.emptyUserAnswersForUtr

  "ObligedEntityPdfYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, obligedEntityPdfYesNoControllerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ObligedEntityPdfYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, identifier)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(ObligedEntityPdfYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, obligedEntityPdfYesNoControllerRoute)

      val view = application.injector.instanceOf[ObligedEntityPdfYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), identifier)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when YES is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]

      when(mockPlaybackRepository.set(any())).thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, obligedEntityPdfYesNoControllerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.ObligedEntityPdfController.getPdf(emptyUserAnswersForUtr.identifier).url

      application.stop()
    }

    "redirect to the next page when NO is submitted" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, obligedEntityPdfYesNoControllerRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.WhatIsNextController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, obligedEntityPdfYesNoControllerRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ObligedEntityPdfYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, identifier)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(MongoError))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, obligedEntityPdfYesNoControllerRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, obligedEntityPdfYesNoControllerRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
