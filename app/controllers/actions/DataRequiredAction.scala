/*
 * Copyright 2022 HM Revenue & Customs
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
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject()(implicit val executionContext: ExecutionContext) extends DataRequiredAction with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.userAnswers match {
      case None =>
        logger.warn(s"[DataRequiredActionImpl][refine] [Session ID: ${Session.id(hc)}] no user answers for session in mongo, cannot continue with session")
        Future.successful(Left(Redirect(routes.SessionExpiredController.onPageLoad())))
      case Some(data) =>
        logger.info(s"[DataRequiredActionImpl][refine] [Session ID: ${Session.id(hc)}][UTR/URN: ${data.identifier}] user answers in request, continuing with journey")
        Future.successful(Right(DataRequest(request.request, data, request.user)))
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
