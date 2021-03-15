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

package controllers.makechanges

import base.SpecBase
import connectors.{TrustConnector, TrustsStoreConnector}
import forms.YesNoFormProvider
import models.{CompletedMaintenanceTasks, UserAnswers}
import models.pages.WhatIsNext
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.WhatIsNextPage
import pages.makechanges._
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsArray, JsBoolean}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sections.assets.NonEeaBusinessAsset
import views.html.makechanges.UpdateOtherIndividualsYesNoView

import scala.concurrent.Future

class UpdateOtherIndividualsYesNoControllerSpec extends SpecBase {

  val prefix = "updateOtherIndividuals"

  val form: Form[Boolean] = new YesNoFormProvider().withPrefix(prefix)

  val mockTrustsStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]

  lazy val updateOtherIndividualsYesNoRoute: String = routes.UpdateOtherIndividualsYesNoController.onPageLoad().url

  val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

  "UpdateOtherIndividualsYesNoController" when {

    "in 4mld mode" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, prefix)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers
          .set(AddOrUpdateOtherIndividualsYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

        val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

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
          .set(UpdateTrusteesYesNoPage, true).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreConnector].toInstance(mockTrustsStoreConnector))
          .build()

        val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

        when(mockTrustsStoreConnector.set(any(), any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

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
          view(boundForm, prefix)(request, messages).toString

        application.stop()
      }
    }

    "in 5mld mode for a 5mld taxable trust" must {

      val baseAnswers5mldTaxable = baseAnswers.copy(is5mldEnabled = true, isTrustTaxable = true)
        .set(ExpressTrustYesNoPage, false).success.value
      
      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers5mldTaxable)).build()

        val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, prefix)(request, messages).toString

        application.stop()
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = baseAnswers5mldTaxable
          .set(AddOrUpdateOtherIndividualsYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, updateOtherIndividualsYesNoRoute)

        val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), prefix)(request, messages).toString

        application.stop()
      }

      "redirect to add a non eea company yes no page when valid data is submitted and no eea company exists" in {

        val userAnswers = baseAnswers5mldTaxable
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value

        when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(false)))

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

        val userAnswers = baseAnswers5mldTaxable.copy(is5mldEnabled = true, isTrustTaxable = true)
          .set(NonEeaBusinessAsset, JsArray()).success.value
          .set(UpdateTrusteesYesNoPage, false).success.value
          .set(UpdateBeneficiariesYesNoPage, false).success.value
          .set(UpdateSettlorsYesNoPage, false).success.value
          .set(AddOrUpdateProtectorYesNoPage, false).success.value

        when(mockTrustsConnector.getDoNonEeaCompaniesAlreadyExist(any())(any(), any()))
          .thenReturn(Future.successful(JsBoolean(true)))

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

        val application = applicationBuilder(userAnswers = Some(baseAnswers5mldTaxable)).build()

        val request = FakeRequest(POST, updateOtherIndividualsYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UpdateOtherIndividualsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, prefix)(request, messages).toString

        application.stop()
      }
    }
  }
}
