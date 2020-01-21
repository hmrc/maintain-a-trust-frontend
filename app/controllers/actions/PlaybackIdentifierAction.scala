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

import com.google.inject.{ImplementedBy, Inject}
import connectors.EnrolmentStoreConnector
import models.requests.DataRequest
import pages.UTRPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, BodyParsers, Result}
import services.PlaybackAuthenticationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class PlaybackIdentifierActionImpl @Inject()(val parser: BodyParsers.Default,
                                             enrolmentStoreConnector: EnrolmentStoreConnector,
                                             playbackAuthenticationService: PlaybackAuthenticationService
                                  )(override implicit val executionContext: ExecutionContext) extends PlaybackIdentifierAction {

  override def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    request.userAnswers.get(UTRPage) map { utr =>
      playbackAuthenticationService.authenticate(utr)(request, hc)
    } getOrElse Future.successful(Left(Redirect(controllers.routes.IndexController.onPageLoad())))

  }

}

@ImplementedBy(classOf[PlaybackIdentifierActionImpl])
trait PlaybackIdentifierAction extends ActionRefiner[DataRequest, DataRequest] {

  def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]]

}