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

package controllers.tasklist

import base.SpecBase
import connectors.{TrustConnector, TrustsStoreConnector}
import generators.ModelGenerators
import models.MigrationTaskStatus.NothingToUpdate
import models.pages.Tag._
import models.pages.WhatIsNext
import models.{CompletedMaintenanceTasks, FirstTaxYearAvailable, TaskList, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.WhatIsNextPage
import pages.tasks._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import views.html.{NonTaxToTaxProgressView, VariationProgressView}

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with ModelGenerators {

  val mockTrustsStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]
  val mockVariationProgress: VariationProgress = mock[VariationProgress]

  override def beforeEach(): Unit = {
    reset(mockTrustsStoreConnector, mockTrustsConnector, mockVariationProgress, playbackRepository)

    when(mockTrustsStoreConnector.getStatusOfTasks(any())(any(), any()))
      .thenReturn(Future.successful(CompletedMaintenanceTasks()))

    when(mockTrustsConnector.getSettlorsStatus(any())(any(), any()))
      .thenReturn(Future.successful(NothingToUpdate))

    when(mockTrustsConnector.getBeneficiariesStatus(any())(any(), any()))
      .thenReturn(Future.successful(NothingToUpdate))

    when(mockTrustsConnector.getFirstTaxYearToAskFor(any())(any(), any()))
      .thenReturn(Future.successful(FirstTaxYearAvailable(1, earlierYearsToDeclare = false)))

    when(mockVariationProgress.generateTaskList(any(), any(), any()))
      .thenReturn(TaskList(Nil, Nil))

    when(mockVariationProgress.generateTransitionTaskList(any(), any(), any(), any(), any()))
      .thenReturn(TaskList(Nil, Nil))

    when(playbackRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  "TaskListControllerController" must {

    "return OK and the correct view for a GET" when {

      "migrating from non-tax to tax" in {

        val answers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
            bind(classOf[TrustConnector]).toInstance(mockTrustsConnector),
            bind(classOf[VariationProgress]).toInstance(mockVariationProgress)
          ).build()

        val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonTaxToTaxProgressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answers.identifier, answers.identifierType, Nil, Nil, Organisation, isAbleToDeclare = true)(request, messages).toString

        verify(mockVariationProgress).generateTransitionTaskList(any(), any(), any(), any(), any())

        application.stop()
      }

      "not migrating from non-tax to tax" when {

        "making changes" in {

          val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(
              bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
              bind(classOf[TrustConnector]).toInstance(mockTrustsConnector),
              bind(classOf[VariationProgress]).toInstance(mockVariationProgress)
            ).build()

          val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VariationProgressView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(answers.identifier, answers.identifierType, Nil, Nil, Organisation, isAbleToDeclare = true, closingTrust = false)(request, messages).toString

          verify(mockVariationProgress).generateTaskList(any(), any(), any())

          application.stop()
        }

        "closing" in {

          val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.CloseTrust).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(
              bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
              bind(classOf[TrustConnector]).toInstance(mockTrustsConnector),
              bind(classOf[VariationProgress]).toInstance(mockVariationProgress)
            ).build()

          val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VariationProgressView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(answers.identifier, answers.identifierType, Nil, Nil, Organisation, isAbleToDeclare = true, closingTrust = true)(request, messages).toString

          verify(mockVariationProgress).generateTaskList(any(), any(), any())

          application.stop()
        }
      }
    }

    "redirect to Technical difficulties page when no value found for What do you want to do next" when {

      "onPageLoad" in {
        val answers = emptyUserAnswersForUtr

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "onSubmit" in {
        val answers = emptyUserAnswersForUtr

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "redirectToTask" in {
        val answers = emptyUserAnswersForUtr

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(TrustDetailsTaskStartedPage).url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    "redirect to AgencyRegisteredAddressUkYesNoController for agent" in {

      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers), affinityGroup = Agent).build()

      val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad().url

      application.stop()
    }

    "redirect to IndividualDeclarationController for individual" when {

      "migrating from non-tax to tax" in {
        val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transition.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }

      "not migrating from non-tax to tax" in {
        val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }
    }

    "redirect to trust details service when trust details task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(TrustDetailsTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9838/maintain-a-trust/trust-details/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(TrustDetailsTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to settlors service when settlors task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(SettlorsTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9795/maintain-a-trust/settlors/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(SettlorsTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to trustees service when trustees task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(TrusteesTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9792/maintain-a-trust/trustees/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(TrusteesTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to beneficiaries service when beneficiaries task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(BeneficiariesTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9793/maintain-a-trust/beneficiaries/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(BeneficiariesTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to assets service when assets task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(AssetsTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9800/maintain-a-trust/trust-assets/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(AssetsTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to tax liability service when tax liability task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(TaxLiabilityTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9844/maintain-a-trust/tax-liability/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(TaxLiabilityTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to protectors service when protectors task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(ProtectorsTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9796/maintain-a-trust/protectors/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(ProtectorsTaskStartedPage).get mustBe true

      application.stop()
    }

    "redirect to other individuals service when other individuals task selected" in {
      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onRedirectToTask(OtherIndividualsTaskStartedPage).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        s"http://localhost:9799/maintain-a-trust/other-individuals/${answers.identifier}"

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(OtherIndividualsTaskStartedPage).get mustBe true

      application.stop()
    }
  }
}
