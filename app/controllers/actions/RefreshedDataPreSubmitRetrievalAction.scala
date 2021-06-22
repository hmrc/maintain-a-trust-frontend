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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.AgentDeclaration
import models.http.{GetTrust, Processed}
import models.pages.WhatIsNext
import models.requests.DataRequest
import pages.WhatIsNextPage
import pages.declaration.AgentDeclarationPage
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import repositories.PlaybackRepository
import services.FeatureFlagService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.TrustClosureDate.{getClosureDate, setClosureDate}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class RefreshedDataPreSubmitRetrievalActionImpl @Inject()(
                                                           val parser: BodyParsers.Default,
                                                           playbackRepository: PlaybackRepository,
                                                           trustConnector: TrustConnector,
                                                           playbackExtractor: UserAnswersExtractor,
                                                           featureFlagService: FeatureFlagService
                                                         )(override implicit val executionContext: ExecutionContext)
  extends RefreshedDataPreSubmitRetrievalAction with Logging {

  case class SubmissionData(utr: String, whatIsNext: WhatIsNext, agent: Option[AgentDeclaration], endDate: Option[LocalDate])

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for {
      whatIsNext <- request.userAnswers.get(WhatIsNextPage)
      optionalAgentInformation = request.userAnswers.get(AgentDeclarationPage)
    } yield {

      val identifier = request.userAnswers.identifier

      val submissionData = SubmissionData(identifier, whatIsNext, optionalAgentInformation, getClosureDate(request.userAnswers))

      trustConnector.playback(identifier).flatMap {
        case Processed(playback, _) => extractAndRefreshUserAnswers(submissionData, playback)(request, hc)
        case _ => Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      }
    }).getOrElse {
      logger.error(s"[RefreshedDraftDataRetrievalAction] unable to get data from user answers")
      Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

  private def extractAndRefreshUserAnswers[A](data: SubmissionData, playback: GetTrust)
                                             (implicit request: DataRequest[A], hc: HeaderCarrier): Future[Either[Result, DataRequest[A]]] = {
    featureFlagService.is5mldEnabled() flatMap { is5mldEnabled =>
      if (!request.userAnswers.is5mldEnabled && is5mldEnabled) {
        logger.info(s"[RefreshedDraftDataRetrievalAction] 5MLD has been turned on mid-session." +
          s" Redirecting to express page to start 4MLD -> 5MLD transition journey.")
        Future.successful(Left(Redirect(controllers.transition.routes.ExpressTrustYesNoController.onPageLoad())))
      } else {
        Future.fromTry {
          request.userAnswers.clearData
            .set(WhatIsNextPage, data.whatIsNext)
            .flatMap(_.set(AgentDeclarationPage, data.agent))
            .flatMap(answers => setClosureDate(answers, data.endDate))
        } flatMap { updatedAnswers =>
          playbackExtractor.extract(updatedAnswers, playback) flatMap {
            case Right(extractedAnswers) =>
              playbackRepository.set(extractedAnswers) map { _ =>
                logger.debug(s"[RefreshedDraftDataRetrievalAction] Set updated user answers in db")
                Right(DataRequest(request.request, extractedAnswers, request.user))
              }
            case Left(reason) =>
              logger.warn(s"[RefreshedDraftDataRetrievalAction] unable to extract user answers due to $reason")
              Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
          }
        }
      }
    } recoverWith {
      case e =>
        logger.error(s"[RefreshedDraftDataRetrievalAction] Failed to retrieve is5mldEnabled feature flag: ${e.getMessage}")
        Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

}

@ImplementedBy(classOf[RefreshedDataPreSubmitRetrievalActionImpl])
trait RefreshedDataPreSubmitRetrievalAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]

}
