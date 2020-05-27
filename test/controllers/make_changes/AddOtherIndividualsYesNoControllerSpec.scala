/*
 * Copyright 2020 HM Revenue & Customs
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
import pages.makechanges._
import pages.{UTRPage, WhatIsNextPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.makechanges.AddOtherIndividualsYesNoView

import scala.concurrent.Future

class AddOtherIndividualsYesNoControllerSpec extends SpecBase {

  val formProvider = new YesNoFormProvider()
  val prefix: String = "addOtherIndividuals"
  val form = formProvider.withPrefix(prefix)

  val mockConnector = mock[TrustsStoreConnector]

  lazy val addOtherIndividualsYesNoRoute = routes.AddOtherIndividualsYesNoController.onPageLoad().url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatIsNextPage, MakeChanges).success.value

  "AddOtherIndividualsYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, addOtherIndividualsYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AddOtherIndividualsYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, prefix)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddOrUpdateOtherIndividualsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, addOtherIndividualsYesNoRoute)

      val view = application.injector.instanceOf[AddOtherIndividualsYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), prefix)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to individual declaration when valid data is submitted and no has been selected for all the questions" in {

      val utr = "0987654321"

      val userAnswers = baseAnswers
        .set(UTRPage, utr).success.value
        .set(UpdateTrusteesYesNoPage, false).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, addOtherIndividualsYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

      application.stop()
    }

    "redirect to overview when valid data is submitted, yes has been selected for update trustees question and no has been selected for the rest" in {

      val utr = "0987654321"

      val userAnswers = baseAnswers
        .set(UTRPage, utr).success.value
        .set(UpdateTrusteesYesNoPage, true).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
          .build()

      val request =
        FakeRequest(POST, addOtherIndividualsYesNoRoute)
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

      val addOtherIndividualsYesNoRoute = routes.AddOtherIndividualsYesNoController.onPageLoad().url

      val utr = "0987654321"

      val userAnswers = emptyUserAnswers
        .set(WhatIsNextPage, CloseTrust).success.value
        .set(UTRPage, utr).success.value
        .set(UpdateTrusteesYesNoPage, false).success.value
        .set(UpdateBeneficiariesYesNoPage, false).success.value
        .set(UpdateSettlorsYesNoPage, false).success.value
        .set(AddOrUpdateProtectorYesNoPage, false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreConnector].toInstance(mockConnector))
          .build()

      val request =
        FakeRequest(POST, addOtherIndividualsYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      when(mockConnector.set(any(), any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value must include(
        s"/maintain-a-trust/overview"
      )

      application.stop()
    }

    "redirect to Session Expired if required answer does not exist" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, addOtherIndividualsYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, addOtherIndividualsYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AddOtherIndividualsYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, prefix)(fakeRequest, messages).toString

      application.stop()
    }

  }
}
