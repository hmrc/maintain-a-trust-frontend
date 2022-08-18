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

package controllers

import com.google.inject.{Inject, Singleton}
import controllers.actions.Actions
import models.pages.WhatIsNext.MakeChanges
import pages.WhatIsNextPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html._

import scala.concurrent.Future

@Singleton
class MigrateTo5mldInformationController @Inject()(
                                                    actions: Actions,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: MigrateTo5mldInformationView
                                                  ) extends FrontendBaseController with I18nSupport with Logging {

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier {
    Redirect(controllers.transition.routes.ExpressTrustYesNoController.onPageLoad())
  }

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>
      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType
      val isAgent = request.user.affinityGroup == Agent
      Ok(view(identifier,identifierType,isAgent))
  }
}
