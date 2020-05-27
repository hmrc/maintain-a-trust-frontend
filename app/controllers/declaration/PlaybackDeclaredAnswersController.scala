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

package controllers.declaration

import java.time.LocalDateTime

import controllers.actions.{AuthenticateForPlayback, WhatNextRequiredAction}
import javax.inject.Inject
import models.pages.WhatIsNext.CloseTrust
import pages.declaration.AgentDeclarationPage
import pages.{SubmissionDatePage, TVNPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.DateFormatter
import utils.print.PrintPlaybackHelper
import views.html.declaration.{PlaybackDeclaredAnswersView, PlaybackFinalDeclaredAnswersView}

import scala.concurrent.{ExecutionContext, Future}

class PlaybackDeclaredAnswersController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   actions: AuthenticateForPlayback,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   declaredAnswersView: PlaybackDeclaredAnswersView,
                                                   finalDeclaredAnswersView: PlaybackFinalDeclaredAnswersView,
                                                   printPlaybackAnswersHelper: PrintPlaybackHelper,
                                                   dateFormatter: DateFormatter
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.requireAnswer.async {
    implicit request =>

      val entities = printPlaybackAnswersHelper.entities(request.userAnswers)

      val trustDetails = printPlaybackAnswersHelper.trustDetails(request.userAnswers)

      val tvn = request.userAnswers.get(TVNPage).getOrElse("")

      val crn = request.userAnswers.get(AgentDeclarationPage).map(_.crn).getOrElse("")

      val trnDateTime = request.userAnswers.get(SubmissionDatePage).getOrElse(LocalDateTime.now)

      val declarationSent : String = dateFormatter.formatDate(trnDateTime)

      val isAgent = request.user.affinityGroup == Agent

      Future.successful(Ok(
        request.whatIsNext match {
          case CloseTrust =>
            val closeDate = printPlaybackAnswersHelper.closeDate(request.userAnswers)
            finalDeclaredAnswersView(closeDate, entities, trustDetails, tvn, crn, declarationSent, isAgent)
          case _ =>
            declaredAnswersView(entities, trustDetails, tvn, crn, declarationSent, isAgent)
        }
      ))
  }

}