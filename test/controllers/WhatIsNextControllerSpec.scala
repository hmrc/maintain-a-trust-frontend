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
import connectors.TrustConnector
import forms.WhatIsNextFormProvider
import generators.ModelGenerators
import models.Underlying4mldTrustIn4mldMode
import models.pages.WhatIsNext
import models.pages.WhatIsNext._
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.WhatIsNextPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MaintainATrustService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HttpResponse
import views.html.WhatIsNextView

import scala.concurrent.Future

class WhatIsNextControllerSpec extends SpecBase with MockitoSugar with ScalaCheckPropertyChecks with ModelGenerators {

  val form: Form[WhatIsNext] = new WhatIsNextFormProvider()()

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()
  
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]
  val mockMaintainATrustService: MaintainATrustService = mock[MaintainATrustService]

  def beforeTest(): Unit = {
    reset(mockTrustsConnector)
    reset(mockMaintainATrustService)

    when(mockTrustsConnector.setTaxableMigrationFlag(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))

    when(mockMaintainATrustService.removeTransformsAndResetTaskList(any())(any(), any()))
      .thenReturn(Future.successful(()))
  }

  "WhatIsNext Controller" must {

    "return OK and the correct view for a GET" in {

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

    "populate the view correctly on a GET when the question has previously been answered" in {

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

    "redirect to Session Expired if no data" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, onPageLoad)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect for a user selection" when {

      "Declare No Changes" when {

        "agent user" must {
          "redirect to AgencyRegisteredAddressUkYesNoController" in {

            beforeTest()

            val userAnswers = emptyUserAnswersForUtr

            val application = applicationBuilder(userAnswers = Some(userAnswers), AffinityGroup.Agent)
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
              .build()

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
              .withFormUrlEncodedBody(("value", DeclareTheTrustIsUpToDate.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustBe
              controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad().url

            application.stop()
          }
        }

        "non-agent user" must {
          "redirect to IndividualDeclarationController" in {

            beforeTest()

            val userAnswers = emptyUserAnswersForUtr

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
              .build()

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
              .withFormUrlEncodedBody(("value", DeclareTheTrustIsUpToDate.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustBe
              controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

            application.stop()
          }
        }
      }

      "Make Changes" when {

        "4mld" must {
          "redirect to update trustee details" in {

            beforeTest()

            val userAnswers = emptyUserAnswersForUtr

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
              .build()

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
              .withFormUrlEncodedBody(("value", MakeChanges.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

            application.stop()
          }
        }

        "5mld" when {

          "underlying data is 4mld" must {
            "redirect to update trustee details" in {

              beforeTest()

              val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isUnderlyingData5mld = false)

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
                .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
                .build()

              implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
                .withFormUrlEncodedBody(("value", MakeChanges.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad().url

              application.stop()
            }
          }

          "underlying data is 5mld" must {
            "redirect to update trust details" when {

              "taxable" in {

                beforeTest()

                val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isUnderlyingData5mld = true)

                val application = applicationBuilder(userAnswers = Some(userAnswers))
                  .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
                  .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
                  .build()

                implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
                  .withFormUrlEncodedBody(("value", MakeChanges.toString))

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER

                redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad().url

                application.stop()
              }

              "non-taxable" in {

                beforeTest()

                val userAnswers = emptyUserAnswersForUrn

                val application = applicationBuilder(userAnswers = Some(userAnswers))
                  .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
                  .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
                  .build()

                implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
                  .withFormUrlEncodedBody(("value", MakeChanges.toString))

                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER

                redirectLocation(result).value mustBe controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad().url

                application.stop()
              }
            }
          }
        }
      }

      "Close Trust" when {

        "taxable" must {
          "redirect to date the last asset was shared out" in {

            val gen = arbitrary[WhatIsNext]

            forAll(gen) { previousAnswer =>
              beforeTest()

              val userAnswers = emptyUserAnswersForUtr
                .set(WhatIsNextPage, previousAnswer).success.value

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
                .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
                .build()

              implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
                .withFormUrlEncodedBody(("value", CloseTrust.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustBe controllers.close.taxable.routes.DateLastAssetSharedOutYesNoController.onPageLoad().url

              application.stop()
            }
          }
        }

        "non-taxable" must {
          "redirect to date the trust was closed" in {

            val gen = arbitrary[WhatIsNext]

            forAll(gen) { previousAnswer =>
              beforeTest()

              val userAnswers = emptyUserAnswersForUrn
                .set(WhatIsNextPage, previousAnswer).success.value

              val application = applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
                .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
                .build()

              implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
                .withFormUrlEncodedBody(("value", CloseTrust.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustBe controllers.close.nontaxable.routes.DateClosedController.onPageLoad().url

              application.stop()
            }
          }
        }
      }

      "No Longer Taxable" must {
        "redirect to tax liability info page" in {

          beforeTest()

          val userAnswers = emptyUserAnswersForUtr

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
            .build()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", NoLongerTaxable.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe controllers.routes.NoTaxLiabilityInfoController.onPageLoad().url

          application.stop()
        }
      }

      "Needs to pay tax" must {
        "redirect to NeedToPayTaxYesNo Page" in {

          beforeTest()

          val userAnswers = emptyUserAnswersForUtr

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
            .build()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", NeedsToPayTax.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe controllers.transition.routes.NeedToPayTaxYesNoController.onPageLoad().url

          application.stop()
        }
      }

      "Generate PDF" must {
        "redirect to generated PDF" in {

          beforeTest()

          val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true)

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
            .build()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", GeneratePdf.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe controllers.routes.ObligedEntityPdfController.getPdf(userAnswers.identifier).url

          application.stop()
        }
      }
    }

    "remove transforms if answer has changed (and new answer is not GeneratePdf)" when {

      "there is no previous answer" in {

        val gen = arbitrary[WhatIsNext]

        forAll(gen.suchThat(_ != GeneratePdf)) { answer =>
          beforeTest()

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
            .build()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", answer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          verify(mockMaintainATrustService).removeTransformsAndResetTaskList(any())(any(), any())

          application.stop()
        }
      }

      "there is a previous answer" in {

        val gen = arbitrary[WhatIsNext]

        forAll(gen) { previousAnswer =>
          forAll(gen.suchThat(x => x != previousAnswer && x != GeneratePdf)) { newAnswer =>

            beforeTest()

            val userAnswers = emptyUserAnswersForUtr
              .set(WhatIsNextPage, previousAnswer).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
              .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
              .build()

            implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
              .withFormUrlEncodedBody(("value", newAnswer.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            verify(mockMaintainATrustService).removeTransformsAndResetTaskList(any())(any(), any())

            application.stop()
          }
        }
      }
    }

    "not remove transforms" when {

      "answer hasn't changed" in {

        val gen = arbitrary[WhatIsNext]

        forAll(gen) { previousAnswer =>
          beforeTest()

          val userAnswers = emptyUserAnswersForUtr
            .set(WhatIsNextPage, previousAnswer).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
            .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
            .build()

          implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
            .withFormUrlEncodedBody(("value", previousAnswer.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          verify(mockMaintainATrustService, never()).removeTransformsAndResetTaskList(any())(any(), any())

          application.stop()
        }
      }

      "answer is GeneratePdf" in {

        beforeTest()

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
          .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
          .build()

        implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(("value", GeneratePdf.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockMaintainATrustService, never()).removeTransformsAndResetTaskList(any())(any(), any())

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[WhatIsNextView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, Underlying4mldTrustIn4mldMode)(request, messages).toString

      application.stop()
    }
  }

  "set taxable migration flag to true when NeedsToPayTax selected" in {

    beforeTest()

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
      .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
      .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
      .build()

    implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
      .withFormUrlEncodedBody(("value", NeedsToPayTax.toString))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER

    verify(mockTrustsConnector).setTaxableMigrationFlag(any(), eqTo(true))(any(), any())

    application.stop()
  }

  "set taxable migration flag to false when new answer is neither NeedsToPayTax nor GeneratePdf" in {

    val gen = arbitrary[WhatIsNext]

    forAll(gen.suchThat(x => x != NeedsToPayTax && x != GeneratePdf)) { answer =>
      beforeTest()

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
        .overrides(bind[TrustConnector].toInstance(mockTrustsConnector))
        .overrides(bind[MaintainATrustService].toInstance(mockMaintainATrustService))
        .build()

      implicit val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(POST, onSubmit.url)
        .withFormUrlEncodedBody(("value", answer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      verify(mockTrustsConnector).setTaxableMigrationFlag(any(), eqTo(false))(any(), any())

      application.stop()
    }
  }

}
