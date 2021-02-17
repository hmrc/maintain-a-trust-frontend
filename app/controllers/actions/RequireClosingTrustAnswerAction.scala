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
import handlers.ErrorHandler
import models.pages.WhatIsNext.CloseTrust
import models.requests.{ClosingTrustRequest, DataRequest}
import pages.WhatIsNextPage
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result, Results}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class RequireClosingTrustAnswerAction @Inject()(errorHandler: ErrorHandler)
                                               (implicit val executionContext: ExecutionContext)
  extends ActionRefiner[DataRequest, ClosingTrustRequest] with Logging {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, ClosingTrustRequest[A]]] = {

    val hc = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

    Future.successful(
      request.userAnswers.get(WhatIsNextPage) match {
        case None =>
          logger.error(s"[Session ID: ${Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}] " +
            s"no answer for 'What next' found in user answers, cannot determine if user is closing the trust, cannot continue with journey")
          Left(
            Results.InternalServerError(errorHandler.internalServerErrorTemplate(request.request))
          )
        case Some(value) =>
          Right(
            ClosingTrustRequest(
              request,
              value == CloseTrust
            )
          )
      }
    )
  }
}
