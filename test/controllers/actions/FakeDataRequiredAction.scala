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

package controllers.actions

import com.google.inject.Inject
import controllers.routes
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRequiredAction @Inject()(userAnswers: Option[UserAnswers])
                                      (implicit executionContext: ExecutionContext) extends DataRequiredAction {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    userAnswers match {
      case Some(answers) => Future.successful(Right(DataRequest(request.request, answers, request.user)))
      case None => Future.successful(Left(Redirect(routes.SessionExpiredController.onPageLoad)))
    }
  }
}
