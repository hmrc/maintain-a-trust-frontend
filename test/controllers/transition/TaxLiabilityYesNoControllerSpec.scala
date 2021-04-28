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

package controllers.transition

import base.SpecBase
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.UTR
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.transition.TaxLiabilityYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HttpResponse
import utils.TestUserAnswers.utr
import views.html.transition.TaxLiabilityYesNoView

import scala.concurrent.Future

class TaxLiabilityYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new YesNoFormProvider()
  val form: Form[Boolean] = formProvider.withPrefix("taxLiabilityYesNo")

  lazy val taxLiabilityYesNoRoute: String = routes.TaxLiabilityYesNoController.onPageLoad().url

  "TaxLiabilityYesNoController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, taxLiabilityYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[TaxLiabilityYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, utr, UTR)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(TaxLiabilityYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, taxLiabilityYesNoRoute)

      val view = application.injector.instanceOf[TaxLiabilityYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), utr, UTR)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when true is submitted, marking this as taxable" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustsConnector = mock[TrustConnector]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockTrustsConnector.removeTransforms(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockTrustsConnector.setExpressTrust(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .build()

      val request = FakeRequest(POST, taxLiabilityYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BeforeYouContinueToTaxableController.onPageLoad().url

      verify(mockTrustsConnector).setExpressTrust(any(), eqTo(true))(any(), any())

      application.stop()
    }

    "redirect to the previous page when false is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustsConnector = mock[TrustConnector]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockTrustsConnector.removeTransforms(any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockTrustsConnector.setExpressTrust(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .build()

      val request = FakeRequest(POST, taxLiabilityYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WhatIsNextController.onPageLoad().url

      verify(mockTrustsConnector, times(0)).setExpressTrust(any(), eqTo(true))(any(), any())

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(POST, taxLiabilityYesNoRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[TaxLiabilityYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, utr, UTR)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, taxLiabilityYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
