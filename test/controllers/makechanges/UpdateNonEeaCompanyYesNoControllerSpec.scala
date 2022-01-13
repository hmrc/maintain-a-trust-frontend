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
import connectors.TrustsStoreConnector
import forms.YesNoFormProvider
import models.pages.WhatIsNext.{CloseTrust, MakeChanges}
import models.{CompletedMaintenanceTasks, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges._
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.UpdateNonEeaCompanyYesNoView

import scala.concurrent.Future

class UpdateNonEeaCompanyYesNoControllerSpec extends SpecBase {

  private val mockConnector: TrustsStoreConnector = mock[TrustsStoreConnector]

  private lazy val updateNonEeaCompanyYesNoRoute: String = routes.UpdateNonEeaCompanyYesNoController.onPageLoad().url

  "UpdateNonEeaCompanyYesNoController" when {

    "making changes" must {

      val prefix: String = "updateNonEeaCompany"
      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
        .set(WhatIsNextPage, MakeChanges).success.value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateNonEeaCompanyYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, prefix)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(AddOrUpdateNonEeaCompanyYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateNonEeaCompanyYesNoRoute)

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), prefix)(request, messages).toString

        application.stop()
      }

      "redirect to individual declaration when valid data is submitted and no has been selected for all the questions" in {

        val userAnswers = baseAnswers
          .set(UpdateTrustDetailsYesNoPage, false).success.value
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, updateNonEeaCompanyYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }

      "redirect to overview when valid data is submitted, yes has been selected for update trustees question and no has been selected for the rest" in {

        val userAnswers = baseAnswers
          .set(UpdateTrustDetailsYesNoPage, false).success.value
          .set(UpdateTrusteesYesNoPage, true).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(POST, updateNonEeaCompanyYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        when(mockConnector.set(any(), any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value must include(
          s"/maintain-a-trust/overview"
        )

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateNonEeaCompanyYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, prefix)(request, messages).toString

        application.stop()
      }
    }

    "closing" must {

      val prefix: String = "updateNonEeaCompanyClosing"
      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

      val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(isUnderlyingData5mld = true)
        .set(WhatIsNextPage, CloseTrust).success.value

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateNonEeaCompanyYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, prefix)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(AddOrUpdateNonEeaCompanyYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateNonEeaCompanyYesNoRoute)

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), prefix)(request, messages).toString

        application.stop()
      }

      "redirect to overview when valid data is submitted and no has been selected for all questions" in {

        val updateNonEeaCompanyYesNoRoute = routes.UpdateNonEeaCompanyYesNoController.onPageLoad().url

        val userAnswers = baseAnswers
          .set(UpdateTrustDetailsYesNoPage, false).success.value
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(POST, updateNonEeaCompanyYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        when(mockConnector.set(any(), any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value must include(
          s"/maintain-a-trust/overview"
        )

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(POST, updateNonEeaCompanyYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, prefix)(request, messages).toString

        application.stop()
      }
    }
  }
}
