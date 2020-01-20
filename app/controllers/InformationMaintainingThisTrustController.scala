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

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import pages.WhatIsTheUTRVariationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{AgentCannotAccessTrustYetView, InformationMaintainingThisTrustView}

import scala.concurrent.ExecutionContext

class InformationMaintainingThisTrustController @Inject()(
                                                           identify: IdentifierAction,
                                                           getData: DataRetrievalAction,
                                                           requireData: DataRequiredAction,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           maintainingTrustView: InformationMaintainingThisTrustView,
                                                           agentCannotAccessTrustYetView: AgentCannotAccessTrustYetView
                                                         )(implicit ec: ExecutionContext, config: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(WhatIsTheUTRVariationPage) match {
        case Some(utr) =>
          request.affinityGroup match {
            case Agent => Ok(agentCannotAccessTrustYetView(utr))
            case _ => Ok(maintainingTrustView(utr))
          }
        case None => Redirect(routes.WhatIsTheUTRVariationsController.onPageLoad())
      }
  }
}
