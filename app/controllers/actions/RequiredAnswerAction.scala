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

package controllers.actions

import com.google.inject.Inject
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Call, Result}
import queries.Gettable

import scala.concurrent.{ExecutionContext, Future}

final case class RequiredAnswer[T](answer: Gettable[T],
                                   redirect: Call = controllers.routes.SessionExpiredController.onPageLoad())

class RequiredAnswerAction[T] @Inject()(required: RequiredAnswer[T])
                                       (implicit val executionContext: ExecutionContext,
                                        val reads: Reads[T]) extends ActionRefiner[DataRequest, DataRequest] {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers.get(required.answer) match {
      case None =>
        Future.successful(Left(Redirect(required.redirect)))
      case Some(_) =>
        Future.successful(Right(request))
    }
  }
}

class RequiredAnswerActionProvider @Inject()(implicit ec: ExecutionContext) {

  def apply[T](required: RequiredAnswer[T])(implicit reads: Reads[T]) =
    new RequiredAnswerAction(required)
}