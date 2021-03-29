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

package controllers

import base.SpecBase
import forms.WhatIsNextFormProvider
import models.pages.WhatIsNext
import models.{Underlying4mldTrustIn5mldMode, Underlying4mldTrustIn4mldMode, Underlying5mldTaxableTrustIn5mldMode}
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsNextPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.data.Form
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatIsNextView

class WhatIsNextControllerSpec extends SpecBase with MockitoSugar {

  val form: Form[WhatIsNext] = new WhatIsNextFormProvider()()

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()

  "WhatIsNext Controller" must {

    "return OK and the correct view for a GET" when {

      "in 4mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsNextView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, Underlying4mldTrustIn4mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsNextView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, Underlying4mldTrustIn5mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode maintaining a 5mld taxable trust" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
          .set(ExpressTrustYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsNextView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, Underlying5mldTaxableTrustIn5mldMode)(request, messages).toString

        application.stop()
      }
    }

    "populate the view correctly on a GET when the question has previously been answered" when {

      "in 4mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)
          .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(WhatIsNext.MakeChanges), Underlying4mldTrustIn4mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)
          .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(WhatIsNext.MakeChanges), Underlying4mldTrustIn5mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode maintaining a 5mld taxable trust" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
          .set(WhatIsNextPage, WhatIsNext.NoLongerTaxable).success.value
          .set(ExpressTrustYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, onPageLoad)

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(WhatIsNext.NoLongerTaxable), Underlying5mldTaxableTrustIn5mldMode)(request, messages).toString

        application.stop()
      }
    }

    "redirect to Session Expired if no data" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to declaration when user selects 'Declare no changes'" in {

      val userAnswers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "declare"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe
        controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

      application.stop()
    }

    "redirect to do you need to update details for the trustees when user selects 'Make changes' and not maintaining a 5mld taxable trust" in {

      val userAnswers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "make-changes"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to do you need to update details for the trust when user selects 'Make changes' and maintaining a 5mld taxable trust" in {

      val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
        .set(ExpressTrustYesNoPage, false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "make-changes"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to Do you know the date the last asset in the trust was shared out when user selects 'Close'" in {

      val userAnswers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "close-trust"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.close.taxable.routes.DateLastAssetSharedOutYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to no tax liability info page when user selects 'No longer taxable'" in {

      val userAnswers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "no-longer-taxable"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.NoTaxLiabilityInfoController.onPageLoad().url

      application.stop()
    }

    "redirect to feature unavilable page when user selects 'Needs to pay tax'" in {

      val userAnswers = emptyUserAnswersForUtr

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "needs-to-pay-tax"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.FeatureNotAvailableController.onPageLoad().url

      application.stop()
    }

    "redirect to Generated PDF when user selects 'generate-pdf'" in {

      val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "generate-pdf"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.ObligedEntityPdfController.getPdf(userAnswers.identifier).url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" when {

      "in 4mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request =
          FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, Underlying4mldTrustIn4mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, Underlying4mldTrustIn5mldMode)(request, messages).toString

        application.stop()
      }

      "in 5mld mode maintaining a 5mld taxable trust" in {

        val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
          .set(ExpressTrustYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WhatIsNextView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, Underlying5mldTaxableTrustIn5mldMode)(request, messages).toString

        application.stop()
      }
    }
  }
}
