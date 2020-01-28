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

package controllers.declaration

import base.SpecBase
import forms.declaration.IndividualDeclarationFormProvider
import models.IndividualDeclaration
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.declaration.IndividualDeclarationView

class IndividualDeclarationControllerSpec extends SpecBase {

  val formProvider = new IndividualDeclarationFormProvider()
  val form: Form[IndividualDeclaration] = formProvider()

  lazy val onSubmit: Call = routes.IndividualDeclarationController.onSubmit()

  "Individual Declaration Controller" must {

    "return OK and the correct view for a onPageLoad" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.IndividualDeclarationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[IndividualDeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, onSubmit)(fakeRequest, messages).toString

      application.stop()
    }
  }

}
