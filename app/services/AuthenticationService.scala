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

package services

import com.google.inject.Inject
import connectors.TrustAuthConnector
import handlers.ErrorHandler
import models.requests.DataRequest
import models.{TrustAuthAgentAllowed, TrustAuthAllowed, TrustAuthDenied}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Session
import scala.concurrent.{ExecutionContext, Future}

class AuthenticationServiceImpl @Inject()(errorHandler: ErrorHandler,
                                         trustAuthConnector: TrustAuthConnector)
                                         (implicit ec: ExecutionContext)
  extends AuthenticationService with Logging {

  override def authenticateAgent[A]()
                                   (implicit request: Request[A], hc: HeaderCarrier): Future[Either[Result, String]] = {
    trustAuthConnector.agentIsAuthorised().flatMap {
      case TrustAuthAgentAllowed(arn) =>
        Future.successful(Right(arn))
      case TrustAuthDenied(redirectUrl) =>
        Future.successful(Left(Redirect(redirectUrl)))
      case _ =>
        logger.warn(s"[AuthenticationServiceImpl][authenticateAgent][Session ID: ${Session.id(hc)}] Unable to authenticate agent with trusts-auth")
        errorHandler.internalServerErrorTemplate.map { html =>
          Left(InternalServerError(html))
        }
    }
  }


  override def authenticateForIdentifier[A](identifier: String)
                                           (implicit request: DataRequest[A],
                                            hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]] = {
    trustAuthConnector.authorisedForIdentifier(identifier).flatMap {
      case _: TrustAuthAllowed =>
        Future.successful(Right(request))
      case TrustAuthDenied(redirectUrl) =>
        Future.successful(Left(Redirect(redirectUrl)))
      case _ =>
        logger.warn(
          s"[AuthenticationServiceImpl][authenticateForIdentifier]" +
            s"[Session ID: ${Session.id(hc)}][UTR/URN: $identifier] Unable to authenticate organisation with trusts-auth"
        )
        errorHandler.internalServerErrorTemplate.map { html =>
          Left(InternalServerError(html))
        }
    }
  }


}

trait AuthenticationService {

  def authenticateAgent[A]()
                          (implicit request: Request[A], hc: HeaderCarrier): Future[Either[Result, String]]

  def authenticateForIdentifier[A](identifier: String)
                                  (implicit request: DataRequest[A],
                                   hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]]
}
