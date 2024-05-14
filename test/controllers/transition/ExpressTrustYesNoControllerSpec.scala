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
import models.errors.{MongoError, ServerError, TrustErrors}
import models.pages.WhatIsNext.NeedsToPayTax
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsNextPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.http.HttpResponse
import views.html.transition.ExpressTrustYesNoView

import scala.concurrent.Future

class ExpressTrustYesNoControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new YesNoFormProvider()
  private val form: Form[Boolean] = formProvider.withPrefix("expressTrustYesNo")

  private lazy val expressTrustYesNoRoute: String = routes.ExpressTrustYesNoController.onPageLoad().url
  private val validAnswer = true

  "ExpressTrustYesNoController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, expressTrustYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ExpressTrustYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(ExpressTrustYesNoPage, validAnswer).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, expressTrustYesNoRoute)

      val view = application.injector.instanceOf[ExpressTrustYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer))(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustsConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustsConnector.setExpressTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, expressTrustYesNoRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ConfirmTrustTaxableController.onPageLoad().url

      verify(mockMaintainATrustService).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustsConnector).setExpressTrust(any(), eqTo(validAnswer))(any(), any())

      application.stop()
    }

    "redirect to the next page when valid data is submitted for the Non tax to Tax transition journey" in {

      val mockPlaybackRepository = mock[PlaybackRepository]
      val mockTrustsConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      when(mockPlaybackRepository.set(any()))
        .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Right(()))))

      when(mockTrustsConnector.setExpressTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr.set(WhatIsNextPage, NeedsToPayTax).value))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, expressTrustYesNoRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.tasklist.routes.TaskListController.onPageLoad().url

      verify(mockMaintainATrustService, times(0)).removeTransformsAndResetTaskList(any())(any(), any())
      verify(mockTrustsConnector).setExpressTrust(any(), eqTo(validAnswer))(any(), any())

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(POST, expressTrustYesNoRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ExpressTrustYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      val mockTrustsConnector = mock[TrustConnector]
      val mockMaintainATrustService = mock[MaintainATrustService]

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(MongoError))

      when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, Unit](Future.successful(Left(ServerError()))))

      when(mockTrustsConnector.setExpressTrust(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      val request = FakeRequest(POST, expressTrustYesNoRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, expressTrustYesNoRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
