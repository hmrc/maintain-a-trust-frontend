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

package mapping.protectors

import models.http.DisplayTrustProtector
import models.pages.IndividualOrBusiness
import models.{Address, MetaData, PassportOrIdCardDetails, UserAnswers}
import pages.QuestionPage
import pages.protectors.ProtectorIndividualOrBusinessPage
import pages.protectors.individual._

import java.time.LocalDate
import scala.util.Try

class IndividualProtectorExtractor extends ProtectorPlaybackExtractor[DisplayTrustProtector] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = IndividualProtectorAddressPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorNINOYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = IndividualProtectorNINOPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportOrIdCardDetails] = IndividualProtectorPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = IndividualProtectorDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = IndividualProtectorDateOfBirthPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustProtector,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(IndividualProtectorNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(_.set(IndividualProtectorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap {
        _.set(
          IndividualProtectorMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
  }

}
