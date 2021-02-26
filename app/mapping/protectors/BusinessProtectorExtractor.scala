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

import models.{InternationalAddress, MetaData, UKAddress, UserAnswers}
import models.http.DisplayTrustProtectorBusiness
import pages.protectors._
import pages.protectors.business._

import scala.util.{Success, Try}
import mapping.PlaybackImplicits._
import models.pages.IndividualOrBusiness

class BusinessProtectorExtractor extends ProtectorPlaybackExtractor[DisplayTrustProtectorBusiness] {

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustProtectorBusiness, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(ProtectorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(BusinessProtectorNamePage(index), entity.name))
      .flatMap(_.set(BusinessProtectorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(answers => extractUtr(entity, index, answers))
      .flatMap(answers => extractAddress(entity, index, answers))
      .flatMap {
        _.set(
          BusinessProtectorMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
  }

  private def extractUtr(protector: DisplayTrustProtectorBusiness, index: Int, answers: UserAnswers): Try[UserAnswers] =
    protector.identification.flatMap(_.utr) match {
      case Some(utr) =>
        answers.set(BusinessProtectorUtrYesNoPage(index), true)
          .flatMap(_.set(BusinessProtectorUtrPage(index), utr))
      case None =>
        answers.set(BusinessProtectorUtrYesNoPage(index), false)
    }

  private def extractAddress(protector: DisplayTrustProtectorBusiness, index: Int, answers: UserAnswers): Try[UserAnswers] =
    protector.identification.flatMap(_.address).convert match {
      case Some(uk: UKAddress) =>
        answers.set(BusinessProtectorAddressPage(index), uk)
          .flatMap(_.set(BusinessProtectorAddressUKYesNoPage(index), true))
          .flatMap(_.set(BusinessProtectorAddressYesNoPage(index), true))
      case Some(nonUk: InternationalAddress) =>
        answers.set(BusinessProtectorAddressPage(index), nonUk)
          .flatMap(_.set(BusinessProtectorAddressUKYesNoPage(index), false))
          .flatMap(_.set(BusinessProtectorAddressYesNoPage(index), true))
      case None =>
        protector.identification.flatMap(_.utr) match {
          case None => answers.set(BusinessProtectorAddressYesNoPage(index), false)
          case _ => Success(answers)
        }
    }
}
