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
import forms.declaration.AgentDeclarationFormProvider
import models.{AgentDeclaration, Mode, NormalMode, UKAddress}
import pages.declaration.{AgencyRegisteredAddressUkPage, AgencyRegisteredAddressUkYesNoPage}
import pages.UTRPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{DeclarationService, FakeDeclarationService, FakeFailingDeclarationService}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import views.html.declaration.AgentDeclarationView

class AgentDeclarationControllerSpec extends SpecBase {

  val formProvider = new AgentDeclarationFormProvider()
  val form: Form[AgentDeclaration] = formProvider()
  val address: UKAddress = UKAddress("line1", "line2", None, None, "postCode")
  val mode: Mode = NormalMode
  lazy val onSubmit: Call = routes.AgentDeclarationController.onSubmit(mode)

  "Agent Declaration Controller" must {

    "return OK and the correct view for a onPageLoad" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.AgentDeclarationController.onPageLoad(mode).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AgentDeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to confirmation for a POST" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = emptyUserAnswers
        .set(UTRPage, utr).success.value
        .set(AgencyRegisteredAddressUkYesNoPage, true).success.value
        .set(AgencyRegisteredAddressUkPage, address).success.value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].to(new FakeDeclarationService())
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"), ("agencyName", "Agency Name"), ("telephoneNumber", "01234567890"), ("crn", "123456"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.declaration.routes.ConfirmationController.onPageLoad(mode).url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, routes.AgentDeclarationController.onPageLoad(mode).url)
          .withFormUrlEncodedBody(("firstName", ""), ("lastName", ""), ("agencyName", ""), ("telephoneNumber", ""), ("crn", ""))

      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> "", "agencyName" -> "", "telephoneNumber" -> "", "crn" -> ""))

      val view = application.injector.instanceOf[AgentDeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, mode)(fakeRequest, messages).toString

      application.stop()
    }

    "render problem declaring when error retrieving TVN" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = emptyUserAnswers
        .set(UTRPage, utr).success.value
        .set(AgencyRegisteredAddressUkYesNoPage, true).success.value
        .set(AgencyRegisteredAddressUkPage, address).success.value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].to(new FakeFailingDeclarationService())
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"), ("agencyName", "Agency Name"), ("telephoneNumber", "01234567890"), ("crn", "123456"))

      val result = route(application, request).value

      redirectLocation(result).value mustBe controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url

      application.stop()
    }
  }

}
