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

package mapping.beneficiaries

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.UserAnswers
import models.http.DisplayTrustBeneficiaryType

class BeneficiaryExtractor @Inject()(charityBeneficiaryExtractor: CharityBeneficiaryExtractor,
                                     companyBeneficiaryExtractor: CompanyBeneficiaryExtractor,
                                     trustBeneficiaryExtractor: TrustBeneficiaryExtractor,
                                     otherBeneficiaryExtractor: OtherBeneficiaryExtractor,
                                     classOfBeneficiaryExtractor: ClassOfBeneficiaryExtractor,
                                     individualBeneficiaryExtractor: IndividualBeneficiaryExtractor,
                                     largeBeneficiaryExtractor: LargeBeneficiaryExtractor) extends PlaybackExtractor[DisplayTrustBeneficiaryType] {

  override def extract(answers: UserAnswers, data: DisplayTrustBeneficiaryType): Either[PlaybackExtractionError, UserAnswers] = {

    import models.UserAnswersCombinator._

    val beneficiaries: List[UserAnswers] = List(
      individualBeneficiaryExtractor.extract(answers, data.individualDetails),
      classOfBeneficiaryExtractor.extract(answers, data.unidentified),
      charityBeneficiaryExtractor.extract(answers, data.charity),
      trustBeneficiaryExtractor.extract(answers, data.trust),
      companyBeneficiaryExtractor.extract(answers, data.company),
      largeBeneficiaryExtractor.extract(answers, data.large),
      otherBeneficiaryExtractor.extract(answers, data.other)
    ).collect {
      case Right(z) => z
    }
    
    beneficiaries match {
      case Nil => Left(FailedToExtractData("Beneficiary Extraction Error"))
      case _ => beneficiaries.combine.map(Right.apply).getOrElse(Left(FailedToExtractData("Beneficiary Extraction Error")))
    }
  }
}