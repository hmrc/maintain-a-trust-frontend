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

package controllers.print

import controllers.actions.Actions
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PrintPlaybackHelper
import views.html.print.PrintMaintainDraftAnswersView

import scala.concurrent.Future

class PrintMaintainDraftAnswersController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     actions: Actions,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: PrintMaintainDraftAnswersView,
                                                     printPlaybackAnswersHelper: PrintPlaybackHelper
                                                   ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      val closeDate = printPlaybackAnswersHelper.closeDate(request.userAnswers)

      val entities = printPlaybackAnswersHelper.entities(request.userAnswers)

      val trustDetails = printPlaybackAnswersHelper.trustDetails(request.userAnswers)

      Future.successful(Ok(view(closeDate, entities, trustDetails)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer {
    _ =>
      Redirect(controllers.tasklist.routes.TaskListController.onPageLoad())
  }

}
