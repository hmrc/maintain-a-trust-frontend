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

package controllers

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.actions.Actions
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MaintainThisTrustView

@Singleton
class MaintainThisTrustController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             actions: Actions,
                                             val controllerComponents: MessagesControllerComponents,
                                             config: FrontendAppConfig,
                                             view: MaintainThisTrustView
                                           ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(needsIv: Boolean): Action[AnyContent] = actions.authWithData {
    implicit request =>

      val identifier = request.userAnswers.identifier
      val identifierType = request.userAnswers.identifierType

      @scala.annotation.tailrec
      def commaSeparate(connective: String, list: List[String], acc: String = "")
                       (implicit request: DataRequest[AnyContent]): String = {
        list.size match {
          case 0 => acc
          case 1 if acc.nonEmpty => commaSeparate(connective, list.tail, acc + " " + connective + " " + list.head)
          case 1 | 2 => commaSeparate(connective, list.tail, acc + list.head)
          case _ => commaSeparate(connective, list.tail, acc + list.head + ", ")
        }
      }

      val sections: List[(Boolean, String)] = List(
        (config.maintainSettlorsEnabled, request.messages(messagesApi)("section.settlors")),
        (config.maintainTrusteesEnabled, request.messages(messagesApi)("section.trustees")),
        (config.maintainBeneficiariesEnabled, request.messages(messagesApi)("section.beneficiaries")),
        (config.maintainProtectorsEnabled, request.messages(messagesApi)("section.protectors")),
        (config.maintainOtherIndividualsEnabled, request.messages(messagesApi)("section.otherIndividuals"))
      )

      val availableSections = commaSeparate(
        request.messages(messagesApi)("site.and"),
        sections.collect {
          case (true, x) => x
        }
      )

      val continueUrl: Call = routes.MaintainThisTrustController.onSubmit(needsIv)

      Ok(view(identifier, identifierType, availableSections, continueUrl))
  }

  def onSubmit(needsIv: Boolean): Action[AnyContent] = actions.authWithData {
    implicit request =>
      if (needsIv) {
        val identifier = request.userAnswers.identifier
        Redirect(config.verifyIdentityForATrustUrl(identifier))
      } else {
        Redirect(routes.InformationMaintainingThisTrustController.onPageLoad())
      }
  }
}
