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
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.UserAnswers
import models.pages.WhatIsNext.MakeChanges
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges.AddOrUpdateProtectorYesNoPage
import play.api.inject.bind
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.AddProtectorYesNoView

import scala.concurrent.Future

class AddProtectorYesNoControllerSpec extends SpecBase {

  val formProvider = new YesNoFormProvider()
  val prefix: String = "addProtector"
  val form = formProvider.withPrefix(prefix)

  lazy val addProtectorYesNoRoute = routes.AddProtectorYesNoController.onPageLoad().url

  val utr = "utr"

  val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, MakeChanges).success.value

  "AddProtectorYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, addProtectorYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AddProtectorYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, prefix)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddOrUpdateProtectorYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, addProtectorYesNoRoute)

      val view = application.injector.instanceOf[AddProtectorYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), prefix)(request, messages).toString

      application.stop()
    }

    "redirect to the add an other individuals page when valid data is submitted and no individuals exist" in {

      val  mockTrustConnector = mock[TrustConnector]

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[TrustConnector].toInstance(mockTrustConnector)
          )
          .build()

      val request =
        FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
        .thenReturn(Future.successful(JsBoolean(false)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.AddOtherIndividualsYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to the update individuals page when valid data is submitted and individuals exist" in {

      val  mockTrustConnector = mock[TrustConnector]

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(
          bind[TrustConnector].toInstance(mockTrustConnector)
        )
        .build()

      val request =
        FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      when(mockTrustConnector.getDoOtherIndividualsAlreadyExist(any())(any(), any()))
        .thenReturn(Future.successful(JsBoolean(true)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.UpdateOtherIndividualsYesNoController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, addProtectorYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AddProtectorYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, prefix)(request, messages).toString

      application.stop()
    }

  }
}
