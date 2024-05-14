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

package controllers.transition

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.TrustConnector
import controllers.actions._
import handlers.ErrorHandler
import models.pages.WhatIsNext.MakeChanges
import navigation.Navigator.declarationUrl
import pages.WhatIsNextPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import views.html.transition.ConfirmTrustTaxableView

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmTrustTaxableController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               playbackRepository: PlaybackRepository,
                                               actions: Actions,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: ConfirmTrustTaxableView,
                                               trustsConnector: TrustConnector,
                                               config: FrontendAppConfig,
                                               errorHandler: ErrorHandler
                                             )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val className = getClass.getSimpleName

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      Ok(view())
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>
      val result = for {
        updatedAnswers <- TrustEnvelope(request.userAnswers.set(WhatIsNextPage, MakeChanges))
        _ <- playbackRepository.set(updatedAnswers)
        _ <- trustsConnector.setTaxableTrust(request.userAnswers.identifier, value = true)
      } yield
        request.userAnswers.get(ExpressTrustYesNoPage) match {
          case Some(true) if config.schedule3aExemptEnabled => Redirect(routes.Schedule3aExemptYesNoController.onPageLoad())
          case _ => Redirect(declarationUrl(
            request.user.affinityGroup,
            isTrustMigratingFromNonTaxableToTaxable = request.userAnswers.isTrustMigratingFromNonTaxableToTaxable
          ))
        }

      result.value.map {
        case Right(call) => call
        case Left(_) =>
          logger.warn(s"[$className][onSubmit][Session ID: ${utils.Session.id(hc)}] Error while storing user answers")
          InternalServerError(errorHandler.internalServerErrorTemplate)
      }
  }

}
