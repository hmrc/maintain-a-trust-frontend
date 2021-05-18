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
import models.pages.Tag.{InProgress, UpToDate}
import models.pages.{Tag, WhatIsNext}
import models.{CompletedMaintenanceTasks, EntityStatus, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.WhatIsNextPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sections._
import sections.assets.{Assets, NonEeaBusinessAsset}
import sections.beneficiaries.Beneficiaries
import sections.settlors.Settlors
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import viewmodels.{Link, Task}
import views.html.{NonTaxToTaxProgressView, VariationProgressView}

import scala.concurrent.Future

class TaskListControllerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks {

  lazy val onPageLoad: String = controllers.routes.WhatIsNextController.onPageLoad().url

  lazy val onSubmit: Call = controllers.routes.WhatIsNextController.onSubmit()

  val expectedContinueUrl: String = controllers.declaration.routes.IndividualDeclarationController.onPageLoad().url

  def mandatorySections4mld(identifier: String): List[Task] = List(
    Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), Some(InProgress)),
    Task(Link(Trustees, s"http://localhost:9792/maintain-a-trust/trustees/$identifier"), Some(InProgress)),
    Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), Some(InProgress))
  )

  def mandatorySections5mld(identifier: String): List[Task] =
    Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Some(InProgress)) ::
      mandatorySections4mld(identifier)

  def mandatorySectionsTransitionToTaxable(identifier: String): List[Task] = List(
    Task(Link(TrustDetails, s"http://localhost:9838/maintain-a-trust/trust-details/$identifier"), Some(InProgress)),
    Task(Link(Assets, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), Some(InProgress)),
    Task(Link(TaxLiability, s"http://localhost:9838/maintain-a-trust/tax-liability/$identifier"), Some(InProgress))
  )

  def mandatorySectionsTransitionToTaxableWithSettlorsAndBeneficiaries(identifier: String, status: Tag): List[Task] =
    mandatorySectionsTransitionToTaxable(identifier) ++
      List(
        Task(Link(Settlors, s"http://localhost:9795/maintain-a-trust/settlors/$identifier"), Some(status)),
        Task(Link(Beneficiaries, s"http://localhost:9793/maintain-a-trust/beneficiaries/$identifier"), Some(status))
      )

  def optionalSections4mld(identifier: String): List[Task] = List(
    Task(Link(Protectors, s"http://localhost:9796/maintain-a-trust/protectors/$identifier"), Some(InProgress)),
    Task(Link(Natural, s"http://localhost:9799/maintain-a-trust/other-individuals/$identifier"), Some(InProgress))
  )

  def optionalSections5mld(identifier: String): List[Task] =
    Task(Link(NonEeaBusinessAsset, s"http://localhost:9800/maintain-a-trust/trust-assets/$identifier"), Some(InProgress)) ::
      optionalSections4mld(identifier)

  val mockTrustsStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  val mockTrustsConnector: TrustConnector = mock[TrustConnector]

  override def beforeEach(): Unit = {
    reset(mockTrustsStoreConnector, mockTrustsConnector)

    when(mockTrustsStoreConnector.getStatusOfTasks(any())(any(), any())).thenReturn(Future.successful(CompletedMaintenanceTasks()))

    when(mockTrustsConnector.getSettlorsStatus(any())(any(), any())).thenReturn(Future.successful(EntityStatus(None)))
    when(mockTrustsConnector.getBeneficiariesStatus(any())(any(), any())).thenReturn(Future.successful(EntityStatus(None)))
  }

  "TaskListController Controller" when {

    "in 4mld mode" must {

      val baseAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = false, isUnderlyingData5mld = false)

      val utr = baseAnswers.identifier

      behave like taskListController(baseAnswers, mandatorySections4mld(utr), optionalSections4mld(utr))
    }

    "in 5mld mode" when {

      "underlying trust data is 4mld" must {

        val baseAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isUnderlyingData5mld = false)

        val utr = baseAnswers.identifier

        behave like taskListController(baseAnswers, mandatorySections4mld(utr), optionalSections4mld(utr))
      }

      "underlying trust data is 5mld" when {

        "trust is taxable" must {

          val baseAnswers = emptyUserAnswersForUtr.copy(is5mldEnabled = true, isUnderlyingData5mld = true)

          val utr = baseAnswers.identifier

          behave like taskListController(baseAnswers, mandatorySections5mld(utr), optionalSections5mld(utr))
        }

        "trust is non-taxable" must {

          val baseAnswers = emptyUserAnswersForUrn

          val urn = baseAnswers.identifier

          behave like taskListController(baseAnswers, mandatorySections5mld(urn), optionalSections5mld(urn))
        }

        "trust is non-taxable changing to taxable" must {

          val baseAnswers = emptyUserAnswersForUrn

          behave like transitionTaskListController(baseAnswers)
        }
      }
    }

    def taskListController(baseAnswers: UserAnswers, mandatorySections: List[Task], optionalSections: List[Task]): Unit = {

      "return OK and the correct view for a GET" when {

        "making changes" in {

          val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.MakeChanges).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(
              bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
              bind(classOf[TrustConnector]).toInstance(mockTrustsConnector)
            ).build()

          val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VariationProgressView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(baseAnswers.identifier, baseAnswers.identifierType, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = false)(request, messages).toString

          application.stop()
        }

        "closing the trust" in {

          val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.CloseTrust).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(
              bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
              bind(classOf[TrustConnector]).toInstance(mockTrustsConnector)
            ).build()

          val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[VariationProgressView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(baseAnswers.identifier, baseAnswers.identifierType, mandatorySections, optionalSections, Organisation, expectedContinueUrl, isAbleToDeclare = false, closingTrust = true)(request, messages).toString

          application.stop()
        }
      }

      "redirect to Technical difficulties page when no value found for What do you want to do next" in {

        val answers = baseAnswers

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    def transitionTaskListController(baseAnswers: UserAnswers): Unit = {

      "return OK and the correct view for a GET" when {

        "changing from non taxable to taxable trust" in {

          val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(
              bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
              bind(classOf[TrustConnector]).toInstance(mockTrustsConnector)
            ).build()

          val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NonTaxToTaxProgressView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              identifier = baseAnswers.identifier,
              identifierType = baseAnswers.identifierType,
              mandatory = mandatorySectionsTransitionToTaxable(baseAnswers.identifier),
              affinityGroup = Organisation,
              nextUrl = expectedContinueUrl,
              isAbleToDeclare = false
            )(request, messages).toString

          application.stop()
        }

        "settlors and beneficiaries also need updating" in {

          forAll(arbitrary[Boolean]) {
            bool =>

              val answers = baseAnswers.set(WhatIsNextPage, WhatIsNext.NeedsToPayTax).success.value

              val application = applicationBuilder(userAnswers = Some(answers))
                .overrides(
                  bind(classOf[TrustsStoreConnector]).toInstance(mockTrustsStoreConnector),
                  bind(classOf[TrustConnector]).toInstance(mockTrustsConnector)
                ).build()

              when(mockTrustsConnector.getSettlorsStatus(any())(any(), any())).thenReturn(Future.successful(EntityStatus(Some(bool))))
              when(mockTrustsConnector.getBeneficiariesStatus(any())(any(), any())).thenReturn(Future.successful(EntityStatus(Some(bool))))

              val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[NonTaxToTaxProgressView]

              status(result) mustEqual OK

              val entityStatus = if (bool) UpToDate else InProgress

              contentAsString(result) mustEqual
                view(
                  identifier = baseAnswers.identifier,
                  identifierType = baseAnswers.identifierType,
                  mandatory = mandatorySectionsTransitionToTaxableWithSettlorsAndBeneficiaries(answers.identifier, entityStatus),
                  affinityGroup = Organisation,
                  nextUrl = expectedContinueUrl,
                  isAbleToDeclare = false
                )(request, messages).toString

              application.stop()
          }
        }
      }

      "redirect to Technical difficulties page when no value found for What do you want to do next" in {

        val answers = baseAnswers

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, controllers.tasklist.routes.TaskListController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }
  }
}
