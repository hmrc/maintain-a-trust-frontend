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
import forms.YesNoFormProvider
import models.requests.DataRequest
import navigation.DeclareNoChange
import pages.UTRPage
import pages.makechanges._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import views.html.makechanges.AddOtherIndividualsYesNoView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddOtherIndividualsYesNoController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        playbackRepository: PlaybackRepository,
                                        actions: AuthenticateForPlayback,
                                        yesNoFormProvider: YesNoFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: AddOtherIndividualsYesNoView,
                                        config: FrontendAppConfig
                                     )(implicit ec: ExecutionContext) extends DeclareNoChange with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("addOtherIndividuals")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddOtherIndividualsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(AddOtherIndividualsYesNoPage, value)
            )
            _ <- playbackRepository.set(updatedAnswers)
          } yield {
            redirect()
          }
        }
      )
  }

  private def redirect()(implicit request: DataRequest[AnyContent]): Result = {

    case class UpdateFilterQuestions(trustees: Boolean, beneficiaries: Boolean, settlors: Boolean, protectors: Boolean, natural: Boolean)

    (for {
      t <- request.userAnswers.get(UpdateTrusteesYesNoPage)
      b <- request.userAnswers.get(UpdateBeneficiariesYesNoPage)
      s <- request.userAnswers.get(UpdateSettlorsYesNoPage)
      p <- request.userAnswers.get(AddProtectorYesNoPage)
      n <- request.userAnswers.get(AddOtherIndividualsYesNoPage)
    } yield {
      UpdateFilterQuestions(t, b, s, p, n) match {
        case UpdateFilterQuestions(true, false, false, false, false) =>
          redirectToMaintainTrustees()
        case UpdateFilterQuestions(false, false, false, false, false) =>
          redirectToDeclaration()
        case _ =>
          // TODO: Redirect to an unavailable sections page. Waiting on design.
          Redirect(controllers.routes.FeatureNotAvailableController.onPageLoad())
      }
    }).getOrElse(Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()))
  }

  private def redirectToMaintainTrustees()(implicit request: DataRequest[AnyContent]): Result = {
    request.userAnswers.get(UTRPage) map {
      utr =>
        val url = s"${config.maintainATrusteeFrontendUrl}/$utr"
        Redirect(Call("GET", url))
    } getOrElse {
      Redirect(controllers.routes.UTRController.onPageLoad())
    }
  }

}
