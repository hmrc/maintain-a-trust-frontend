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

package controllers.actions

import base.SpecBase
import models.pages.WhatIsNext.MakeChanges
import models.requests.{DataRequest, OrganisationUser, User, WhatNextRequest}
import org.scalatest.concurrent.ScalaFutures
import pages.WhatIsNextPage
import play.api.mvc.Result
import uk.gov.hmrc.auth.core.Enrolments
import play.api.test.Helpers._

import scala.concurrent.Future

class WhatNextRequiredActionSpec extends SpecBase with ScalaFutures {

  class Harness() extends WhatNextRequiredAction {
    def callRefine[A](request: DataRequest[A]): Future[Either[Result, WhatNextRequest[A]]] = refine(request)
  }

  private val user: User = OrganisationUser("id", Enrolments(Set()))

  "'What do you want to do next?' required answer Action" when {

    "there is no answer" must {

      "redirect to Session Expired" in {

        val action = new Harness()

        val futureResult = action.callRefine(
          DataRequest(
            fakeRequest,
            emptyUserAnswers,
            user
          )
        )

        whenReady(futureResult) { r =>
          val result = Future.successful(r.left.get)
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
        }
      }
    }

    "there is an answer" must {

      "add the answer to the request" in {

        val action = new Harness()

        val userAnswers = emptyUserAnswers.set(WhatIsNextPage, MakeChanges).success.value

        val futureResult = action.callRefine(
          DataRequest(
            fakeRequest,
            userAnswers,
            user
          )
        )

        whenReady(futureResult) { result =>
          result.right.get.whatIsNext mustEqual MakeChanges
        }
      }
    }
  }

}
