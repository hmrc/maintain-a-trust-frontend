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
import config.FrontendAppConfig
import forms.WhatIsNextFormProvider
import models.pages.WhatIsNext
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsNextPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatIsNextView

class WhatIsNextControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new WhatIsNextFormProvider()
  val form: Form[WhatIsNext] = formProvider()

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  "WhatIsNext Controller" must {

    "return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhatIsNextView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, onPageLoad)

      val view = application.injector.instanceOf[WhatIsNextView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(WhatIsNext.MakeChanges))(request, messages).toString

      application.stop()
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
      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "declare"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe
        controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

      application.stop()
    }

    "redirect to do you need to update details for the trustees when user selects 'Make changes'" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "make-changes"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to Do you know the date the last asset in the trust was shared out when user selects 'Close' and feature toggle set to true" in {

      when(mockAppConfig.closeATrustEnabled) thenReturn true

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
        .build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "close-trust"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.close.routes.DateLastAssetSharedOutYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to Feature unavailable when user selects 'Close' and feature toggle set to false" in {

      when(mockAppConfig.closeATrustEnabled) thenReturn false

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
        .build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", "close-trust"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe controllers.routes.FeatureNotAvailableController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[WhatIsNextView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }
  }
}
