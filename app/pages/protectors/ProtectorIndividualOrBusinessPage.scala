/*
 * Copyright 2020 HM Revenue & Customs
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

package pages.protectors

import models.UserAnswers
import models.pages.IndividualOrBusiness
import models.pages.IndividualOrBusiness._
import pages.QuestionPage
import pages.protectors.business._
import pages.protectors.individual._
import play.api.libs.json.JsPath
import sections.Protectors

import scala.util.Try

final case class ProtectorIndividualOrBusinessPage(index : Int) extends QuestionPage[IndividualOrBusiness] {

  override def path: JsPath = Protectors.path \ index \ toString

  override def toString: String = "individualOrBusiness"

  override def cleanup(value: Option[IndividualOrBusiness], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(Individual) =>
        userAnswers.remove(BusinessProtectorUtrPage(index))
          .flatMap(_.remove(BusinessProtectorUtrYesNoPage(index)))
      case Some(Business) =>
        userAnswers.remove(IndividualProtectorDateOfBirthYesNoPage(index))
          .flatMap(_.remove(IndividualProtectorDateOfBirthPage(index)))
          .flatMap(_.remove(IndividualProtectorNINOYesNoPage(index)))
          .flatMap(_.remove(IndividualProtectorNINOPage(index)))
          .flatMap(_.remove(IndividualProtectorPassportIDCardYesNoPage(index)))
          .flatMap(_.remove(IndividualProtectorPassportIDCardPage(index)))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
