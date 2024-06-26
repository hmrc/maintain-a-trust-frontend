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

package controllers.close

import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import controllers.makechanges.MakeChangesQuestionRouterController
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.close.BeforeClosingView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BeforeClosingController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         actions: Actions,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: BeforeClosingView,
                                         trustConnector: TrustConnector,
                                         trustsStoreConnector: TrustsStoreConnector
                                       )(implicit ec: ExecutionContext)
  extends MakeChangesQuestionRouterController(trustConnector, trustsStoreConnector) {

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>
      Ok(view())
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>
      Redirect(redirectToFirstUpdateQuestion)
  }
}
