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

package controllers

import base.SpecBase
import connectors.TrustsStoreConnector
import models.CompletedMaintenanceTasks
import models.pages.Tag.InProgress
import models.pages.WhatIsNext
import org.mockito.Matchers.any
import org.mockito.Mockito._
import pages.WhatIsNextPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sections.Protectors
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import viewmodels.tasks.{Beneficiaries, NaturalPeople, Settlors, Trustees}
import viewmodels.{Link, Task}
import views.html.VariationProgressView

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()

  val fakeUTR = "utr"

  val expectedContinueUrl = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  val mandatorySections = List(
    Task(Link(Settlors, "http://localhost:9795/maintain-a-trust/settlors/utr"), Some(InProgress)),
    Task(Link(Trustees, "http://localhost:9792/maintain-a-trust/trustees/utr"), Some(InProgress)),
    Task(Link(Beneficiaries, "http://localhost:9793/maintain-a-trust/beneficiaries/utr"), Some(InProgress))
  )
  val optionalSections = List(
    Task(Link(Protectors, "http://localhost:9796/maintain-a-trust/protectors/utr"), Some(InProgress)),
    Task(Link(NaturalPeople, "http://localhost:9799/maintain-a-trust/other-individuals/utr"), Some(InProgress))
  )

  "TaskListController Controller" must {

    "return OK and the correct view for a GET when making changes" in {

      val mockConnector = mock[TrustsStoreConnector]

      val answers = emptyUserAnswers
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
        view(fakeUTR, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = false)(request, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET when closing the trust" in {

      val mockConnector = mock[TrustsStoreConnector]

      val answers = emptyUserAnswers
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
        view(fakeUTR, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = true)(request, messages).toString

      application.stop()
    }

    "redirect to session expired page when no value found for What do you want to do next" in {

      val answers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, controllers.task_list.routes.TaskListController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }


  }
}
