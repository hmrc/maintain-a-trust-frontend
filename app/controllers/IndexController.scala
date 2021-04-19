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

package controllers

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions.Actions
import models.IdentifierSession
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.ActiveSessionRepository
import services.{FeatureFlagService, UserAnswersSetupService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: Actions,
                                 sessionRepository: ActiveSessionRepository
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = actions.auth.async {
    implicit request =>
      initialise(Redirect(controllers.routes.UTRController.onPageLoad()))
  }

  def startUtr(): Action[AnyContent] = actions.auth.async {
    implicit request =>
      initialise(Redirect(controllers.routes.UTRController.onPageLoad()))
  }

  def startUrn(): Action[AnyContent] = actions.auth.async {
    implicit request =>
      initialise(Redirect(controllers.routes.URNController.onPageLoad()))
  }

  private def getIdentifierFromEnrolment(enrolmentKey: String, identifierKey: String)
                                        (implicit request: IdentifierRequest[_]): Option[String] =
    request.user.enrolments.enrolments
      .find(_.key equals enrolmentKey)
      .flatMap(_.identifiers.find(_.key equals identifierKey))
      .map(_.value)

  private def initialise(redirectForIdentifier: Result)(implicit request: IdentifierRequest[_]): Future[Result] = {

    val utr = getIdentifierFromEnrolment("HMRC-TERS-ORG", "SAUTR")
    val urn = getIdentifierFromEnrolment("HMRC-TERSNT-ORG", "URN")

    val identifier: Option[String] = utr.orElse(urn).orElse(None)

    identifier match {
      case Some(value) =>
        val session = IdentifierSession(request.user.internalId, value)
        for {
          _ <- sessionRepository.set(session)
        } yield {
          Redirect(controllers.routes.TrustStatusController.status())
        }
      case None =>
        logger.info(s"[Session ID: ${Session.id(hc)}]" +
          s" user is not enrolled, starting maintain journey, redirect to ask for identifier")
        Future.successful(redirectForIdentifier)
    }
  }
}
