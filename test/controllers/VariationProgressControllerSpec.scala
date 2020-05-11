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
import models.pages.Tag.{InProgress, UpToDate}
import pages.UTRPage
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import viewmodels.tasks.{Beneficiaries, NaturalPeople, Settlors, Trustees}
import viewmodels.{Link, Task}
import views.html.VariationProgressView
import play.api.inject.bind
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future

class VariationProgressControllerSpec extends SpecBase {

  lazy val onPageLoad: String = routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = routes.WhatIsNextController.onSubmit()

  val expectedContinueUrl = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  val mandatorySections = List(
    Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$utr"), Some(InProgress)),
    Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$utr"), Some(InProgress)),
    Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$utr"), Some(InProgress))
  )
  val optionalSections = List(
    Task(Link(NaturalPeople, controllers.routes.FeatureNotAvailableController.onPageLoad().url), Some(UpToDate)))

  "VariationProgress Controller" must {

    "return OK and the correct view for a GET" in {

      val mockConnector = mock[TrustsStoreConnector]

      val answers = emptyUserAnswers.set(UTRPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          Seq(
            bind(classOf[TrustsStoreConnector]).toInstance(mockConnector)
          )
        )
        .build()

      when(mockConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

      val request = FakeRequest(GET, routes.VariationProgressController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[VariationProgressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(utr, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false)(fakeRequest, messages).toString

      application.stop()
    }

  }
}
