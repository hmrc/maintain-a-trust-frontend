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

package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.AuthenticateForPlayback
import models.UserAnswers
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class IndexController @Inject()(val controllerComponents: MessagesControllerComponents,
                                actions: AuthenticateForPlayback,
                                playbackRepository: PlaybackRepository
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.auth.async {
    implicit request =>

      request.user.enrolments.enrolments
        .find(_.key equals "HMRC-TERS-ORG")
        .flatMap(_.identifiers.find(_.key equals "SAUTR"))
        .map(_.value)
        .fold {
          Logger.info(s"[IndexController] user is not enrolled, starting maintain journey, redirect to ask for UTR")
          Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
        } {
          utr =>
            for {
              _ <- playbackRepository.resetCache(request.user.internalId)
              newSessionWithUtr <- Future.fromTry {
                Try(UserAnswers.startNewSession(request.user.internalId, utr))
              }
              _ <- playbackRepository.set(newSessionWithUtr)
            } yield {
              Logger.info(s"[IndexController] $utr user is enrolled, storing UTR in user answers, checking status of trust")
              Redirect(controllers.routes.TrustStatusController.status())
            }
        }
  }
}
