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

package controllers.transition

import com.google.inject.{Inject, Singleton}
import connectors.TrustConnector
import controllers.actions._
import forms.YesNoFormProvider
import models.requests.DataRequest
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.MaintainATrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.transition.ExpressTrustYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExpressTrustYesNoController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             playbackRepository: PlaybackRepository,
                                             actions: Actions,
                                             yesNoFormProvider: YesNoFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ExpressTrustYesNoView,
                                             trustsConnector: TrustConnector,
                                             maintainATrustService: MaintainATrustService
                                           )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("expressTrustYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(ExpressTrustYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors))),

        value => {

          val isTrustMigrating = request.userAnswers.isTrustMigratingFromNonTaxableToTaxable
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ExpressTrustYesNoPage, value))
            _ <- playbackRepository.set(updatedAnswers)
            _ <- removeTransformsIfNotMigrating(isTrustMigrating)
            _ <- trustsConnector.setExpressTrust(request.userAnswers.identifier, value)
          } yield {
            if (isTrustMigrating) {
              logger.info(s"--------------------- isTrustMigrating $isTrustMigrating  TaskListController")
              Redirect(controllers.tasklist.routes.TaskListController.onPageLoad())
            } else {
              logger.info(s"--------------------- isTrustMigrating $isTrustMigrating  ConfirmTrustTaxableController")
              Redirect(routes.ConfirmTrustTaxableController.onPageLoad())
            }
          }
        }
      )
  }

  private def removeTransformsIfNotMigrating(isTrustMigrating: Boolean)
                                            (implicit request: DataRequest[AnyContent]): Future[Unit] = {
    if (isTrustMigrating) {
      logger.info(s"[Session ID: ${Session.id(hc)}] Migrating from non-taxable to taxable. Keeping transforms.")
      Future.successful(())
    } else {
      logger.info(s"[Session ID: ${Session.id(hc)}] Redirected from RefreshedDataPreSubmitRetrievalAction or " +
        s"transitioning from 4MLD to 5MLD. Removing transforms and resetting tasks.")
      maintainATrustService.removeTransformsAndResetTaskList(request.userAnswers.identifier)
    }
  }

}
