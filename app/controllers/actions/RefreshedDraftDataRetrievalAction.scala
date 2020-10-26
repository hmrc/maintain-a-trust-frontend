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

package controllers.actions

import java.time.LocalDate

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import controllers.routes
import mapping.UserAnswersExtractor
import models.http.{GetTrust, Processed}
import models.pages.WhatIsNext
import models.requests.DataRequest
import models.{AgentDeclaration, UserAnswers}
import pages.close.DateLastAssetSharedOutPage
import pages.declaration.AgentDeclarationPage
import pages.WhatIsNextPage
import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import repositories.PlaybackRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class RefreshedDraftDataRetrievalActionImpl @Inject()(val parser: BodyParsers.Default,
                                             playbackRepository: PlaybackRepository,
                                             trustConnector: TrustConnector,
                                             playbackExtractor: UserAnswersExtractor
                                            )(override implicit val executionContext: ExecutionContext) extends RefreshedDraftDataRetrievalAction {

  case class SubmissionData(utr: String, whatIsNext: WhatIsNext, agent: Option[AgentDeclaration], endDate: Option[LocalDate])

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    (for {
      whatIsNext <- request.userAnswers.get(WhatIsNextPage)
      optionalAgentInformation = request.userAnswers.get(AgentDeclarationPage)
      optionalEndDate = request.userAnswers.get(DateLastAssetSharedOutPage)
    } yield {

      val utr = request.userAnswers.utr

      val submissionData = SubmissionData(utr, whatIsNext, optionalAgentInformation, optionalEndDate)

      trustConnector.playback(utr).flatMap {
        case Processed(playback, _) => extractAndRefreshUserAnswers(submissionData, utr, playback)(request)
        case _ => Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
      }
    }).getOrElse {
            Logger.error(s"[RefreshedDraftDataRetrievalAction] unable to get data from user answers")
            Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

  private def extractAndRefreshUserAnswers[A](data: SubmissionData, utr: String, playback: GetTrust)
                        (implicit request: DataRequest[A]) : Future[Either[Result, DataRequest[A]]] = {

    val newSession = UserAnswers.startNewSession(request.user.internalId, utr)

    playbackExtractor.extract(newSession, playback) match {
      case Right(answers) =>
        for {
          updatedAnswers <- Future.fromTry {
            answers
              .set(WhatIsNextPage, data.whatIsNext)
              .flatMap(_.set(AgentDeclarationPage, data.agent))
              .flatMap(_.set(DateLastAssetSharedOutPage, data.endDate))
          }
          _ <- playbackRepository.set(updatedAnswers)
        } yield {
          Logger.debug(s"[RefreshedDraftDataRetrievalAction] Set updated user answers in db")
          Right(DataRequest(request.request, updatedAnswers, request.user))
        }
      case Left(reason) =>
        Logger.warn(s"[RefreshedDraftDataRetrievalAction] unable to extract user answers due to $reason")
        Future.successful(Left(Redirect(routes.TrustStatusController.sorryThereHasBeenAProblem())))
    }
  }

}

@ImplementedBy(classOf[RefreshedDraftDataRetrievalActionImpl])
trait RefreshedDraftDataRetrievalAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]

}