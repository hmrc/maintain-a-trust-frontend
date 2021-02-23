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
import controllers.actions.Actions
import models.{IdentifierSession, UserAnswers}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.{ActiveSessionRepository, PlaybackRepository}
import services.{FeatureFlagService, UserAnswersSetupService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(val controllerComponents: MessagesControllerComponents,
                                actions: Actions,
                                uaSetupService: UserAnswersSetupService,
                                featureFlagService: FeatureFlagService
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = actions.auth.async {
    implicit request =>

      val utr = request.user.enrolments.enrolments
        .find(_.key equals "HMRC-TERS-ORG")
        .flatMap(_.identifiers.find(_.key equals "SAUTR"))
        .map(_.value)

      val urn = request.user.enrolments.enrolments
        .find(_.key equals "HMRC-TERSNT-ORG")
        .flatMap(_.identifiers.find(_.key equals "URN"))
        .map(_.value)

      featureFlagService.is5mldEnabled().flatMap {
        is5mldEnabled =>
          (utr, urn) match {
            case (Some(utr), _) => uaSetupService.setupAndRedirectToStatus(utr, request.user.internalId, is5mldEnabled)
            case (_, Some(urn)) => uaSetupService.setupAndRedirectToStatus(urn, request.user.internalId, is5mldEnabled)
            case _ =>
              if (is5mldEnabled) {
                logger.info(s"[Session ID: ${Session.id(hc)}]" +
                  s" user is not enrolled, starting 5mld maintain journey, redirect to ask for identifier")
                Future.successful(Redirect(controllers.routes.WhichIdentifierController.onPageLoad()))
              } else {
                logger.info(s"[Session ID: ${Session.id(hc)}]" +
                  s" user is not enrolled, starting maintain journey, redirect to ask for UTR")
                Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
              }
          }
      }
  }


}
