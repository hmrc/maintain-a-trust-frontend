/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.data.EitherT
import models.UserAnswers
import models.errors.TrustErrors
import models.http.GetTrust
import uk.gov.hmrc.http.HeaderCarrier
import utils.TrustEnvelope.TrustEnvelope

import scala.concurrent.{ExecutionContext, Future}

class FakeUserAnswerExtractor(mockResult: Either[TrustErrors, UserAnswers]) extends UserAnswersExtractor {
  override def extract(answers: UserAnswers, data: GetTrust)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[UserAnswers] =
    EitherT[Future, TrustErrors, UserAnswers](Future.successful(mockResult))
}
