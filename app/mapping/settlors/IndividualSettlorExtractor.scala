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

import models.http.DisplayTrustSettlor
import models.pages.IndividualOrBusiness
import models.pages.Tag.Completed
import models.{PassportOrIdCardDetails, UserAnswers}
import pages.QuestionPage
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._

import java.time.LocalDate
import scala.util.Try

class IndividualSettlorExtractor extends SettlorPlaybackExtractor[DisplayTrustSettlor] {

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualNINOYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = SettlorIndividualNINOPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportOrIdCardDetails] = SettlorIndividualPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = SettlorIndividualDateOfBirthPage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = SettlorCountryOfNationalityPage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = SettlorCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = SettlorCountryOfResidencePage(index)

  override def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualMentalCapacityYesNoPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustSettlor,
                                 index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(SettlorIndividualNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.nationality, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractMentalCapacity(entity.legallyIncapable, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(_.set(SettlorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), Completed))
  }
}
