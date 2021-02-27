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
import models.UserAnswers
import models.UserAnswersCombinator._
import models.http.DisplayTrustEntitiesType
import sections.settlors.LivingSettlors

class SettlorExtractor @Inject()(deceasedSettlorExtractor: DeceasedSettlorExtractor,
                                 individualSettlorExtractor: IndividualSettlorExtractor,
                                 businessSettlorExtractor: BusinessSettlorExtractor) {

  def extract(answers: UserAnswers, data: DisplayTrustEntitiesType): Either[PlaybackExtractionError, UserAnswers] = {

    val settlors: List[UserAnswers] = List(
      deceasedSettlorExtractor.extract(answers, data.deceased.toList),
      individualSettlorExtractor.extract(answers, data.settlors.map(_.settlor).getOrElse(Nil)),
      businessSettlorExtractor.extract(answers, data.settlors.map(_.settlorCompany).getOrElse(Nil))
    ).collect {
      case Right(z) => z
    }

    val noDeceasedSettlor: Boolean = data.deceased.isEmpty

    (settlors, noDeceasedSettlor) match {
      case (Nil, _) => Left(FailedToExtractData("Settlor Extraction Error"))
      case (_, true) => settlors.combineArraysWithPath(LivingSettlors.path).map(Right(_)).getOrElse(Left(FailedToExtractData("Settlor Extraction Error")))
      case (_, false) => settlors.combine.map(Right(_)).getOrElse(Left(FailedToExtractData("Settlor Extraction Error")))}
  }

}
