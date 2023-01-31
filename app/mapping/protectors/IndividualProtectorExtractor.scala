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

package mapping.protectors

import models.errors.TrustErrors
import models.http.{DisplayTrustProtector, PassportType}
import models.pages.IndividualOrBusiness
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.protectors.ProtectorIndividualOrBusinessPage
import pages.protectors.individual._

import java.time.LocalDate

class IndividualProtectorExtractor extends ProtectorPlaybackExtractor[DisplayTrustProtector] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = IndividualProtectorAddressPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorNINOYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = IndividualProtectorNINOPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportType] = IndividualProtectorPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = IndividualProtectorDateOfBirthPage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = IndividualProtectorCountryOfNationalityPage(index)
  
  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = IndividualProtectorCountryOfResidencePage(index)

  override def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorMentalCapacityYesNoPage(index)

  override def metaDataPage(index: Int): QuestionPage[MetaData] = IndividualProtectorMetaData(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayTrustProtector,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(IndividualProtectorNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.nationality, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractMentalCapacity(entity.legallyIncapable, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(_.set(IndividualProtectorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
  }

}
