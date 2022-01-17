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

package controllers.close.taxable

import connectors.TrustConnector
import controllers.actions.Actions
import forms.DateFormProvider
import pages.close.taxable.DateLastAssetSharedOutPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.close.taxable.DateLastAssetSharedOutView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateLastAssetSharedOutController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  playbackRepository: PlaybackRepository,
                                                  actions: Actions,
                                                  formProvider: DateFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: DateLastAssetSharedOutView,
                                                  trustConnector: TrustConnector
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val prefix: String = "dateLastAssetSharedOut"

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
      } yield {
        val form = formProvider.withPrefixAndTrustStartDate(prefix, startDate)

        val preparedForm = request.userAnswers.get(DateLastAssetSharedOutPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm))
      }
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      def render(startDate: LocalDate): Future[Result] = {
        val form = formProvider.withPrefixAndTrustStartDate(prefix, startDate)

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DateLastAssetSharedOutPage, value))
              _ <- playbackRepository.set(updatedAnswers)
            } yield Redirect(controllers.close.routes.BeforeClosingController.onPageLoad())
        )
      }

      for {
        startDate <- trustConnector.getStartDate(request.userAnswers.identifier)
        result <- render(startDate)
      } yield result
  }
}
