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

package controllers.close.nontaxable

import base.SpecBase
import connectors.TrustConnector
import forms.DateFormProvider
import models.UserAnswers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.close.nontaxable.DateClosedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.close.nontaxable.DateClosedView

import java.time.LocalDate
import scala.concurrent.Future

class DateClosedControllerSpec extends SpecBase with MockitoSugar {

  val trustStartDate: LocalDate = LocalDate.parse("2019-02-03")

  private def form: Form[LocalDate] = new DateFormProvider().withPrefixAndTrustStartDate("dateClosed", trustStartDate)

  val fakeConnector: TrustConnector = mock[TrustConnector]

  val validAnswer: LocalDate = LocalDate.parse("2019-02-04")

  lazy val dateClosedRoute: String = routes.DateClosedController.onPageLoad().url

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateClosedRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, dateClosedRoute)
    .withFormUrlEncodedBody(
      "value.day" -> validAnswer.getDayOfMonth.toString,
      "value.month" -> validAnswer.getMonthValue.toString,
      "value.year" -> validAnswer.getYear.toString
    )

  "DateLastAssetSharedOutController" must {

    "return OK and the correct view for a GET" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUrn))
        .overrides(bind[TrustConnector].toInstance(fakeConnector))
        .build()

      val result = route(application, getRequest).value

      val view = application.injector.instanceOf[DateClosedView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(getRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val userAnswers = emptyUserAnswersForUrn
        .set(DateClosedPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(fakeConnector))
        .build()

      val view = application.injector.instanceOf[DateClosedView]

      val result = route(application, getRequest).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer))(getRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val baseAnswers: UserAnswers = emptyUserAnswersForUrn

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[TrustConnector].toInstance(fakeConnector))
        .build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.close.routes.BeforeClosingController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUrn))
        .overrides(bind[TrustConnector].toInstance(fakeConnector))
        .build()

      val request = FakeRequest(POST, dateClosedRoute)
        .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[DateClosedView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val result = route(application, postRequest()).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
