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
import connectors.{TrustConnector, TrustsStoreConnector}
import controllers.actions.Actions
import controllers.makechanges.MakeChangesQuestionRouterController
import forms.WhatIsNextFormProvider
import models.pages.WhatIsNext
import models.pages.WhatIsNext._
import models.requests.DataRequest
import pages.WhatIsNextPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import views.html.WhatIsNextView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class WhatIsNextController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      playbackRepository: PlaybackRepository,
                                      actions: Actions,
                                      formProvider: WhatIsNextFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: WhatIsNextView,
                                      trustConnector: TrustConnector,
                                      trustsStoreConnector: TrustsStoreConnector
                                    )(implicit ec: ExecutionContext)
  extends MakeChangesQuestionRouterController(trustConnector, trustsStoreConnector) {

  val form: Form[WhatIsNext] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhatIsNextPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.userAnswers.trustMldStatus))
  }

  def onSubmit(): Action[AnyContent] = actions.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.userAnswers.trustMldStatus))),

        value => {
          for {
            hasAnswerChanged <- Future.fromTry(Success(!request.userAnswers.get(WhatIsNextPage).contains(value)))
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsNextPage, value))
            _ <- playbackRepository.set(updatedAnswers)
            result <- updateMigrationStatusAndRedirect(value, hasAnswerChanged)
          } yield {
            result
          }
        }
      )
  }

  private def updateMigrationStatusAndRedirect(newAnswer: WhatIsNext, hasAnswerChanged: Boolean)
                                              (implicit request: DataRequest[AnyContent]): Future[Result] = {

    def redirect(implicit request: DataRequest[AnyContent]): Result = Redirect {
      newAnswer match {
        case DeclareTheTrustIsUpToDate =>
          redirectToDeclaration
        case MakeChanges =>
          redirectToFirstUpdateQuestion
        case CloseTrust if request.userAnswers.isTrustTaxable =>
          controllers.close.taxable.routes.DateLastAssetSharedOutYesNoController.onPageLoad()
        case CloseTrust =>
          controllers.close.nontaxable.routes.DateClosedController.onPageLoad()
        case NoLongerTaxable =>
          controllers.routes.NoTaxLiabilityInfoController.onPageLoad()
        case NeedsToPayTax =>
          controllers.transition.routes.TaxLiabilityYesNoController.onPageLoad()
        case GeneratePdf =>
          controllers.routes.ObligedEntityPdfController.getPdf(request.userAnswers.identifier)
      }
    }

    if (newAnswer != GeneratePdf) {
      for {
        _ <- {
          if (hasAnswerChanged) {
            trustConnector.removeTransforms(request.userAnswers.identifier).map(_ => ())
          } else {
            Future.successful(())
          }
        }
        _ <- if (newAnswer != NeedsToPayTax) {
          trustConnector.setTaxableMigrationFlag(request.userAnswers.identifier, false)
        } else {
          Future.successful(())
        }
      } yield {
        redirect
      }
    } else {
      Future.successful(redirect)
    }
  }
}
