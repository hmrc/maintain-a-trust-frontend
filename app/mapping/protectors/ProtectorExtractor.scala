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

package mapping.protectors

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.{DisplayTrustProtector, DisplayTrustProtectorBusiness, DisplayTrustProtectorsType, Protector}
import pages.protectors._
import play.api.Logger

import scala.util.{Failure, Success, Try}

class ProtectorExtractor @Inject()(individualProtectorExtractor: IndividualProtectorExtractor,
                                   businessProtectorExtractor: BusinessProtectorExtractor) extends PlaybackExtractor[Option[DisplayTrustProtectorsType]] {

  private val logger: Logger = Logger(getClass)

  override def extract(answers: UserAnswers, data: Option[DisplayTrustProtectorsType]): Either[PlaybackExtractionError, UserAnswers] = {

    data match {
      case Some(p) =>

        val protectors: List[Protector] = p.protector ++ p.protectorCompany

        protectors match {
          case Nil =>
            Right(updateAnswers(answers, doesTrustHaveProtector = false))
          case _ =>
            extractProtectors(updateAnswers(answers, doesTrustHaveProtector = true), protectors)
        }
      case None =>
        Right(updateAnswers(answers, doesTrustHaveProtector = false))
    }
  }

  def updateAnswers(answers: UserAnswers, doesTrustHaveProtector: Boolean): UserAnswers = {
    answers.set(DoesTrustHaveAProtectorYesNoPage(), doesTrustHaveProtector) match {
      case Success(ua) => ua
      case _ => answers
    }
  }

  def extractProtectors(answers: UserAnswers, data: List[Protector]): Either[PlaybackExtractionError, UserAnswers] = {
    val updated = data.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
      case (answers, (protector, index)) =>

        protector match {
          case x : DisplayTrustProtector => individualProtectorExtractor.extract(answers, index, x)
          case x : DisplayTrustProtectorBusiness => businessProtectorExtractor.extract(answers, index, x)
          case _ => Failure(new RuntimeException("Unexpected protector type"))
        }
    }

    updated match {
      case Success(a) =>
        Right(a)
      case Failure(_) =>
        logger.warn(s"[UTR: ${answers.utr}] failed to extract data")
        Left(FailedToExtractData(DisplayTrustProtectorsType.toString))
    }
  }
}