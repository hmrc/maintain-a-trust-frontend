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

package mapping.protectors

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import models.UserAnswers
import models.UserAnswersCombinator._
import models.http.DisplayTrustProtectorsType
import pages.protectors._
import play.api.Logging
import sections.Protectors

import scala.util.Success

class ProtectorExtractor @Inject()(individualProtectorExtractor: IndividualProtectorExtractor,
                                   businessProtectorExtractor: BusinessProtectorExtractor) extends Logging {

  def extract(answers: UserAnswers, data: Option[DisplayTrustProtectorsType]): Either[PlaybackExtractionError, UserAnswers] = {

    data match {
      case Some(p) =>

        val protectors: List[UserAnswers] = List(
          individualProtectorExtractor.extract(answers, p.protector),
          businessProtectorExtractor.extract(answers, p.protectorCompany)
        ).collect {
          case Right(z) => z
        }

        p.count match {
          case 0 =>
            Right(updateAnswers(answers, doesTrustHaveProtector = false))
          case _ =>
            protectors.combineArraysWithPath(Protectors.path) match {
              case Some(value) => Right(updateAnswers(value, doesTrustHaveProtector = true))
              case None => Left(FailedToExtractData("Protector Extraction Error"))
            }
        }
      case None =>
        Right(updateAnswers(answers, doesTrustHaveProtector = false))
    }
  }

  private def updateAnswers(answers: UserAnswers, doesTrustHaveProtector: Boolean): UserAnswers = {
    answers.set(DoesTrustHaveAProtectorYesNoPage(), doesTrustHaveProtector) match {
      case Success(ua) => ua
      case _ => answers
    }
  }
}
