/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.actions.{Actions, RequireClosingTrustAnswerAction}
import pages.TVNPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.declaration.{CloseTrustConfirmationView, ConfirmationView}

@Singleton
class ConfirmationController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        actions: Actions,
                                        val controllerComponents: MessagesControllerComponents,
                                        confirmationView: ConfirmationView,
                                        closeTrustConfirmationView: CloseTrustConfirmationView,
                                        config: FrontendAppConfig,
                                        answerRequiredAction: RequireClosingTrustAnswerAction
                                      ) extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName

  def onPageLoad(): Action[AnyContent] = actions.refreshedData.andThen(answerRequiredAction) {
    implicit request =>

      val isAgent = request.user.affinityGroup == Agent

      request.userAnswers.get(TVNPage).fold {
        logger.warn(s"[$className][onPageLoad][UTR/URN: ${request.userAnswers.identifier}] no TVN in user answers, cannot render confirmation page")
        Redirect(controllers.routes.TrustStatusController.sorryThereHasBeenAProblem())
      }{
        tvn =>
          Ok(
            if (request.closingTrust) {
              closeTrustConfirmationView(tvn, isAgent, agentOverviewUrl = config.agentOverviewUrl)
            } else {
              confirmationView(tvn, isAgent, request.userAnswers.isTrustTaxable, agentOverviewUrl = config.agentOverviewUrl)
            }
          )
      }
  }
}
