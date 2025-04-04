/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.data.EitherT
import connectors.{TrustConnector, TrustsStoreConnector}
import models.UserAnswers
import models.requests.DataRequest
import pages.makechanges.AddOrUpdateNonEeaCompanyYesNoPage
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsFalse, JsTrue}
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.TrustEnvelope
import utils.TrustEnvelope.TrustEnvelope
import scala.concurrent.ExecutionContext

abstract class MakeChangesQuestionRouterController(
                                                    trustConnector: TrustConnector,
                                                    trustStoreConnector: TrustsStoreConnector
                                                  )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  private def redirectAndResetTaskList(updatedAnswers: UserAnswers)
                                      (implicit request: DataRequest[AnyContent], hc: HeaderCarrier): TrustEnvelope[Result] = EitherT {
    trustStoreConnector.set(request.userAnswers.identifier, updatedAnswers).value.map(_.map(
      _ => Redirect(controllers.tasklist.routes.TaskListController.onPageLoad())
    ))
  }

  protected def redirectToDeclaration(implicit request: DataRequest[AnyContent]): Call = {
    request.user.affinityGroup match {
      case Agent =>
        controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad()
      case _ =>
        controllers.declaration.routes.IndividualDeclarationController.onPageLoad()
    }
  }

  protected def redirectToFirstUpdateQuestion(implicit request: DataRequest[AnyContent]): Call = {
    if (is5mldTrustIn5mldMode) {
      controllers.makechanges.routes.UpdateTrustDetailsYesNoController.onPageLoad()
    } else {
      controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()
    }
  }

  protected def routeToAddOrUpdateProtectors(implicit request: DataRequest[AnyContent]): TrustEnvelope[Result] = {
    trustConnector.getDoProtectorsAlreadyExist(request.userAnswers.identifier) map {
      case JsTrue => Redirect(controllers.makechanges.routes.UpdateProtectorYesNoController.onPageLoad())
      case JsFalse => Redirect(controllers.makechanges.routes.AddProtectorYesNoController.onPageLoad())
    }
  }

  protected def routeToAddOrUpdateOtherIndividuals(implicit request: DataRequest[AnyContent]): TrustEnvelope[Result] = {
    trustConnector.getDoOtherIndividualsAlreadyExist(request.userAnswers.identifier) map {
      case JsTrue => Redirect(controllers.makechanges.routes.UpdateOtherIndividualsYesNoController.onPageLoad())
      case JsFalse => Redirect(controllers.makechanges.routes.AddOtherIndividualsYesNoController.onPageLoad())
    }
  }

  protected def routeToAddOrUpdateNonEeaCompany(updatedAnswers: UserAnswers, isClosingTrust: Boolean)
                                               (implicit request: DataRequest[AnyContent]): TrustEnvelope[Result] = {
    if (is5mldTrustIn5mldMode) {
      trustConnector.getDoNonEeaCompaniesAlreadyExist(request.userAnswers.identifier) map {
        case JsTrue => Redirect(controllers.makechanges.routes.UpdateNonEeaCompanyYesNoController.onPageLoad())
        case JsFalse => Redirect(controllers.makechanges.routes.AddNonEeaCompanyYesNoController.onPageLoad())
      }
    } else {
      TrustEnvelope(updatedAnswers.set(AddOrUpdateNonEeaCompanyYesNoPage, false)) flatMap { updatedAnswers =>
        routeToDeclareOrTaskList(updatedAnswers, isClosingTrust)
      }
    }
  }

  protected def routeToDeclareOrTaskList(updatedAnswers: UserAnswers, isClosingTrust: Boolean)
                                        (implicit request: DataRequest[AnyContent]): TrustEnvelope[Result] = {
    MakeChangesRouter.decide(updatedAnswers) match {
      case MakeChangesRouter.Declaration if !isClosingTrust =>
        TrustEnvelope(Redirect(redirectToDeclaration))
      case MakeChangesRouter.Declaration =>
        redirectAndResetTaskList(updatedAnswers)
      case MakeChangesRouter.TaskList =>
        redirectAndResetTaskList(updatedAnswers)
      case MakeChangesRouter.UnableToDecide =>
        TrustEnvelope(Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()))
    }
  }

  def is5mldTrustIn5mldMode(implicit request: DataRequest[_]): Boolean = {
    request.userAnswers.is5mldTrustIn5mldMode
  }

}
