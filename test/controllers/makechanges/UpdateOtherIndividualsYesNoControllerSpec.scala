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

package controllers.makechanges

import base.SpecBase
import cats.data.EitherT
import connectors.{TrustConnector, TrustsStoreConnector}
import forms.YesNoFormProvider
import models.errors.{MongoError, TrustErrors}
import models.pages.WhatIsNext
import models.{CompletedMaintenanceTasks, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges._
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsArray, JsBoolean}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sections.assets.NonEeaBusinessAsset
import views.html.makechanges.UpdateOtherIndividualsYesNoView

import scala.concurrent.Future

class UpdateOtherIndividualsYesNoControllerSpec extends SpecBase {

  private val mockTrustsStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  private val mockTrustsConnector: TrustConnector = mock[TrustConnector]

  private lazy val updateOtherIndividualsYesNoRoute: String = routes.UpdateOtherIndividualsYesNoController.onPageLoad().url

  "UpdateOtherIndividualsYesNoController" when {

    "making changes" must {

      val prefix: String = "updateOtherIndividuals"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      "underlying trust data is 4mld" must {

        val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = false)
          .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }

        "populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = baseAnswers
            .set(AddOrUpdateOtherIndividualsYesNoPage, true).value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }

        "redirect to individual declaration when valid data is submitted and no has been selected for all the questions" in {

          val userAnswers = baseAnswers
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

          application.stop()
        }

        "redirect to overview when valid data is submitted, yes has been selected for update trustees question and no has been selected for the rest" in {
          val userAnswers = baseAnswers
            .set(UpdateTrusteesYesNoPage, true).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreConnector].toInstance(mockTrustsStoreConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          when(mockTrustsStoreConnector.set(any(), any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, CompletedMaintenanceTasks](Future.successful(Right(CompletedMaintenanceTasks()))))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value must include(
            s"/maintain-a-trust/overview"
          )

          application.stop()
        }

        "return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }
      }

      "underlying trust data is 5mld" must {

        val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
          .set(WhatIsNextPage, WhatIsNext.MakeChanges).value

        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }

        "populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = baseAnswers
            .set(AddOrUpdateOtherIndividualsYesNoPage, true).value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }

        "redirect to add a non eea company yes no page when valid data is submitted and no eea company exists" in {

          val userAnswers = baseAnswers
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(false)))))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.AddNonEeaCompanyYesNoController.onPageLoad().url

          application.stop()
        }

        "redirect to update a non eea company yes no page when valid data is submitted and an eea company already exists" in {

          val userAnswers = baseAnswers
            .set(NonEeaBusinessAsset, JsArray()).value
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(true)))))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateNonEeaCompanyYesNoController.onPageLoad().url

          application.stop()
        }

        "return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, determinePrefix, closingTrust = false)(request, messages).toString

          application.stop()
        }
      }
    }

    "closing" must {

      val prefix: String = "updateOtherIndividualsClosing"
      val determinePrefix = (_: Boolean) => prefix

      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      "underlying trust data is 4mld" must {

        val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = false)
          .set(WhatIsNextPage, WhatIsNext.CloseTrust).value

        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }

        "populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = baseAnswers
            .set(AddOrUpdateOtherIndividualsYesNoPage, true).value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }

        "redirect to overview when valid data is submitted and no has been selected for all questions" in {

          val userAnswers = baseAnswers
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreConnector].toInstance(mockTrustsStoreConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          when(mockTrustsStoreConnector.set(any(), any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, CompletedMaintenanceTasks](Future.successful(Right(CompletedMaintenanceTasks()))))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value must include(
            s"/maintain-a-trust/overview"
          )

          application.stop()
        }

        "return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }
      }

      "underlying trust data is 5mld" must {

        val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
          .set(WhatIsNextPage, WhatIsNext.CloseTrust).value

        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }

        "populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = baseAnswers
            .set(AddOrUpdateOtherIndividualsYesNoPage, true).value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form.fill(true), determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }

        "redirect to add a non eea company yes no page when valid data is submitted and no eea company exists" in {

          val userAnswers = baseAnswers
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(false)))))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.AddNonEeaCompanyYesNoController.onPageLoad().url

          application.stop()
        }

        "redirect to update a non eea company yes no page when valid data is submitted and an eea company already exists" in {

          val userAnswers = baseAnswers
            .set(NonEeaBusinessAsset, JsArray()).value
            .set(UpdateTrusteesYesNoPage, false).value
            .set(UpdateBeneficiariesYesNoPage, false).value
            .set(UpdateSettlorsYesNoPage, false).value
            .set(AddOrUpdateProtectorYesNoPage, false).value

          when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
            .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(true)))))

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.makechanges.routes.UpdateNonEeaCompanyYesNoController.onPageLoad().url

          application.stop()
        }

        "return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

          val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(boundForm, determinePrefix, closingTrust = true)(request, messages).toString

          application.stop()
        }
      }
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(MongoError))

      val userAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
        .set(WhatIsNextPage, WhatIsNext.CloseTrust).value
        .set(UpdateTrusteesYesNoPage, false).value
        .set(UpdateBeneficiariesYesNoPage, false).value
        .set(UpdateSettlorsYesNoPage, false).value
        .set(AddOrUpdateProtectorYesNoPage, false).value

      when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, JsBoolean](Future.successful(Right(JsBoolean(false)))))

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .build()

      val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }
  }
}
