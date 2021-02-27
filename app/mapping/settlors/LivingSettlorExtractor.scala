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

import mapping.PlaybackExtractor
import models.http._
import models.pages.IndividualOrBusiness
import models.pages.Tag.UpToDate
import models.{Address, MetaData, PassportOrIdCardDetails, UserAnswers}
import pages.QuestionPage
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._

import java.time.LocalDate
import scala.util.{Failure, Try}

class LivingSettlorExtractor extends PlaybackExtractor[LivingSettlor] {

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = SettlorUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = SettlorUtrPage(index)

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = SettlorAddressPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualNINOYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = SettlorIndividualNINOPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualPassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportOrIdCardDetails] = SettlorIndividualPassportIDCardPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = SettlorIndividualDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = SettlorIndividualDateOfBirthPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: LivingSettlor, index: Int): Try[UserAnswers] = {
    entity match {
      case x: DisplayTrustSettlorCompany => extractSettlorCompany(answers, index, x)
      case x: DisplayTrustSettlor => extractSettlorIndividual(answers, index, x)
      case _ => Failure(new RuntimeException("Unexpected settlor type"))
    }
  }

  private def extractSettlorIndividual(answers: Try[UserAnswers],
                                       index: Int,
                                       individual: DisplayTrustSettlor): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(SettlorIndividualNamePage(index), individual.name))
      .flatMap(answers => extractDateOfBirth(individual.dateOfBirth, index, answers))
      .flatMap(answers => extractIndIdentification(individual.identification, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = individual.lineNo.getOrElse(""),
            bpMatchStatus = individual.bpMatchStatus,
            entityStart = individual.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorSafeIdPage(index), individual.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }

  private def extractSettlorCompany(answers: Try[UserAnswers],
                                    index: Int,
                                    company: DisplayTrustSettlorCompany): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(SettlorBusinessNamePage(index), company.name))
      .flatMap(answers => extractOrgIdentification(company.identification, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = company.lineNo.getOrElse(""),
            bpMatchStatus = company.bpMatchStatus,
            entityStart = company.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorCompanyTypePage(index), company.companyType))
      .flatMap(_.set(SettlorCompanyTimePage(index), company.companyTime))
      .flatMap(_.set(SettlorSafeIdPage(index), company.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }

}
