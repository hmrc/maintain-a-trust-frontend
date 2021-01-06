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
import models.{UserAnswers, UtrSession}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.{ActiveSessionRepository, PlaybackRepository}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndexController @Inject()(val controllerComponents: MessagesControllerComponents,
                                actions: Actions,
                                playbackRepository: PlaybackRepository,
                                sessionRepository: ActiveSessionRepository
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = actions.auth.async {
    implicit request =>

      request.user.enrolments.enrolments
        .find(_.key equals "HMRC-TERS-ORG")
        .flatMap(_.identifiers.find(_.key equals "SAUTR"))
        .map(_.value)
        .fold {
          logger.info(s"[Session ID: ${Session.id(hc)}]" +
            s" user is not enrolled, starting maintain journey, redirect to ask for UTR")
          Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
        } {
          utr =>

            val activeSession = UtrSession(request.user.internalId, utr)
            val newEmptyAnswers = UserAnswers.startNewSession(request.user.internalId, utr)

            for {
              _ <- playbackRepository.resetCache(utr, request.user.internalId)
              _ <- playbackRepository.set(newEmptyAnswers)
              _ <- sessionRepository.set(activeSession)
            } yield {
              logger.info(s"[Session ID: ${Session.id(hc)}]" +
                s" $utr user is enrolled, storing UTR in user answers, checking status of trust")
              Redirect(controllers.routes.TrustStatusController.status())
            }
        }
  }
}
