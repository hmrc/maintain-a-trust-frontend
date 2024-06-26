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

package controllers.transition

import base.SpecBase
import cats.data.EitherT
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.UTR
import models.errors.{MongoError, ServerError, TrustErrors}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.transition.NeedToPayTaxYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.http.HttpResponse
import utils.TestUserAnswers.utr
import views.html.transition.NeedToPayTaxYesNoView

import scala.concurrent.Future

class NeedToPayTaxYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form: Form[Boolean] = formProvider.withPrefix("needToPayTaxYesNo")

  private lazy val needToPayTaxYesNoRoute: String = routes.NeedToPayTaxYesNoController.onPageLoad().url

  "NeedToPayTaxYesNoController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, needToPayTaxYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[NeedToPayTaxYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, utr, UTR)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(NeedToPayTaxYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, needToPayTaxYesNoRoute)

      val view = application.injector.instanceOf[NeedToPayTaxYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), utr, UTR)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when true is submitted, marking this as taxable" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustConnector.setTaxableTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BeforeYouContinueToTaxableController.onPageLoad().url

      verify(mockMaintainATrustService, times(0)).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustConnector).setTaxableTrust(any(), eqTo(true))(any(), any())

      application.stop()
    }

    "redirect to the next page when true is submitted and this doesn't match the previous answer" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustConnector.setTaxableTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val userAnswers = emptyUserAnswersForUtr
        .set(NeedToPayTaxYesNoPage, false).value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BeforeYouContinueToTaxableController.onPageLoad().url

      verify(mockMaintainATrustService, times(0)).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustConnector).setTaxableTrust(any(), eqTo(true))(any(), any())

      application.stop()
    }

    "redirect to the next page when true is submitted and this matches the previous answer" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustConnector.setTaxableTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val userAnswers = emptyUserAnswersForUtr
        .set(NeedToPayTaxYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BeforeYouContinueToTaxableController.onPageLoad().url

      verify(mockMaintainATrustService, times(0)).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustConnector, times(0)).setTaxableTrust(any(), eqTo(true))(any(), any())

      application.stop()
    }

    "redirect to the previous page when false is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustConnector.setTaxableTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.WhatIsNextController.onPageLoad().url

      verify(mockMaintainATrustService).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustConnector, times(0)).setTaxableTrust(any(), any())(any(), any())

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[NeedToPayTaxYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, utr, UTR)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      val mockTrustConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(MongoError))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustConnector.setTaxableTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Left(ServerError()))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, needToPayTaxYesNoRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
