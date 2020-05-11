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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.actions.AuthenticateForPlayback
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import pages.UTRPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.MaintainThisTrustView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MaintainThisTrustController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             actions: AuthenticateForPlayback,
                                             val controllerComponents: MessagesControllerComponents,
                                             config: FrontendAppConfig,
                                             view: MaintainThisTrustView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad() = actions.authWithData {
    implicit request =>

      request.userAnswers.get(UTRPage).map { utr =>

        @scala.annotation.tailrec
        def commaSeparate(connective: String, list: List[String], acc: String = "")
                         (implicit request: DataRequest[AnyContent]): String = {
          list.size match {
            case 0 => acc
            case 1 if !acc.isEmpty => commaSeparate(connective, list.tail, acc + " " + connective + " " + list.head)
            case 1 | 2 => commaSeparate(connective, list.tail, acc + list.head)
            case _ => commaSeparate(connective, list.tail, acc + list.head + ", ")
          }
        }

        val sections: List[(Boolean, String)] = List(
          (config.maintainSettlorsEnabled, request.messages(messagesApi)("section.settlors")),
          (config.maintainTrusteesEnabled, request.messages(messagesApi)("section.trustees")),
          (config.maintainBeneficiariesEnabled, request.messages(messagesApi)("section.beneficiaries")),
          (config.maintainProtectorsEnabled, request.messages(messagesApi)("section.protectors")),
          (config.maintainOtherIndividualsEnabled, request.messages(messagesApi)("section.natural"))
        )

        val availableSections = commaSeparate(
          request.messages(messagesApi)("site.and"),
          sections.collect {
            case (true, x) => x
          }
        )

        Ok(view(utr, availableSections))
      } getOrElse Redirect(routes.SessionExpiredController.onPageLoad())

  }

}
