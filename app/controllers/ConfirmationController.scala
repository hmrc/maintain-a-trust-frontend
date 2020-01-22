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

import controllers.actions.AuthenticateForPlayback
import com.google.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.ConfirmationView

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmationController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        actions: AuthenticateForPlayback,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ConfirmationView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad() = actions.verifiedForUtr {
    implicit request =>

      val isAgent = request.user.affinityGroup == Agent
      //      val agentOverviewUrl = controllers.register.agents.routes.AgentOverviewController.onPageLoad().url

      val fakeTvn = "XC TVN 000 000 4912"

      Ok(view(fakeTvn, isAgent, agentOverviewUrl = "#"))
  }
}
