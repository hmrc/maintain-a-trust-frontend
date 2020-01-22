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

import com.google.inject.Inject
import controllers.actions.AuthenticateForPlayback
import models.requests.DataRequest
import pages.UTRPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.TrustClaimedView

import scala.concurrent.{ExecutionContext, Future}

class TrustAlreadyClaimedController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: AuthenticateForPlayback,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: TrustClaimedView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(): Action[AnyContent] = actions.authWithData.async {
    implicit request =>
      enforceUtr() { utr =>
        Future.successful(Ok(view(utr)))
      }
  }

  private def enforceUtr()(block: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(UTRPage) match {
      case None => Future.successful(Redirect(routes.UTRController.onPageLoad()))
      case Some(utr) => block(utr)
    }
  }

}
