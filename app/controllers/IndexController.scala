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

import java.time.LocalDate

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions.AuthenticateForPlayback
import models.UserAnswers
import pages.{StartDatePage, UTRPage}
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class IndexController @Inject()(val controllerComponents: MessagesControllerComponents,
                                actions: AuthenticateForPlayback,
                                playbackRepository: PlaybackRepository,
                                connector: TrustConnector
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.authWithSession.async {
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
            resetAnswersAndRedirect(utr, request.user.internalId)
        }
  }

  def saveStartDate(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>

      request.userAnswers.get(UTRPage) match {
        case Some(utr) =>
          resetAnswersAndRedirect(utr, request.user.internalId)
        case None =>
          Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }

  private def resetAnswersAndRedirect(utr: String,
                                      internalId: String
                                     )(implicit hc: HeaderCarrier): Future[Result] = {

    def newUserAnswers(startDate: LocalDate): Try[UserAnswers] = UserAnswers(internalId)
      .set(UTRPage, utr)
      .flatMap(_.set(StartDatePage, startDate))

    for {
      _ <- playbackRepository.resetCache(internalId)
      details <- connector.getTrustDetails(utr)
      startDate = LocalDate.parse(details.startDate)
      userAnswers <- Future.fromTry(newUserAnswers(startDate))
      _ <- playbackRepository.set(userAnswers)
    } yield {
      Logger.info(s"[IndexController] $utr user is enrolled, storing UTR and StartDate in user answers, checking status of trust")
      Redirect(controllers.routes.TrustStatusController.status())
    }
  }
}
