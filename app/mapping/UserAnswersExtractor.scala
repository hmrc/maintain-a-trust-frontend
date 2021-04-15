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
import connectors.TrustConnector
import mapping.PlaybackExtractionErrors.{FailedToCombineAnswers, PlaybackExtractionError}
import mapping.beneficiaries.BeneficiaryExtractor
import mapping.protectors.ProtectorExtractor
import mapping.settlors.{SettlorExtractor, TrustTypeExtractor}
import mapping.trustees.TrusteeExtractor
import models.UserAnswers
import models.UserAnswersCombinator._
import models.http.GetTrust
import play.api.Logging
import services.FeatureFlagService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserAnswersExtractorImpl])
trait UserAnswersExtractor {
  def extract(userAnswers: UserAnswers, playback: GetTrust)
             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionError, UserAnswers]]
}

class UserAnswersExtractorImpl @Inject()(
                                          featureFlagService: FeatureFlagService,
                                          trustsConnector: TrustConnector,
                                          beneficiariesExtractor: BeneficiaryExtractor,
                                          trusteesExtractor: TrusteeExtractor,
                                          settlorsExtractor: SettlorExtractor,
                                          nonEeaBusinessAssetExtractor: NonEeaBusinessAssetExtractor,
                                          trustTypeExtractor: TrustTypeExtractor,
                                          protectorsExtractor: ProtectorExtractor,
                                          otherIndividualsExtractor: OtherIndividualExtractor,
                                          correspondenceExtractor: CorrespondenceExtractor,
                                          trustDetailsExtractor: TrustDetailsExtractor
                                        ) extends UserAnswersExtractor with Logging {

  def extract(answers: UserAnswers, data: GetTrust)
             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[PlaybackExtractionError, UserAnswers]] = {

    for {
      is5mldEnabled <- featureFlagService.is5mldEnabled()
      trustDetails <- trustsConnector.getUntransformedTrustDetails(answers.identifier)
    } yield {
      val updatedAnswers = answers.copy(is5mldEnabled = is5mldEnabled, isUnderlyingData5mld = trustDetails.is5mld)

      def answersCombined: Either[PlaybackExtractionError, Option[UserAnswers]] = for {
        correspondence <- correspondenceExtractor.extract(updatedAnswers, data.correspondence).right
        beneficiaries <- beneficiariesExtractor.extract(updatedAnswers, data.trust.entities.beneficiary).right
        settlors <- settlorsExtractor.extract(updatedAnswers, data.trust.entities).right
        assets <- nonEeaBusinessAssetExtractor.extract(updatedAnswers, data.trust.assets.map(_.nonEEABusiness).getOrElse(Nil)).right
        trustType <- trustTypeExtractor.extract(updatedAnswers, data.trust).right
        protectors <- protectorsExtractor.extract(updatedAnswers, data.trust.entities.protectors).right
        otherIndividuals <- otherIndividualsExtractor.extract(updatedAnswers, data.trust.entities.naturalPerson.getOrElse(Nil)).right
        trustees <- trusteesExtractor.extract(updatedAnswers, data.trust.entities).right
        trustDetails <- trustDetailsExtractor.extract(updatedAnswers, data.trust.details).right
      } yield {
        List(correspondence, beneficiaries, settlors, assets, trustType, protectors, otherIndividuals, trustees, trustDetails).combine
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
}
