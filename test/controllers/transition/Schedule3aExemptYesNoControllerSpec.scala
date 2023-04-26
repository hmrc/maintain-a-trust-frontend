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

package controllers.transition

import base.SpecBase
import cats.data.EitherT
import connectors.TrustConnector
import forms.YesNoFormProvider
import models.errors.{ServerError, TrustErrors}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.trustdetails.Schedule3aExemptYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.http.HttpResponse
import views.html.transition.Schedule3aExemptYesNoView

import scala.concurrent.Future

class Schedule3aExemptYesNoControllerSpec extends SpecBase {

  private val form: Form[Boolean] = new YesNoFormProvider().withPrefix("schedule3aExemptYesNo")

  private lazy val schedule3aExemptYesNoRoute: String = routes.Schedule3aExemptYesNoController.onPageLoad().url

  "Schedule3aExemptYesNoController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, schedule3aExemptYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[Schedule3aExemptYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswersForUtr.set(Schedule3aExemptYesNoPage, true).value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, schedule3aExemptYesNoRoute)

      val view = application.injector.instanceOf[Schedule3aExemptYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true))(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockTrustsConnector = mock[TrustConnector]

      when(mockTrustsConnector.setSchedule3aExempt(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr), affinityGroup = Organisation)
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .build()

      val request =
        FakeRequest(POST, schedule3aExemptYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      val mockTrustsConnector = mock[TrustConnector]

      when(mockTrustsConnector.setSchedule3aExempt(any(), any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Left(ServerError()))))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr), affinityGroup = Organisation)
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .build()

      val request =
        FakeRequest(POST, schedule3aExemptYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request =
        FakeRequest(POST, schedule3aExemptYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[Schedule3aExemptYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, schedule3aExemptYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, schedule3aExemptYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
