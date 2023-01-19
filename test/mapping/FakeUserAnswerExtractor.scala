/*
 * Copyright 2023 HM Revenue & Customs
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

package mapping

import mapping.PlaybackExtractionErrors.FailedToExtractData
import models.UserAnswers
import models.http.GetTrust
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class FakeUserAnswerExtractor(userAnswers: UserAnswers) extends UserAnswersExtractor {
  override def extract(answers: UserAnswers, data: GetTrust)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionErrors.PlaybackExtractionError, UserAnswers]] =
    Future.successful(Right(userAnswers))
}

class FakeFailingUserAnswerExtractor extends UserAnswersExtractor {
  override def extract(answers: UserAnswers, data: GetTrust)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionErrors.PlaybackExtractionError, UserAnswers]] =
    Future.successful(Left(FailedToExtractData("No beneficiaries")))
}
