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

package controllers.tasklist

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TrustsStoreConnector
import controllers.actions.Actions
import models.{Enumerable, MigratingFromNonTaxableToTaxable}
import navigation.Navigator.declarationUrl
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{TransitionProgressView, VariationProgressView}

import scala.concurrent.ExecutionContext

class TaskListController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    actions: Actions,
                                    view: VariationProgressView,
                                    transitionView: TransitionProgressView,
                                    val controllerComponents: MessagesControllerComponents,
                                    val config: FrontendAppConfig,
                                    storeConnector: TrustsStoreConnector
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with Enumerable.Implicits with TaskListSections {

  def onPageLoad(): Action[AnyContent] = actions.refreshAndRequireIsClosingAnswer.async {
    implicit request =>

      val identifier = request.userAnswers.identifier

      storeConnector.getStatusOfTasks(identifier) map {
        tasks =>

          val sections = generateTaskList(tasks, identifier, request.userAnswers.trustMldStatus, request.userAnswers.trustTaxability)

          if (request.userAnswers.trustTaxability == MigratingFromNonTaxableToTaxable) {
            Ok(transitionView(identifier,
              identifierType = request.userAnswers.identifierType,
              mandatory = sections.mandatory,
              affinityGroup = request.user.affinityGroup,
              nextUrl = declarationUrl(request.user.affinityGroup),
              isAbleToDeclare = sections.isAbleToDeclare,
              closingTrust = request.closingTrust
            ))
          } else {
            Ok(view(identifier,
              identifierType = request.userAnswers.identifierType,
              mandatory = sections.mandatory,
              optional = sections.other,
              affinityGroup = request.user.affinityGroup,
              nextUrl = declarationUrl(request.user.affinityGroup),
              isAbleToDeclare = sections.isAbleToDeclare,
              closingTrust = request.closingTrust
            ))
          }


      }
  }
}
