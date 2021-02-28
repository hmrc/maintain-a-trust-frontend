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
import models.pages.Tag.UpToDate
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

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustSettlor,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(SettlorIndividualNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(answers => extractMetaData(entity, index, answers))
      .flatMap(_.set(SettlorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }
}
