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
import connectors.TrustsStoreConnector
import controllers.actions._
import forms.YesNoFormProvider
import models.UserAnswers
import models.requests.WhatNextRequest
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
                                                    trustStoreConnector: TrustsStoreConnector
                                                  )(implicit ec: ExecutionContext) extends DeclareNoChange with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.requireAnswer {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      val preparedForm = request.userAnswers.get(AddOrUpdateOtherIndividualsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, prefix(request.closingTrust)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireAnswer.async {
    implicit request =>

      val form: Form[Boolean] = yesNoFormProvider.withPrefix(prefix(request.closingTrust))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, prefix(request.closingTrust)))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddOrUpdateOtherIndividualsYesNoPage, value))
            _ <- playbackRepository.set(updatedAnswers)
            route <- determineRoute(updatedAnswers, request.closingTrust)
          } yield {
            route
          }
        }
    )
  }

  private def determineRoute(updatedAnswers: UserAnswers, closingTrust: Boolean)
                            (implicit request: WhatNextRequest[AnyContent]) : Future[Result] = {

    MakeChangesRouter.decide(updatedAnswers) match {
      case MakeChangesRouter.Declaration if !closingTrust =>
        Future.successful(redirectToDeclaration()(request.request))
      case MakeChangesRouter.TaskList | MakeChangesRouter.Declaration =>
        request.userAnswers.get(UTRPage).map {
          utr =>
            for {
              _ <- trustStoreConnector.set(utr, updatedAnswers)
            } yield {
              Redirect(controllers.routes.VariationProgressController.onPageLoad())
            }
        }.getOrElse {
          Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
        }
      case MakeChangesRouter.UnableToDecide =>
        Future.successful(Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()))
    }
  }

  private def prefix(closingTrust: Boolean): String = {
    if (closingTrust) "addOtherIndividualsClosing" else "addOtherIndividuals"
  }
}
