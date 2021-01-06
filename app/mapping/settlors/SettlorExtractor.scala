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

package mapping.settlors

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.DisplayTrustEntitiesType
import models.UserAnswersCombinator._

class SettlorExtractor @Inject()(deceasedSettlorExtractor: DeceasedSettlorExtractor,
                                 livingSettlorExtractor: LivingSettlorExtractor) extends PlaybackExtractor[DisplayTrustEntitiesType] {

  override def extract(answers: UserAnswers, data: DisplayTrustEntitiesType): Either[PlaybackExtractionError, UserAnswers] = {

    val livingSettlors = for {
      settlors <- data.settlors
      companies = settlors.settlorCompany.getOrElse(Nil)
      living = settlors.settlor.getOrElse(Nil)
    } yield  companies ++ living

    val settlors: List[UserAnswers] = List(
      deceasedSettlorExtractor.extract(answers, data.deceased),
      livingSettlorExtractor.extract(answers, livingSettlors)
    ).collect {
      case Right(z) => z
    }

    settlors match {
      case Nil => Left(FailedToExtractData("Settlor Extraction Error"))
      case _ => settlors.combine.map(Right.apply).getOrElse(Left(FailedToExtractData("Settlor Extraction Error")))
    }
  }

}