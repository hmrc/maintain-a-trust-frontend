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
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{FeatureFlagService, UserAnswersSetupService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: Actions,
                                 uaSetupService: UserAnswersSetupService,
                                 featureFlagService: FeatureFlagService,
                                 trustsConnector: TrustConnector
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

  private def initialise(redirect: Result)(implicit request: IdentifierRequest[_]): Future[Result] = {

    def getIdentifierFromEnrolment(enrolmentKey: String, identifierKey: String): Option[String] = request.user.enrolments.enrolments
      .find(_.key equals enrolmentKey)
      .flatMap(_.identifiers.find(_.key equals identifierKey))
      .map(_.value)

    val utr = getIdentifierFromEnrolment("HMRC-TERS-ORG", "SAUTR")
    val urn = getIdentifierFromEnrolment("HMRC-TERSNT-ORG", "URN")

    val identifier = (utr, urn) match {
      case (Some(_), None) => utr
      case (None, Some(_)) => urn
      case _ => None
    }

    identifier match {
      case Some(value) =>
        for {
          is5mldEnabled <- featureFlagService.is5mldEnabled()
          isUnderlyingData5mld <- trustsConnector.isTrust5mld(value)
          result <- uaSetupService.setupAndRedirectToStatus(value, request.user.internalId, is5mldEnabled, isUnderlyingData5mld)
        } yield {
          result
        }
      case None =>
        logger.info(s"[Session ID: ${Session.id(hc)}]" +
          s" user is not enrolled, starting maintain journey, redirect to ask for identifier")
        Future.successful(redirect)
    }
  }
}
