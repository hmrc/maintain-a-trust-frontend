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

package controllers.makechanges

import connectors.{TrustConnector, TrustsStoreConnector}
import models.UserAnswers
import models.requests.DataRequest
import pages.makechanges.AddOrUpdateNonEeaCompanyYesNoPage
import pages.trustdetails.ExpressTrustYesNoPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import sections.assets.NonEeaBusinessAsset
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

abstract class MakeChangesQuestionRouterController(trustConnector: TrustConnector,
                                                   trustStoreConnector: TrustsStoreConnector)
                                                  (implicit ec : ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  private def redirectAndResetTaskList(updatedAnswers: UserAnswers)
                                      (implicit request: DataRequest[AnyContent], hc: HeaderCarrier) =
      for {
        _ <- trustStoreConnector.set(request.userAnswers.identifier, updatedAnswers)
      } yield {
        Redirect(controllers.task_list.routes.TaskListController.onPageLoad())
      }

  protected def redirectToDeclaration()(implicit request: DataRequest[AnyContent]): Result = {
    request.user.affinityGroup match {
      case Agent =>
        Redirect(controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad())
      case _ =>
        Redirect(controllers.declaration.routes.IndividualDeclarationController.onPageLoad())
    }
  }

  protected def routeToAddOrUpdateProtectors()(implicit request: DataRequest[AnyContent]) = {
      for {
        existsF <- trustConnector.getDoProtectorsAlreadyExist(request.userAnswers.identifier)
        exist = existsF.value
      } yield if (exist) {
        Redirect(controllers.makechanges.routes.UpdateProtectorYesNoController.onPageLoad())
      } else {
        Redirect(controllers.makechanges.routes.AddProtectorYesNoController.onPageLoad())
      }
  }

  protected def routeToAddOrUpdateOtherIndividuals()(implicit request: DataRequest[AnyContent]) = {
      for {
        existsF <- trustConnector.getDoOtherIndividualsAlreadyExist(request.userAnswers.identifier)
        exist = existsF.value
      } yield if (exist) {
        Redirect(controllers.makechanges.routes.UpdateOtherIndividualsYesNoController.onPageLoad())
      } else {
        Redirect(controllers.makechanges.routes.AddOtherIndividualsYesNoController.onPageLoad())
      }
  }

  protected def routeToAddOrUpdateNonEeaCompany(answers: UserAnswers, isClosingTrust: Boolean)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val exist = answers.get(NonEeaBusinessAsset).isDefined

    if (answers.is5mldEnabled && isTrust5mldTaxable) {
      if (exist) {
        Future.successful(Redirect(controllers.makechanges.routes.UpdateNonEeaCompanyYesNoController.onPageLoad()))
      } else {
        Future.successful(Redirect(controllers.makechanges.routes.AddNonEeaCompanyYesNoController.onPageLoad()))
      }
    } else {
      Future.fromTry(answers.set(AddOrUpdateNonEeaCompanyYesNoPage, false)).flatMap { updatedAnswers =>
        routeToDeclareOrTaskList(updatedAnswers, isClosingTrust)
      }
    }
  }

  protected def routeToDeclareOrTaskList(updatedAnswers: UserAnswers, isClosingTrust: Boolean)
                                        (implicit request: DataRequest[AnyContent]) : Future[Result] = {
    MakeChangesRouter.decide(updatedAnswers) match {
      case MakeChangesRouter.Declaration if !isClosingTrust =>
        Future.successful(redirectToDeclaration())
      case MakeChangesRouter.Declaration if isClosingTrust =>
        redirectAndResetTaskList(updatedAnswers)
      case MakeChangesRouter.TaskList =>
        redirectAndResetTaskList(updatedAnswers)
      case MakeChangesRouter.UnableToDecide =>
        Future.successful(Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()))
    }
  }

  private def isTrust5mldTaxable(implicit request: DataRequest[_]) = {
    request.userAnswers.get(ExpressTrustYesNoPage).isDefined && request.userAnswers.isTrustTaxable
  }
}
