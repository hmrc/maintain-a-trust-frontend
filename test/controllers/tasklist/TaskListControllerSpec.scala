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

package controllers.tasklist

import base.SpecBase
import cats.data.EitherT
import connectors.{TrustConnector, TrustsStoreConnector}
import generators.ModelGenerators
import models.MigrationTaskStatus.NothingToUpdate
import models.errors.{ServerError, TrustErrors}
import models.pages.Tag._
import models.pages.WhatIsNext
import models.{CompletedMaintenanceTasks, FirstTaxYearAvailable, MigrationTaskStatus, TaskList}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.WhatIsNextPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import views.html.{NonTaxToTaxProgressView, VariationProgressView}

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with ModelGenerators {

  private val mockTrustsStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  private val mockTrustsConnector: TrustConnector = mock[TrustConnector]
  private val mockVariationProgress: VariationProgress = mock[VariationProgress]

  override def beforeEach(): Unit = {
    reset[Any](mockTrustsStoreConnector, mockTrustsConnector, mockVariationProgress, mockPlaybackRepository)

    when(mockTrustsStoreConnector.getStatusOfTasks(any())(any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, CompletedMaintenanceTasks](Future.successful(Right(CompletedMaintenanceTasks()))))

    when(mockTrustsConnector.getSettlorsStatus(any())(any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, MigrationTaskStatus](Future.successful(Right(NothingToUpdate))))

    when(mockTrustsConnector.getBeneficiariesStatus(any())(any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, MigrationTaskStatus](Future.successful(Right(NothingToUpdate))))

    when(mockTrustsConnector.getFirstTaxYearToAskFor(any())(any(), any()))
      .thenReturn(EitherT[Future, TrustErrors, FirstTaxYearAvailable]
        (Future.successful(Right(FirstTaxYearAvailable(1, earlierYearsToDeclare = false))))
      )

    when(mockVariationProgress.generateTaskList(any(), any(), any()))
      .thenReturn(TaskList(Nil, Nil))

    when(mockVariationProgress.generateTransitionTaskList(any(), any(), any(), any(), any()))
      .thenReturn(TaskList(Nil, Nil))

    when(mockPlaybackRepository.set(any()))
      .thenReturn(EitherT[Future, TrustErrors, Boolean](Future.successful(Right(true))))
  }

  "TaskListControllerController" must {

    "return OK and the correct view for a GET" when {

      "migrating from non-tax to tax" in {

        val answers = emptyUserAnswersForUrn.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).value

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

          val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).value

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

          val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.CloseTrust).value

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

    "return an Internal Server Error when a connector call fails for a GET" in {

      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).value

      when(mockTrustsConnector.getSettlorsStatus(any())(any(), any()))
        .thenReturn(EitherT[Future, TrustErrors, MigrationTaskStatus](Future.successful(Left(ServerError()))))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind(classOf[TrustConnector]).toInstance(mockTrustsConnector)
        ).build()

      val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentType(result) mustBe Some("text/html")

      application.stop()
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
    }

    "redirect to AgencyRegisteredAddressUkYesNoController for agent" in {

      val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).value

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
        val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.transition.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }

      "not migrating from non-tax to tax" in {
        val answers = emptyUserAnswersForUtr.set(WhatIsNextPage, WhatIsNext.MakeChanges).value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(POST, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

        application.stop()
      }
    }
  }
}
