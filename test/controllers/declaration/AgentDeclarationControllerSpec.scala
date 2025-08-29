/*
 * Copyright 2025 HM Revenue & Customs
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
import cats.data.EitherT
import forms.declaration.AgentDeclarationFormProvider
import models.errors.{DeclarationError, ServerError, TrustErrors}
import models.http.TVNResponse
import models.pages.WhatIsNext
import models.pages.WhatIsNext.MakeChanges
import models.{AgentDeclaration, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import pages.WhatIsNextPage
import pages.declaration.{AgencyRegisteredAddressInternationalPage, AgencyRegisteredAddressUkPage, AgencyRegisteredAddressUkYesNoPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DeclarationService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.TrustEnvelope.TrustEnvelope
import views.html.declaration.AgentDeclarationView

import scala.concurrent.Future

class AgentDeclarationControllerSpec extends SpecBase {

  private val formProvider = new AgentDeclarationFormProvider()
  private val form: Form[AgentDeclaration] = formProvider()
  private val address: UKAddress = UKAddress("line1", "line2", None, None, "postCode")
  private lazy val onSubmit: Call = routes.AgentDeclarationController.onSubmit()

  private val whatIsNext: WhatIsNext = MakeChanges
  private val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, whatIsNext).value

  private def mockDeclarationServiceResult(declarationService: DeclarationService,
                                           result: Either[TrustErrors, TVNResponse] = Right(TVNResponse("123456"))
                                          ): OngoingStubbing[TrustEnvelope[TVNResponse]] = {

    when(declarationService.agentDeclaration(any(), any(), any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(result)))
  }

  "Agent Declaration Controller" must {

    "return OK and the correct view for a onPageLoad" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, routes.AgentDeclarationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AgentDeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, closingTrust = false)(request, messages).toString

      application.stop()
    }

    "redirect to confirmation for a POST" in {

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkYesNoPage, true).value
        .set(AgencyRegisteredAddressUkPage, address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(
          ("firstName", "John"),
          ("lastName", "Smith"),
          ("agencyName", "Agency Name"),
          ("telephoneNumber", "01234567890"),
          ("crn", "123456")
        )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.declaration.routes.ConfirmationController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, routes.AgentDeclarationController.onPageLoad().url)
          .withFormUrlEncodedBody(("firstName", ""), ("lastName", ""), ("agencyName", ""), ("telephoneNumber", ""), ("crn", ""))

      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> "", "agencyName" -> "", "telephoneNumber" -> "", "crn" -> ""))

      val view = application.injector.instanceOf[AgentDeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, closingTrust = false)(request, messages).toString

      application.stop()
    }

    "render problem declaring when error retrieving TVN" in {

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkYesNoPage, true).value
        .set(AgencyRegisteredAddressUkPage, address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService, Left(DeclarationError()))

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(
          ("firstName", "John"),
          ("lastName", "Smith"),
          ("agencyName", "Agency Name"),
          ("telephoneNumber", "01234567890"),
          ("crn", "123456")
        )

      val result = route(application, request).value

      redirectLocation(result).value mustBe controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url

      application.stop()
    }

    "render problem declaring when the user is not an agent" in {

      val enrolments: Enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", "0987654321")), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkYesNoPage, true).value
        .set(AgencyRegisteredAddressUkPage, address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"), ("agencyName", "Agency Name"), ("telephoneNumber", "01234567890"), ("crn", "123456"))

      val result = route(application, request).value

      redirectLocation(result).value mustBe controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url

      application.stop()

    }

    "render problem declaring when failed to get agency address (agencyAddress = None)" in {

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkYesNoPage, false).value
        .set(AgencyRegisteredAddressUkPage, None).value
        .set(AgencyRegisteredAddressInternationalPage, None).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"), ("agencyName", "Agency Name"), ("telephoneNumber", "01234567890"), ("crn", "123456"))

      val result = route(application, request).value

      redirectLocation(result).value mustBe controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url

      application.stop()
    }

    "return an Internal Server Error when setting the user answers goes wrong" in {

      mockPlaybackRepositoryBuilder(mockPlaybackRepository, setResult = Left(ServerError()))

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "SARN1234567")), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(AgencyRegisteredAddressUkYesNoPage, true).value
        .set(AgencyRegisteredAddressUkPage, address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Agent,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"), ("agencyName", "Agency Name"), ("telephoneNumber", "01234567890"), ("crn", "123456"))

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }
  }

}
