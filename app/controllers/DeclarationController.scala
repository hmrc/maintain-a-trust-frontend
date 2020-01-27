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

import java.time.temporal.ChronoUnit.DAYS
import java.time.{LocalDateTime, ZoneOffset}

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.DeclarationFormProvider
import models.UserAnswers
import models.http.{TVNResponse, TrustResponse}
import pages.{DeclarationPage, SubmissionDatePage, TVNPage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.DeclarationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       playbackRepository: PlaybackRepository,
                                       actions: AuthenticateForPlayback,
                                       formProvider: DeclarationFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DeclarationView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForUtr {
    implicit request =>

      val preparedForm = request.userAnswers.get(DeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.user.affinityGroup, controllers.routes.DeclarationController.onSubmit()))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForUtr.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.user.affinityGroup, controllers.routes.DeclarationController.onSubmit()))),

        // TODO: Check response for submission of no change data and redirect accordingly

        value => {
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers
                .set(DeclarationPage, value)
                .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
            )
            _ <- playbackRepository.set(updatedAnswers)
//            response <- submissionService.submit(updatedAnswers)
//            result <- handleResponse(updatedAnswers, response)
          } yield Redirect(controllers.routes.ConfirmationController.onPageLoad())
//          yield result
//
//                    r.recover {
//                      case _ : UnableToMaintain =>
//                        Logger.error(s"[onSubmit] Not able to maintain, redirecting to ???.")
//                        Redirect(???)
//                      case NonFatal(e) =>
//                        Logger.error(s"[onSubmit] Non fatal exception, throwing again. ${e.getMessage}")
//                        throw e
//                    }
        }
      )

  }

  private def handleResponse(updatedAnswers: UserAnswers, response: TrustResponse) : Future[Result] = {
    response match {
      case tvn: TVNResponse =>
        Logger.info("[DeclarationController][handleResponse] Saving trust playback tvn.")
        saveTVNAndComplete(updatedAnswers, tvn)
      case e =>
        Logger.warn(s"[DeclarationController][handleResponse] unable to submit due to error $e")
        Future.successful(Redirect(routes.WhatIsNextController.onPageLoad()))
    }
  }

  private def saveTVNAndComplete(updatedAnswers: UserAnswers, tvn: TVNResponse): Future[Result] = {
    Future.fromTry(updatedAnswers.set(TVNPage, tvn.tvn)).flatMap {
      trnSaved =>
        val submissionDate = LocalDateTime.now(ZoneOffset.UTC)
        Future.fromTry(trnSaved.set(SubmissionDatePage,  submissionDate)).map {
          _ =>
            val days = DAYS.between(updatedAnswers.updatedAt, submissionDate)
            Logger.info(s"[saveTVNAndComplete] Days between last declaration and now : $days")
            Redirect(routes.ConfirmationController.onPageLoad())
        }
    }
  }

}
