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

package mapping.trustees

import models.UserAnswers
import models.errors.{InvalidExtractorState, TrustErrors}
import models.http.{DisplayTrustIdentificationType, DisplayTrustLeadTrusteeIndType}
import models.pages.IndividualOrBusiness
import pages.trustees._

class IndividualLeadTrusteeExtractor extends TrusteePlaybackExtractor[DisplayTrustLeadTrusteeIndType] {

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustLeadTrusteeIndType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(IsThisLeadTrusteePage(index), true))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), entity.name))
      .flatMap(_.set(TrusteeDateOfBirthPage(index), entity.dateOfBirth))
      .flatMap(answers => extractCountryOfNationality(entity.nationality, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractMentalCapacity(entity.legallyIncapable, index, answers))
      .flatMap(answers => extractIdentification(entity.identification, index, answers))
      .flatMap(answers => extractEmail(entity.email, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), entity.phoneNumber))
      .flatMap(_.set(TrusteeSafeIdPage(index), entity.identification.safeId))
  }

  private def extractIdentification(identification: DisplayTrustIdentificationType,
                                    index: Int,
                                    answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    identification match {
      case DisplayTrustIdentificationType(_, Some(nino), None, Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))
          .flatMap(answers => extractAddress(address, index, answers))
      case DisplayTrustIdentificationType(_, None, Some(passport), Some(address)) =>
        answers.set(TrusteeAUKCitizenPage(index), false)
          .flatMap(answers => extractPassportIdCard(passport, index, answers))
          .flatMap(answers => extractAddress(address, index, answers))
      case DisplayTrustIdentificationType(_, None, Some(_), None) =>
        logger.error(s"[IndividualLeadTrusteeExtractor][extractIdentification][UTR/URN: ${answers.identifier}] only passport identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Left(InvalidExtractorState)
      case DisplayTrustIdentificationType(_, Some(nino), None, None) =>
        answers.set(TrusteeAUKCitizenPage(index), true)
          .flatMap(_.set(TrusteeNinoPage(index), nino))
      case DisplayTrustIdentificationType(_, None, None, Some(_)) =>
        logger.error(s"[IndividualLeadTrusteeExtractor][extractIdentification][UTR/URN: ${answers.identifier}] only address identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Left(InvalidExtractorState)
      case DisplayTrustIdentificationType(_, _, _, _) =>
        logger.error(s"[IndividualLeadTrusteeExtractor][extractIdentification][UTR/URN: ${answers.identifier}] no identification for lead trustee individual returned in DisplayTrustOrEstate api")
        Left(InvalidExtractorState)
    }
  }
}
