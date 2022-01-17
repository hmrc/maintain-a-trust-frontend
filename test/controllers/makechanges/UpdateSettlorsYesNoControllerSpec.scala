/*
 * Copyright 2022 HM Revenue & Customs
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
import models.pages.WhatIsNext
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges.UpdateSettlorsYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.JsBoolean
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.UpdateSettlorsYesNoView

import scala.concurrent.Future

class UpdateSettlorsYesNoControllerSpec extends SpecBase {

  private lazy val updateSettlorsYesNoRoute: String = routes.UpdateSettlorsYesNoController.onPageLoad().url

  "UpdateSettlorsYesNoController" when {

    "making changes" must {

      val prefix: String = "updateSettlors"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateSettlorsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(UpdateSettlorsYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateSettlorsYesNoRoute)

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "redirect to the add a protector page when valid data is submitted and no protectors exist" in {


        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoProtectorsAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(false)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddProtectorYesNoController.onPageLoad().url

        application.stop()
      }

      "redirect to the update protectors page when valid data is submitted and no protectors exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoProtectorsAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(true)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateProtectorYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = false)(request, messages).toString

        application.stop()
      }
    }

    "closing" must {

      val prefix: String = "updateSettlorsClosing"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr
        .set(WhatIsNextPage, WhatIsNext.CloseTrust).success.value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateSettlorsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(UpdateSettlorsYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateSettlorsYesNoRoute)

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "redirect to the add a protector page when valid data is submitted and no protectors exist" in {


        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoProtectorsAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(false)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.AddProtectorYesNoController.onPageLoad().url

        application.stop()
      }

      "redirect to the update protectors page when valid data is submitted and no protectors exist" in {

        val mockTrustConnector = mock[TrustConnector]

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
          .build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

        when(mockTrustConnector.getDoProtectorsAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(true)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UpdateProtectorYesNoController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateSettlorsYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateSettlorsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, determinePrefix, closingTrust = true)(request, messages).toString

        application.stop()
      }
    }
  }
}
