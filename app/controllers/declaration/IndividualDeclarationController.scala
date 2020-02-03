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

package controllers.declaration

import java.time.LocalDateTime

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.declaration.IndividualDeclarationFormProvider
import models.UserAnswers
import models.http.TVNResponse
import pages.declaration.IndividualDeclarationPage
import pages.trustees.TrusteeAddressPage
import pages.{SubmissionDatePage, TVNPage, UTRPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import sections.Trustees
import services.DeclarationService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.declaration.IndividualDeclarationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualDeclarationController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 playbackRepository: PlaybackRepository,
                                                 actions: AuthenticateForPlayback,
                                                 formProvider: IndividualDeclarationFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: IndividualDeclarationView,
                                                 service: DeclarationService
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(IndividualDeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, controllers.declaration.routes.IndividualDeclarationController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, controllers.declaration.routes.IndividualDeclarationController.onSubmit()))),

        declaration => {
          request.userAnswers.get(UTRPage) match {
            case None =>
              Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
            case Some(utr) =>
              (getLeadTrusteeAddress(request.userAnswers) map { address =>
                service.individualDeclareNoChange(utr, declaration, address) flatMap {
                  case TVNResponse(tvn) =>
                    for {
                      updatedAnswers <- Future.fromTry(
                        request.userAnswers
                          .set(IndividualDeclarationPage, declaration)
                          .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
                          .flatMap(_.set(TVNPage, tvn))
                      )
                      _ <- playbackRepository.set(updatedAnswers)
                    } yield Redirect(controllers.declaration.routes.ConfirmationController.onPageLoad())
                  case _ =>
                    Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
                }
              }).getOrElse(Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad())))
          }
        }
      )
  }

  private def getLeadTrusteeAddress(userAnswers: UserAnswers) = {
    for {
      index <- getIndexOfLeadTrustee(userAnswers)
      address <- userAnswers.get(TrusteeAddressPage(index))
    } yield {
      address
    }
  }

  private def getIndexOfLeadTrustee(userAnswers: UserAnswers): Option[Int] = {
    for {
      trusteesAsJson <- userAnswers.get(Trustees)
      zipped = trusteesAsJson.value.zipWithIndex
      lead <- zipped.find(x => (x._1 \ "isThisLeadTrustee").as[Boolean])
    } yield lead._2
  }

}
