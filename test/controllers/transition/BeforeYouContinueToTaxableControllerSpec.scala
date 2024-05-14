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
import models.errors.{ServerError, TrustErrors}
import models.{TrustDetails, UTR}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestUserAnswers.utr
import views.html.transition.BeforeYouContinueToTaxableView

import java.time.LocalDate
import scala.concurrent.Future

class BeforeYouContinueToTaxableControllerSpec extends SpecBase with ScalaCheckPropertyChecks {

  private lazy val beforeYouContinueToTaxableRoute: String = routes.BeforeYouContinueToTaxableController.onPageLoad().url
  private val startDate: LocalDate = LocalDate.parse("2000-01-01")
  private val mockTrustsConnector: TrustConnector = mock[TrustConnector]

  "BeforeYouContinueToTaxableController" when {

    ".onPageLoad" must {

      "return OK and the correct view for a GET" when {

        "express answered at registration" in {

          forAll(arbitrary[Boolean]) { bool =>

            when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
                (Future.successful(Right(TrustDetails(startDate, Some(false), Some(bool), None))))
              )

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .build()

            val request = FakeRequest(GET, beforeYouContinueToTaxableRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[BeforeYouContinueToTaxableView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(utr, UTR, displayExpress = false)(request, messages).toString

            application.stop()
          }
        }

        "express not answered at registration" in {

          when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
              (Future.successful(Right(TrustDetails(startDate, Some(false), None, None))))
            )

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(GET, beforeYouContinueToTaxableRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BeforeYouContinueToTaxableView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(utr, UTR, displayExpress = true)(request, messages).toString

          application.stop()
        }
      }

      "return an Internal Server Error when the connector call returns an error for /GET" in {

        when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
            (Future.successful(Left(ServerError())))
          )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
          .build()

        val request = FakeRequest(GET, beforeYouContinueToTaxableRoute)

        val result = route(application, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentType(result) mustBe Some("text/html")

        application.stop()
      }
    }

    ".onSubmit" when {

      "express answered at registration" must {
        "redirect to task list" in {

          forAll(arbitrary[Boolean]) { bool =>

            when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
              .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
                (Future.successful(Right(TrustDetails(startDate, Some(false), Some(bool), None))))
              )

            val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .build()

            val request = FakeRequest(POST, beforeYouContinueToTaxableRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              controllers.tasklist.routes.TaskListController.onPageLoad().url

            application.stop()
          }
        }
      }

      "express not answered at registration" must {
        "redirect to express trust yes no" in {

          when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
              (Future.successful(Right(TrustDetails(startDate, Some(false), None, None))))
            )

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, beforeYouContinueToTaxableRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.ExpressTrustYesNoController.onPageLoad().url

          application.stop()
        }
      }

      "return an Internal Server Error when the connector call returns an error for /POST" in {

        when(mockTrustsConnector.getUntransformedTrustDetails(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, TrustDetails]
            (Future.successful(Left(ServerError())))
          )

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
          .build()

        val request = FakeRequest(POST, beforeYouContinueToTaxableRoute)

        val result = route(application, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentType(result) mustBe Some("text/html")

        application.stop()
      }
    }
  }
}
