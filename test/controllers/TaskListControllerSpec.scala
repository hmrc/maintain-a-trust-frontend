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
import connectors.TrustsStoreConnector
import models.{CompletedMaintenanceTasks, UTR}
import models.pages.Tag.InProgress
import models.pages.WhatIsNext
import org.mockito.Matchers.any
import org.mockito.Mockito._
import pages.WhatIsNextPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sections.Protectors
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import viewmodels.tasks.{Beneficiaries, NaturalPeople, NonEeaCompany, Settlors, Trustees}
import viewmodels.{Link, Task}
import views.html.VariationProgressView

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()

  val utr: String = "1234567890"

  val expectedContinueUrl = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  val mandatorySections = List(
    Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$utr"), Some(InProgress)),
    Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$utr"), Some(InProgress)),
    Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$utr"), Some(InProgress))
  )
  val optionalSections = List(
    Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$utr"), Some(InProgress)),
    Task(Link(NaturalPeople, s"http://localhost:9799/maintain-a-trust/other-individuals/$utr"), Some(InProgress))
  )

  val optionalSections5mld = List(
    Task(Link(NonEeaCompany, s"http://localhost:9796/maintain-a-trust/feature-not-available"), Some(InProgress)),
    Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$utr"), Some(InProgress)),
    Task(Link(NaturalPeople, s"http://localhost:9799/maintain-a-trust/other-individuals/$utr"), Some(InProgress))
  )

  "TaskListController Controller" when {

    "in 4mld mode" must {

      "return OK and the correct view for a GET when making changes" in {

        val mockConnector = mock[TrustsStoreConnector]

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            Seq(
              bind(classOf[TrustsStoreConnector]).toInstance(mockConnector)
            )
          )
          .build()

        when(mockConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VariationProgressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "return OK and the correct view for a GET when closing the trust" in {

        val mockConnector = mock[TrustsStoreConnector]

        val answers = emptyUserAnswersForUtr
          .set(WhatIsNextPage, WhatIsNext.CloseTrust).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            Seq(
              bind(classOf[TrustsStoreConnector]).toInstance(mockConnector)
            )
          )
          .build()

        when(mockConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VariationProgressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "redirect to Technical difficulties page when no value found for What do you want to do next" in {

        val answers = emptyUserAnswersForUtr

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

    }

    "in 5mld mode for a 5mld taxable trust" must {

      val baseAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isTrustTaxable = true)
        .set(ExpressTrustYesNoPage, false).success.value

      "return OK and the correct view for a GET when making changes" in {

        val mockConnector = mock[TrustsStoreConnector]

        val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            Seq(
              bind(classOf[TrustsStoreConnector]).toInstance(mockConnector)
            )
          )
          .build()

        when(mockConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))
println("*********************************")
println(CompletedMaintenanceTasks())
println("*********************************")
        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VariationProgressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR, mandatorySections, optionalSections5mld, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = false)(request, messages).toString

        application.stop()
      }

      "return OK and the correct view for a GET when closing the trust" in {

        val mockConnector = mock[TrustsStoreConnector]

        val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.CloseTrust).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            Seq(
              bind(classOf[TrustsStoreConnector]).toInstance(mockConnector)
            )
          )
          .build()

        when(mockConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[VariationProgressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(utr, UTR, mandatorySections, optionalSections5mld, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = true)(request, messages).toString

        application.stop()
      }

      "redirect to Technical difficulties page when no value found for What do you want to do next" in {

        val answers = baseAnswers

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

    }
  }
}
