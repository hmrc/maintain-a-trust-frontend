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

import mapping.PlaybackExtractionErrors.InvalidExtractorState
import models.UserAnswers
import models.http.{DisplayTrustIdentificationOrgType, DisplayTrustLeadTrusteeOrgType}
import models.pages.IndividualOrBusiness
import pages.trustees._

import scala.util.{Failure, Try}

class OrganisationLeadTrusteeExtractor extends TrusteePlaybackExtractor[DisplayTrustLeadTrusteeOrgType] {

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustLeadTrusteeOrgType,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(TrusteeOrgNamePage(index), entity.name))
      .flatMap(answers => extractIdentification(entity.identification, index, answers))
      .flatMap(answers => extractEmail(entity.email, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), entity.phoneNumber))
      .flatMap(_.set(TrusteeSafeIdPage(index), entity.identification.safeId))
      .flatMap(answers => extractMetaData(entity, index, answers))
  }

  private def extractIdentification(identification: DisplayTrustIdentificationOrgType,
                                    index: Int,
                                    answers: UserAnswers): Try[UserAnswers] = {
    identification match {
      case DisplayTrustIdentificationOrgType(_, Some(utr), Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))
          .flatMap(answers => extractAddress(address, index, answers))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        answers.set(TrusteeUtrYesNoPage(index), false)
          .flatMap(answers => extractAddress(address, index, answers))

      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(TrusteeUtrYesNoPage(index), true)
          .flatMap(_.set(TrusteeUtrPage(index), utr))

      case DisplayTrustIdentificationOrgType(_, _, _) =>
        logger.error(s"[UTR/URN: ${answers.identifier}] no identification for lead trustee company returned in DisplayTrustOrEstate api")
        Failure(InvalidExtractorState)
    }
  }
}
