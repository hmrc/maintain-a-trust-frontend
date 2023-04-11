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

import com.google.inject.{ImplementedBy, Inject}
import connectors.TrustConnector
import mapping.PlaybackExtractionErrors.{FailedToCombineAnswers, PlaybackExtractionError}
import mapping.assets.AssetsExtractor
import mapping.beneficiaries.BeneficiaryExtractor
import mapping.protectors.ProtectorExtractor
import mapping.settlors.{SettlorExtractor, TrustTypeExtractor}
import mapping.trustees.TrusteeExtractor
import models.UserAnswers
import models.UserAnswersCombinator._
import models.http.GetTrust
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserAnswersExtractorImpl])
trait UserAnswersExtractor {
  def extract(userAnswers: UserAnswers, playback: GetTrust)
             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionError, UserAnswers]]
}

class UserAnswersExtractorImpl @Inject()(
                                          trustsConnector: TrustConnector,
                                          beneficiariesExtractor: BeneficiaryExtractor,
                                          trusteesExtractor: TrusteeExtractor,
                                          settlorsExtractor: SettlorExtractor,
                                          assetsExtractor: AssetsExtractor,
                                          trustTypeExtractor: TrustTypeExtractor,
                                          protectorsExtractor: ProtectorExtractor,
                                          otherIndividualsExtractor: OtherIndividualExtractor,
                                          correspondenceExtractor: CorrespondenceExtractor,
                                          trustDetailsExtractor: TrustDetailsExtractor
                                        ) extends UserAnswersExtractor with Logging {

  def extract(answers: UserAnswers, data: GetTrust)
             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionError, UserAnswers]] = {

    for {
      trustDetails <- trustsConnector.getUntransformedTrustDetails(answers.identifier)
    } yield {

      val updatedAnswers = answers.copy(
        isUnderlyingData5mld = trustDetails.is5mld,
        isUnderlyingDataTaxable = trustDetails.isTaxable
      )

      def answersCombined: Either[PlaybackExtractionError, Option[UserAnswers]] = for {
        correspondence <- correspondenceExtractor.extract(updatedAnswers, data.correspondence)
        beneficiaries <- beneficiariesExtractor.extract(updatedAnswers, data.trust.entities.beneficiary)
        settlors <- settlorsExtractor.extract(updatedAnswers, data.trust.entities)
        assets <- assetsExtractor.extract(updatedAnswers, data.trust.assets)
        trustType <- trustTypeExtractor.extract(updatedAnswers, data.trust)
        protectors <- protectorsExtractor.extract(updatedAnswers, data.trust.entities.protectors)
        otherIndividuals <- otherIndividualsExtractor.extract(updatedAnswers, data.trust.entities.naturalPerson.getOrElse(Nil))
        trustees <- trusteesExtractor.extract(updatedAnswers, data.trust.entities)
        trustDetails <- trustDetailsExtractor.extract(updatedAnswers, data.trust.details)
      } yield {
        List(correspondence, beneficiaries, settlors, assets, trustType, protectors, otherIndividuals, trustees, trustDetails).combine
      }

      answersCombined match {
        case Left(error) =>
          logger.error(s"[UserAnswersExtractorImpl][extract][UTR/URN: ${answers.identifier}] failed to extract session data to user answers, failed for $error")
          Left(error)
        case Right(None) =>
          logger.error(s"[UserAnswersExtractorImpl][extract][UTR/URN: ${answers.identifier}] failed to combine user answers")
          Left(FailedToCombineAnswers)
        case Right(Some(ua)) =>
          Right(ua)
      }
    }
  }
}
