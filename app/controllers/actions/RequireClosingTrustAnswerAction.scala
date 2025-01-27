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
import handlers.ErrorHandler
import models.pages.WhatIsNext.CloseTrust
import models.requests.{ClosingTrustRequest, DataRequest}
import pages.WhatIsNextPage
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result, Results}

import scala.concurrent.{ExecutionContext, Future}

class RequireClosingTrustAnswerAction @Inject()(errorHandler: ErrorHandler)
                                               (implicit val executionContext: ExecutionContext)
  extends ActionRefiner[DataRequest, ClosingTrustRequest] with Logging {

  private val className = getClass.getSimpleName

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, ClosingTrustRequest[A]]] = {
    request.userAnswers.get(WhatIsNextPage) match {
      case None =>
        logger.warn(s"[$className][refine] [UTR/URN: ${request.userAnswers.identifier}] " +
          s"no answer for 'What next' found in user answers, cannot determine if user is closing the trust, cannot continue with journey")

        errorHandler.internalServerErrorTemplate(request.request).map { html =>
          Left(Results.InternalServerError(html))
        }.recover {
          case ex: Throwable =>
            // Log the error and provide a fallback
            logger.error(s"[$className][refine] Failed to render internal server error template", ex)
            Left(Results.InternalServerError("An unexpected error occurred"))
        }

      case Some(value) =>
        Future.successful(Right(ClosingTrustRequest(request, value == CloseTrust)))
    }
  }

}
