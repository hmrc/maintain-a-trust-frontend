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

package mapping.trustees

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import models.UserAnswers
import models.UserAnswersCombinator._
import models.http.DisplayTrustEntitiesType
import sections.Trustees

class TrusteeExtractor @Inject()(individualLeadTrusteeExtractor: IndividualLeadTrusteeExtractor,
                                 organisationLeadTrusteeExtractor: OrganisationLeadTrusteeExtractor,
                                 individualTrusteeExtractor: IndividualTrusteeExtractor,
                                 organisationTrusteeExtractor: OrganisationTrusteeExtractor) {

  def extract(answers: UserAnswers, data: DisplayTrustEntitiesType): Either[PlaybackExtractionError, UserAnswers] = {

    val trustees: List[UserAnswers] = List(
      individualLeadTrusteeExtractor.extract(answers, data.leadTrustee.leadTrusteeInd.map(List(_)).getOrElse(Nil)),
      organisationLeadTrusteeExtractor.extract(answers, data.leadTrustee.leadTrusteeOrg.map(List(_)).getOrElse(Nil)),
      individualTrusteeExtractor.extract(answers, data.trustees.getOrElse(Nil).flatMap(_.trusteeInd)),
      organisationTrusteeExtractor.extract(answers, data.trustees.getOrElse(Nil).flatMap(_.trusteeOrg))
    ).collect {
      case Right(z) => z
    }

    val noLeadTrustee: Boolean = data.leadTrustee.leadTrusteeInd.isEmpty && data.leadTrustee.leadTrusteeOrg.isEmpty

    (trustees, noLeadTrustee) match {
      case (Nil, false) => Left(FailedToExtractData("Trustee Extraction Error"))
      case (_, true) => Left(FailedToExtractData("Lead Trustee Extraction Error - Missing Lead Trustee"))
      case _ =>
        trustees.combineArraysWithPath(Trustees.path) match {
          case Some(value) => Right(value)
          case None => Left(FailedToExtractData("Trustee Extraction Error"))
        }
    }
  }

}
