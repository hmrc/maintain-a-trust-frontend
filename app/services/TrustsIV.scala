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

package services

import controllers.actions.TrustsAuthorisedFunctions
import com.google.inject.Inject
import models.requests.DataRequest
import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core.{BusinessKey, Enrolment, FailedRelationship, InsufficientEnrolments, Relationship}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TrustsIV @Inject()(trustsAuth: TrustsAuthorisedFunctions) {

  def authenticate[A](utr: String,
                      onIVRelationshipExisting: Future[Either[Result, DataRequest[A]]],
                      onIVRelationshipNotExisting: Future[Either[Result, DataRequest[A]]]
                     )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Result, DataRequest[A]]] = {

    val trustIVRelationship =
      Relationship(trustsAuth.config.relationshipName, Set(BusinessKey(trustsAuth.config.relationshipIdentifier, utr)))

    trustsAuth.authorised(trustIVRelationship) {
      onIVRelationshipExisting
    } recoverWith {
      case FailedRelationship(msg) =>
        Logger.info(s"[IdentifyForPlayback] Relationship does not exist in Trust IV for user due to $msg")
        onIVRelationshipNotExisting
    }
  }

}

class AgentAuthorisedForDelegatedEnrolment @Inject()(trustsAuth: TrustsAuthorisedFunctions) {

  def authenticate[A](utr: String)
                     (implicit hc: HeaderCarrier,
                      ec: ExecutionContext,
                      request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    val predicate = Enrolment("HMRC-TERS-ORG")
        .withIdentifier("SAUTR", utr)
        .withDelegatedAuthRule("trust-auth")

    trustsAuth.authorised(predicate) {
      Logger.info(s"[AgentAuthorisedForDelegatedEnrolment] agent is authorised for delegated enrolment")
      Future.successful(Right(request))
    } recover {
      case _ : InsufficientEnrolments =>
        Logger.info(s"[AgentAuthorisedForDelegatedEnrolment] agent is not authorised for delegated enrolment")
        Left(Redirect(controllers.routes.AgentNotAuthorisedController.onPageLoad()))
      case _ =>
        Logger.info(s"[AgentAuthorisedForDelegatedEnrolment] agent is not authorised")
        Left(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
    }
  }

}

