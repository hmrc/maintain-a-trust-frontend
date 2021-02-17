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

package controllers.declaration

import java.time.{LocalDate, LocalDateTime}

import com.google.inject.{Inject, Singleton}
import controllers.actions._
import forms.declaration.AgentDeclarationFormProvider
import models.http.TVNResponse
import models.requests.{AgentUser, DataRequest}
import models.{Address, AgentDeclaration, UserAnswers}
import pages._
import pages.close.DateLastAssetSharedOutPage
import pages.declaration.{AgencyRegisteredAddressInternationalPage, AgencyRegisteredAddressUkPage, AgencyRegisteredAddressUkYesNoPage, AgentDeclarationPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.DeclarationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session
import views.html.declaration.AgentDeclarationView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgentDeclarationController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            playbackRepository: PlaybackRepository,
                                            actions: Actions,
                                            formProvider: AgentDeclarationFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: AgentDeclarationView,
                                            service: DeclarationService
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form: Form[AgentDeclaration] = formProvider()

  def onPageLoad(): Action[AnyContent] = actions.requireIsClosingAnswer {
    implicit request =>

      val preparedForm = request.userAnswers.get(AgentDeclarationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.closingTrust))
  }

  def onSubmit(): Action[AnyContent] = actions.requireIsClosingAnswer.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.closingTrust))),

        declaration => {
          request.user match {
            case agentUser: AgentUser =>
              (for {
                agencyAddress <- getAgencyRegisteredAddress(request.userAnswers)
              } yield {
                submitDeclaration(
                  declaration,
                  agentUser,
                  request.userAnswers.identifier,
                  agencyAddress,
                  request.userAnswers.get(DateLastAssetSharedOutPage)
                )(request.request)
              }).getOrElse(handleError(s"[Session ID: ${Session.id(hc)}] Failed to get agency address"))

            case _ =>
              handleError("User was not an agent")
          }
        }
      )
  }

  private def submitDeclaration(declaration: AgentDeclaration,
                                agentUser: AgentUser,
                                utr: String,
                                agencyAddress: Address,
                                endDate: Option[LocalDate]
                               )(implicit request: DataRequest[AnyContent]) = {

    service.agentDeclaration(utr,
      declaration,
      agentUser.agentReferenceNumber,
      agencyAddress,
      declaration.agencyName,
      endDate
    ) flatMap {
      case TVNResponse(tvn) =>
        for {
          updatedAnswers <- Future.fromTry(
            request.userAnswers
              .set(AgentDeclarationPage, declaration)
              .flatMap(_.set(SubmissionDatePage, LocalDateTime.now))
              .flatMap(_.set(TVNPage, tvn))
          )
          _ <- playbackRepository.set(updatedAnswers)
        } yield Redirect(controllers.declaration.routes.ConfirmationController.onPageLoad())
      case _ =>
        handleError(s"[Session ID: ${Session.id(hc)}][UTR/URN: ${utr}] Failed to declare")
    }
  }

  private def handleError(message: String) = {
    logger.error(message)
    Future.successful(Redirect(controllers.declaration.routes.ProblemDeclaringController.onPageLoad()))
  }

  private def getAgencyRegisteredAddress(userAnswers: UserAnswers): Option[Address] = {
    userAnswers.get(AgencyRegisteredAddressUkYesNoPage) flatMap {
      case true => userAnswers.get(AgencyRegisteredAddressUkPage)
      case false => userAnswers.get(AgencyRegisteredAddressInternationalPage)
    }
  }

}
