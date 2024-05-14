/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.declaration.IndividualDeclarationFormProvider
import models.errors.{DeclarationError, ServerError, TrustErrors}
import models.http.TVNResponse
import models.pages.WhatIsNext
import models.pages.WhatIsNext.MakeChanges
import models.{IndividualDeclaration, UKAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import pages.WhatIsNextPage
import pages.correspondence.CorrespondenceAddressPage
import pages.trustees.{IsThisLeadTrusteePage, TrusteeAddressPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DeclarationService
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.TrustEnvelope.TrustEnvelope
import views.html.declaration.IndividualDeclarationView

import scala.concurrent.Future

class IndividualDeclarationControllerSpec extends SpecBase {

  private val formProvider = new IndividualDeclarationFormProvider()
  private val form: Form[IndividualDeclaration] = formProvider()
  private val address: UKAddress = UKAddress("line1", "line2", None, None, "postCode")
  private lazy val onSubmit: Call = routes.IndividualDeclarationController.onSubmit()

  private val whatIsNext: WhatIsNext = MakeChanges
  private val baseAnswers: UserAnswers = emptyUserAnswersForUtr
    .set(WhatIsNextPage, whatIsNext).value

  private def mockDeclarationServiceResult(declarationService: DeclarationService,
                                           result: Either[TrustErrors, TVNResponse] = Right(TVNResponse("123456"))
                                          ): OngoingStubbing[TrustEnvelope[TVNResponse]] = {

    when(declarationService.individualDeclaration(any(), any(), any())(any(), any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, TVNResponse](Future.successful(result)))
  }

  "Individual Declaration Controller" must {

    "return OK and the correct view for a onPageLoad" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, routes.IndividualDeclarationController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[IndividualDeclarationView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, closingTrust = false)(request, messages).toString

      application.stop()
    }

    "redirect to confirmation for a POST when there is a lead trustee address" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(IsThisLeadTrusteePage(0), true).value
        .set(TrusteeAddressPage(0), address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.declaration.routes.ConfirmationController.onPageLoad().url

      application.stop()
    }

    "redirect to confirmation for a POST when there is a correspondence address" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(IsThisLeadTrusteePage(0), true).value
        .set(CorrespondenceAddressPage, address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService)

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustBe controllers.declaration.routes.ConfirmationController.onPageLoad().url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, routes.IndividualDeclarationController.onPageLoad().url)
          .withFormUrlEncodedBody(("firstName", ""), ("lastName", ""))

      val boundForm = form.bind(Map("firstName" -> "", "lastName" -> ""))

      val view = application.injector.instanceOf[IndividualDeclarationView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, closingTrust = false)(request, messages).toString

      application.stop()
    }

    "render problem declaring when error retrieving TVN" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(IsThisLeadTrusteePage(0), true).value
        .set(TrusteeAddressPage(0), address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService, Left(DeclarationError()))

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.declaration.routes.ProblemDeclaringController.onPageLoad().url)

      application.stop()
    }

    "return an Internal Server Error and render error page when there's an unexpected error" in {

      val utr = "0987654321"

      val enrolments = Enrolments(Set(Enrolment(
        "HMRC-TERS-ORG", Seq(EnrolmentIdentifier("SAUTR", utr)), "Activated"
      )))

      val userAnswers = baseAnswers
        .set(IsThisLeadTrusteePage(0), true).value
        .set(TrusteeAddressPage(0), address).value

      val mockDeclarationService: DeclarationService = mock[DeclarationService]
      mockDeclarationServiceResult(mockDeclarationService, Left(ServerError()))

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers),
          affinityGroup = Organisation,
          enrolments = enrolments
        ).overrides(
          bind[DeclarationService].toInstance(mockDeclarationService)
        ).build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("firstName", "John"), ("lastName", "Smith"))

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
    }
  }

}
