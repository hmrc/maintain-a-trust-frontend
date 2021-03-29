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

package controllers.close.taxable

import base.SpecBase
import connectors.TrustConnector
import forms.DateFormProvider
import models.UserAnswers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.close.taxable.DateLastAssetSharedOutPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestUserAnswers
import views.html.close.taxable.DateLastAssetSharedOutView

import java.time.LocalDate
import scala.concurrent.Future

class DateLastAssetSharedOutControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new DateFormProvider()

  val trustStartDate: LocalDate = LocalDate.parse("2019-02-03")

  private def form: Form[LocalDate] = formProvider.withPrefixAndTrustStartDate("dateLastAssetSharedOut", trustStartDate)

  val fakeConnector : TrustConnector = mock[TrustConnector]

  val validAnswer: LocalDate = LocalDate.parse("2019-02-04")

  lazy val dateLastAssetSharedOutRoute: String = routes.DateLastAssetSharedOutController.onPageLoad().url

  override val emptyUserAnswersForUtr: UserAnswers = TestUserAnswers.emptyUserAnswersForUtr

  def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateLastAssetSharedOutRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateLastAssetSharedOutRoute)
      .withFormUrlEncodedBody(
        "value.day" -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year" -> validAnswer.getYear.toString
      )

  "DateLastAssetSharedOut Controller" must {

    "return OK and the correct view for a GET" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(
          bind[TrustConnector].toInstance(fakeConnector)
        )
        .build()

      val result = route(application, getRequest).value

      val view = application.injector.instanceOf[DateLastAssetSharedOutView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(getRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val userAnswers = emptyUserAnswersForUtr
        .set(DateLastAssetSharedOutPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustConnector].toInstance(fakeConnector)
        ).build()

      val view = application.injector.instanceOf[DateLastAssetSharedOutView]

      val result = route(application, getRequest).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer))(getRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" when {

      "4mld" in {

        val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false, isTrustTaxable = true)

        when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

        val application = applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[TrustConnector].toInstance(fakeConnector)
          ).build()

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

        application.stop()
      }

      "5mld" when {

        "underlying data is 4mld" in {

          val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)

          when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

          val application = applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(
              bind[TrustConnector].toInstance(fakeConnector)
            ).build()

          val result = route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

          application.stop()
        }

        "underlying data is 5mld" when {

          "taxable" in {

            val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
              .set(ExpressTrustYesNoPage, true).success.value

            when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

            val application = applicationBuilder(userAnswers = Some(baseAnswers))
              .overrides(
                bind[TrustConnector].toInstance(fakeConnector)
              ).build()

            val result = route(application, postRequest()).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad().url

            application.stop()
          }

          "non-taxable" in {

            val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = false)
              .set(ExpressTrustYesNoPage, true).success.value

            when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

            val application = applicationBuilder(userAnswers = Some(baseAnswers))
              .overrides(
                bind[TrustConnector].toInstance(fakeConnector)
              ).build()

            val result = route(application, postRequest()).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

            application.stop()
          }
        }
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      when(fakeConnector.getStartDate(any())(any(), any())).thenReturn(Future.successful(trustStartDate))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(
          bind[TrustConnector].toInstance(fakeConnector)
        ).build()

      val request =
        FakeRequest(POST, dateLastAssetSharedOutRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[DateLastAssetSharedOutView]

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

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
