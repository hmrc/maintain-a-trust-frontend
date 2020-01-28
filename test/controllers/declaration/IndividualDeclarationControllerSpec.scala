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
import pages.UTRPage
import play.api.data.Form
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
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

    "redirect to confirmation for a POST" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val userAnswers = emptyUserAnswers.set(UTRPage, utr).success.value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.declaration.routes.ConfirmationController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, routes.IndividualDeclarationController.onPageLoad().url)
          .withFormUrlEncodedBody(("firstName", ""), ("lastName", ""))

      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> ""))

      val view = application.injector.instanceOf[IndividualDeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, onSubmit)(fakeRequest, messages).toString

      application.stop()
    }
  }

}
