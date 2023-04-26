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

package controllers.makechanges

import base.SpecBase
import cats.data.EitherT
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.UserAnswers
import models.errors.{MongoError, TrustErrors}
import models.pages.WhatIsNext
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges.AddOrUpdateProtectorYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.AddProtectorYesNoView

import scala.concurrent.Future

class AddProtectorYesNoControllerSpec extends SpecBase {

  private lazy val addProtectorYesNoRoute: String = routes.AddProtectorYesNoController.onPageLoad().url

  "AddProtectorYesNoController" when {

    "making changes" must {

      val prefix: String = "addProtector"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, addProtectorYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(AddOrUpdateProtectorYesNoPage, true).value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addProtectorYesNoRoute)

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "redirect to the add an other individuals page when valid data is submitted and no individuals exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(false)))))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddOtherIndividualsYesNoController.onPageLoad().url

        application.stop()
      }

      "redirect to the update individuals page when valid data is submitted and individuals exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(true)))))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateOtherIndividualsYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }
    }

    "closing" must {

      val prefix: String = "addProtectorClosing"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.CloseTrust).value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, addProtectorYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(AddOrUpdateProtectorYesNoPage, true).value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addProtectorYesNoRoute)

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "redirect to the add an other individuals page when valid data is submitted and no individuals exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(false)))))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddOtherIndividualsYesNoController.onPageLoad().url

        application.stop()
      }

      "redirect to the update individuals page when valid data is submitted and individuals exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(true)))))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateOtherIndividualsYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddProtectorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(MongoError))

      val userAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.CloseTrust).value

      val mockTrustConnector = mock[TrustConnector]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .build()

      val request = FakeRequest(POST, addProtectorYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(true)))))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }
  }
}
