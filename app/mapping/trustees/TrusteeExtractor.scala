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

package mapping.trustees

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.{DisplayTrustEntitiesType, Trustees}

class TrusteeExtractor @Inject()(trusteesExtractor: TrusteesExtractor) extends PlaybackExtractor[DisplayTrustEntitiesType] {

  override def extract(answers: UserAnswers, data: DisplayTrustEntitiesType): Either[PlaybackExtractionError, UserAnswers] = {

    import models.UserAnswersCombinator._

    val leadTrustee: List[Trustees] = List(data.leadTrustee.leadTrusteeOrg, data.leadTrustee.leadTrusteeInd).collect {
      case Some(x) => x
    }

    val nonLeadTrustees: Option[List[Trustees]] = for {
      trustees <- data.trustees
      companies = trustees.flatMap(_.trusteeOrg)
      individuals = trustees.flatMap(_.trusteeInd)
    } yield {
      companies ++ individuals
    }

    val allTrustees: List[Trustees] = leadTrustee ++ nonLeadTrustees.getOrElse(Nil)

    val trustees: List[UserAnswers] = List(
      trusteesExtractor.extract(answers, Some(allTrustees))
    ).collect {
      case Right(z) => z
    }

    (trustees, leadTrustee.isEmpty) match {
      case (Nil, false) => Left(FailedToExtractData("Trustee Extraction Error"))
      case (_, true) => Left(FailedToExtractData("Lead Trustee Extraction Error - Missing Lead Trustee"))
      case _ => trustees.combine.map(Right.apply).getOrElse(Left(FailedToExtractData("Trustee Extraction Error")))
    }
  }

}