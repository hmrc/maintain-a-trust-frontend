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

package controllers.make_changes

import base.SpecBase
import connectors.TrustsStoreConnector
import controllers.makechanges.routes
import forms.YesNoFormProvider
import models.pages.WhatIsNext.{CloseTrust, MakeChanges}
import models.{CompletedMaintenanceTasks, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges._
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.AddNonEeaCompanyYesNoView

import scala.concurrent.Future

class AddNonEeaCompanyYesNoControllerSpec extends SpecBase {

  val formProvider = new YesNoFormProvider()
  val prefix: String = "addNonEeaCompany"
  val form = formProvider.withPrefix(prefix)

  val mockConnector = mock[TrustsStoreConnector]

  lazy val addNonEeaCompanyYesNoRoute = routes.AddNonEeaCompanyYesNoController.onPageLoad().url

  val baseAnswers: UserAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
    .set(WhatIsNextPage, MakeChanges).success.value
    .set(ExpressTrustYesNoPage, false).success.value

  "AddNonEeaCompanyYesNo Controller" when {

    "in 5mld mode for a 5mld taxable trust" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, addNonEeaCompanyYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddNonEeaCompanyYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, prefix)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers.set(AddOrUpdateNonEeaCompanyYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addNonEeaCompanyYesNoRoute)

        val view = application.injector.instanceOf[AddNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), prefix)(request, messages).toString

        application.stop()
      }

      "redirect to individual declaration when valid data is submitted and no has been selected for all the questions" in {

        val userAnswers = baseAnswers
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request =
          FakeRequest(POST, addNonEeaCompanyYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }

      "redirect to overview when valid data is submitted, yes has been selected for update trustees question and no has been selected for the rest" in {

        val userAnswers = baseAnswers
          .set(UpdateTrusteesYesNoPage, true).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
            .build()

        val request =
          FakeRequest(POST, addNonEeaCompanyYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        when(mockConnector.set(any(), any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value must include(
          s"/maintain-a-trust/overview"
        )

        application.stop()
      }

      "redirect to overview when valid data is submitted, no has been selected for all questions and the user is closing the trust" in {

        val addNonEeaCompanyYesNoRoute = routes.AddNonEeaCompanyYesNoController.onPageLoad().url

        val userAnswers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, CloseTrust).success.value
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value
          .set(AddOrUpdateOtherIndividualsYesNoPage, false).success.value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
            .build()

        val request =
          FakeRequest(POST, addNonEeaCompanyYesNoRoute)
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

        val request =
          FakeRequest(POST, addNonEeaCompanyYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddNonEeaCompanyYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, prefix)(request, messages).toString

        application.stop()
      }

    }
  }
}
