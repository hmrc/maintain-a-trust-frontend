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

import javax.inject.Inject
import models.pages.WhatIsNext
import models.requests.{DataRequest, WhatNextRequest}
import pages.WhatIsNextPage
import play.api.i18n.MessagesApi
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Call, Result}
import queries.Gettable

import scala.concurrent.{ExecutionContext, Future}

case class RequiredAnswer(answer: Gettable[WhatIsNext],
                          redirect: Call = controllers.routes.SessionExpiredController.onPageLoad())

class WhatNextRequiredAction @Inject()(required: RequiredAnswer)
                                      (val executionContext: ExecutionContext, val messagesApi: MessagesApi)
  extends ActionRefiner[DataRequest, WhatNextRequest] {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, WhatNextRequest[A]]] = {

    request.userAnswers.get(WhatIsNextPage) match {
      case None =>
        Future.successful(Left(Redirect(required.redirect)))
      case Some(value) =>
        Future.successful(Right(WhatNextRequest(request, value)))
    }
  }
}
