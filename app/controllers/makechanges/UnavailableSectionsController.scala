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

package controllers.makechanges

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.actions._
import models.requests.DataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.makechanges.UnavailableSectionsView

import scala.concurrent.ExecutionContext

@Singleton
class UnavailableSectionsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        actions: AuthenticateForPlayback,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: UnavailableSectionsView,
                                        config: FrontendAppConfig
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      case class AvailableSections(
                                  trustees: (Boolean, String),
                                  beneficiaries: (Boolean, String),
                                  settlors: (Boolean, String),
                                  protectors: (Boolean, String),
                                  natural: (Boolean, String)
                                  )

      val sections = List(
        (config.maintainSettlorsEnabled, "settlors"),
        (config.maintainTrusteesEnabled, "trustees"),
        (config.maintainBeneficiariesEnabled, "beneficiaries"),
        (config.maintainProtectorsEnabled, "protectors"),
        (config.maintainOtherIndividualsEnabled, "any other individuals")
      )

      val availableSections = commaSeparate(
        request.messages(messagesApi)("site.and"),
        sections.collect {
          case (true, x) => x
        }
      )

      val unavailableSections = commaSeparate(
        request.messages(messagesApi)("site.or"),
        sections.collect {
          case (false, x) => x
        }
      )

      Ok(view(availableSections, unavailableSections))
  }

  @scala.annotation.tailrec
  private def commaSeparate(connective: String, list: List[String], acc: String = "")(implicit request: DataRequest[AnyContent]): String = {
    list.size match {
      case 0 => acc
      case 1 if !acc.isEmpty => commaSeparate(connective, list.tail, acc + " " + connective + " " + list.head)
      case 1 | 2 => commaSeparate(connective, list.tail, acc + list.head)
      case _ => commaSeparate(connective, list.tail, acc + list.head + ", ")
    }
  }

}
