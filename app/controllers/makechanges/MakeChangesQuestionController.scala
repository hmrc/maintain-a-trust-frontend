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

import connectors.TrustsStoreConnector
import models.UserAnswers
import models.requests.DataRequest
import pages.UTRPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

abstract class MakeChangesQuestionController(trustStoreConnector: TrustsStoreConnector)
                                            (implicit ec : ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  private def redirectAndResetTaskList(updatedAnswers: UserAnswers)
                                      (implicit request: DataRequest[AnyContent], hc: HeaderCarrier) = request.userAnswers.get(UTRPage).map {
      utr =>
        for {
          _ <- trustStoreConnector.set(utr, updatedAnswers)
        } yield {
          Redirect(controllers.routes.VariationProgressController.onPageLoad())
        }
    }.getOrElse {
      Future.successful(Redirect(controllers.routes.UTRController.onPageLoad()))
    }

  protected def redirectToDeclaration()(implicit request: DataRequest[AnyContent]): Result = {
    request.user.affinityGroup match {
      case Agent =>
        Redirect(controllers.declaration.routes.AgencyRegisteredAddressUkYesNoController.onPageLoad())
      case _ =>
        Redirect(controllers.declaration.routes.IndividualDeclarationController.onPageLoad())
    }
  }

  protected def decideNexRouteFromAnswers(updatedAnswers: UserAnswers)(implicit request: DataRequest[AnyContent]) : Future[Result] = {
    MakeChangesRouter.decide(updatedAnswers) match {
      case MakeChangesRouter.Declaration =>
        Future.successful(redirectToDeclaration())
      case MakeChangesRouter.TaskList =>
        redirectAndResetTaskList(updatedAnswers)
      case MakeChangesRouter.UnableToDecide =>
        Future.successful(Redirect(controllers.makechanges.routes.UpdateTrusteesYesNoController.onPageLoad()))
    }
  }
}
