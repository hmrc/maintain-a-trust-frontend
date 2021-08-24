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

package mapping

import models.http.{NaturalPersonType, PassportType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.individual._

import java.time.LocalDate
import scala.util.Try

class OtherIndividualExtractor extends PlaybackExtractor[NaturalPersonType] {

  override val optionalEntity: Boolean = true

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = OtherIndividualAddressPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualNationalInsuranceYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = OtherIndividualNationalInsuranceNumberPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportType] = OtherIndividualPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = OtherIndividualDateOfBirthPage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = OtherIndividualCountryOfNationalityPage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = OtherIndividualCountryOfResidencePage(index)

  override def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = OtherIndividualMentalCapacityYesNoPage(index)

  override def metaDataPage(index: Int): QuestionPage[MetaData] = OtherIndividualMetaData(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: NaturalPersonType,
                                 index: Int): Try[UserAnswers] = {
    super.updateUserAnswers(answers, entity, index)
      .flatMap(_.set(OtherIndividualNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractCountryOfNationality(entity.nationality, index, answers))
      .flatMap(answers => extractCountryOfResidence(entity.countryOfResidence, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(_.set(OtherIndividualSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractMentalCapacity(entity.legallyIncapable, index, answers))
  }

}
