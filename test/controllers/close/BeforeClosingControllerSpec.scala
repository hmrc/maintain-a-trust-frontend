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

package controllers.close

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.close.BeforeClosingView

class BeforeClosingControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.BeforeClosingController.onPageLoad().url

  "BeforeClosingController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr)).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BeforeClosingView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(request, messages).toString

      application.stop()
    }

  }
}
