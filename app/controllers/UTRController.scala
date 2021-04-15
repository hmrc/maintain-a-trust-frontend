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
import connectors.TrustConnector
import controllers.actions.Actions
import forms.UTRFormProvider
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{FeatureFlagService, UserAnswersSetupService}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.UTRView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UTRController @Inject()(
                               override val messagesApi: MessagesApi,
                               actions: Actions,
                               uaSetupService: UserAnswersSetupService,
                               formProvider: UTRFormProvider,
                               featureFlagService: FeatureFlagService,
                               trustsConnector: TrustConnector,
                               val controllerComponents: MessagesControllerComponents,
                               view: UTRView
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[String] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.auth {
    implicit request =>
      Ok(view(form, routes.UTRController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = actions.auth.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, routes.UTRController.onSubmit()))),
        utr => {
          featureFlagService.is5mldEnabled() flatMap { is5mldEnabled =>
            trustsConnector.getUntransformedTrustDetails(utr) flatMap { trustDetails =>
              uaSetupService.setupAndRedirectToStatus(
                identifier = utr,
                internalId = request.user.internalId,
                is5mldEnabled = is5mldEnabled,
                isUnderlyingData5mld = trustDetails.is5mld,
                isUnderlyingDataTaxable = trustDetails.isTaxable
              )
            } recover {
              case x: UpstreamErrorResponse if x.statusCode == NOT_FOUND =>
                logger.error(s"[Session ID: ${Session.id(hc)}] Trust not found for UTR $utr.")
                Redirect(routes.TrustStatusController.notFoundWithIdentifier(utr))
              case e =>
                logger.error(s"[Session ID: ${Session.id(hc)}] Error retrieving trust details for UTR $utr: ${e.getMessage}")
                Redirect(routes.TrustStatusController.downWithIdentifier(utr))
            }
          }
        }
      )
  }

}
