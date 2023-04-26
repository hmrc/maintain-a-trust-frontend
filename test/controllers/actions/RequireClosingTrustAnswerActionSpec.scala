/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.actions

import base.SpecBase
import handlers.ErrorHandler
import models.pages.WhatIsNext.{CloseTrust, MakeChanges}
import models.requests.{ClosingTrustRequest, DataRequest, OrganisationUser, User}
import org.scalatest.concurrent.ScalaFutures
import pages.WhatIsNextPage
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.Future

class RequireClosingTrustAnswerActionSpec extends SpecBase with ScalaFutures {

  private val handler = injector.instanceOf[ErrorHandler]

  class Harness() extends RequireClosingTrustAnswerAction(handler) {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, ClosingTrustRequest[A]]] = refine(request)
  }

  private val user: User = OrganisationUser("id", Enrolments(Set()))

  "'What do you want to do next?' required answer Action" when {

    "there is no answer" must {

      "redirect to Session Expired" in {

        val action = new Harness()

        val futureResult = action.callRefine(
          DataRequest(
            fakeRequest,
            emptyUserAnswersForUtr,
            user
          )
        )

        whenReady(futureResult) { r =>
          val result = Future.successful(r.left.get)
          status(result) mustEqual INTERNAL_SERVER_ERROR
        }
      }
    }

    "closing the trust" must {

      "add the answer to the request" in {

        val action = new Harness()

        val userAnswers = emptyUserAnswersForUtr.set(WhatIsNextPage, CloseTrust).value

        val futureResult = action.callRefine(
          DataRequest(
            fakeRequest,
            userAnswers,
            user
          )
        )

        whenReady(futureResult) { result =>
          result.right.get.closingTrust must be(true)
        }
      }
    }

    "not closing the trust" must {

      "add the answer to the request" in {

        val action = new Harness()

        val userAnswers = emptyUserAnswersForUtr.set(WhatIsNextPage, MakeChanges).value

        val futureResult = action.callRefine(
          DataRequest(
            fakeRequest,
            userAnswers,
            user
          )
        )

        whenReady(futureResult) { result =>
          result.right.get.closingTrust must be(false)
        }
      }
    }
  }

}
