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

package controllers.transition

import connectors.TrustConnector
import controllers.actions.Actions
import forms.YesNoFormProvider
import navigation.Navigator.declarationUrl
import pages.trustdetails.Schedule3aExemptYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transition.Schedule3aExemptYesNoView
import javax.inject.Inject
import models.pages.WhatIsNext.MakeChanges
import pages.WhatIsNextPage

import scala.concurrent.{ExecutionContext, Future}

class Schedule3aExemptYesNoController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                playbackRepository: PlaybackRepository,
                                                actions: Actions,
                                                formProvider: YesNoFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: Schedule3aExemptYesNoView,
                                                trustsConnector: TrustConnector
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[Boolean] = formProvider.withPrefix("schedule3aExemptYesNo")

  def onPageLoad(): Action[AnyContent] = actions.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(Schedule3aExemptYesNoPage) match {
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
          val setAnswers = request.userAnswers.set(Schedule3aExemptYesNoPage, value)
          for {
            updatedAnswers <- Future.fromTry(setAnswers)
            _ <- playbackRepository.set(updatedAnswers)
            _ <- trustsConnector.setSchedule3aExempt(request.userAnswers.identifier, value)
          } yield Redirect(declarationUrl(
            request.user.affinityGroup,
            isTrustMigratingFromNonTaxableToTaxable = request.userAnswers.isTrustMigratingFromNonTaxableToTaxable
          ))
        }
      )
  }
}
