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

package mapping

import com.google.inject.{ImplementedBy, Inject}
import mapping.PlaybackExtractionErrors.{FailedToCombineAnswers, PlaybackExtractionError}
import mapping.beneficiaries.BeneficiaryExtractor
import mapping.protectors.ProtectorExtractor
import mapping.settlors.{SettlorExtractor, TrustTypeExtractor}
import mapping.trustees.TrusteeExtractor
import models.UserAnswers
import models.http.GetTrust
import play.api.Logging
import models.UserAnswersCombinator._

@ImplementedBy(classOf[UserAnswersExtractorImpl])
trait UserAnswersExtractor extends PlaybackExtractor[GetTrust]

class UserAnswersExtractorImpl @Inject()(beneficiary: BeneficiaryExtractor,
                                         trustees: TrusteeExtractor,
                                         settlors: SettlorExtractor,
                                         trustType: TrustTypeExtractor,
                                         protectors: ProtectorExtractor,
                                         individualExtractor: OtherIndividualExtractor,
                                         correspondenceExtractor: CorrespondenceExtractor,
                                         trustDetailsExtractor: TrustDetailsExtractor
                                        ) extends UserAnswersExtractor with Logging {

  override def extract(answers: UserAnswers, data: GetTrust): Either[PlaybackExtractionError, UserAnswers] = {

    def answersCombined = for {
      ua <- correspondenceExtractor.extract(answers, data.correspondence).right
      ua1 <- beneficiary.extract(answers, data.trust.entities.beneficiary).right
      ua2 <- settlors.extract(answers, data.trust.entities).right
      ua3 <- trustType.extract(answers, Some(data.trust)).right
      ua4 <- protectors.extract(answers, data.trust.entities.protectors).right
      ua5 <- individualExtractor.extract(answers, data.trust.entities.naturalPerson).right
      ua6 <- trustees.extract(answers, data.trust.entities).right
      ua7 <- trustDetailsExtractor.extract(answers, data.trust.details).right
    } yield {
      List(ua, ua1, ua2, ua3, ua4, ua5, ua6, ua7).combine
    }

    answersCombined match {
      case Left(error) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] failed to unpack data to user answers, failed for $error")
        Left(error)
      case Right(None) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] failed to combine user answers")
        Left(FailedToCombineAnswers)
      case Right(Some(ua)) =>
        Right(ua)
    }
  }
}
