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
import config.FrontendAppConfig
import controllers.actions.Actions
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.{AgentCannotAccessTrustYetView, InformationMaintainingTaxableTrustView, InformationMaintainingThisTrustView}

@Singleton
class InformationMaintainingThisTrustController @Inject()(
                                                           actions: Actions,
                                                           val controllerComponents: MessagesControllerComponents,
                                                           maintainingTrustView: InformationMaintainingThisTrustView,
                                                           maintainingTaxableTrustView: InformationMaintainingTaxableTrustView,
                                                           agentCannotAccessTrustYetView: AgentCannotAccessTrustYetView
                                                         )(implicit config: FrontendAppConfig)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType
      logger.info(s"[Session ID: ${Session.id(hc)}] showing information about this trust $identifier")

      request.user.affinityGroup match {
        case Agent if !config.playbackEnabled =>
          Ok(agentCannotAccessTrustYetView(identifier, identifierType))
        case _ =>
          val isTrust5mld = request.userAnswers.get(ExpressTrustYesNoPage).isDefined
          if (request.userAnswers.is5mldEnabled && isTrust5mld) {
            Ok(maintainingTaxableTrustView(identifier, identifierType))
          } else {
            Ok(maintainingTrustView(identifier, identifierType))
          }
      }
  }
}
