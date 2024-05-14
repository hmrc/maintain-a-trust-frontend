/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.print.sections.protectors

import models.UserAnswers
import models.pages.IndividualOrBusiness._
import pages.QuestionPage
import pages.protectors.ProtectorIndividualOrBusinessPage
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import sections.Protectors
import utils.print.sections.EntitiesPrinter
import viewmodels.AnswerSection

import javax.inject.Inject

class AllProtectorsPrinter @Inject()(individualProtectorPrinter: IndividualProtectorPrinter,
                                     businessProtectorPrinter: BusinessProtectorPrinter) extends EntitiesPrinter[JsArray] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    userAnswers.get(ProtectorIndividualOrBusinessPage(index)).flatMap {
      case Individual => individualProtectorPrinter.printAnswerRows(index, userAnswers)
      case Business => businessProtectorPrinter.printAnswerRows(index, userAnswers)
    }
  }

  override def section: QuestionPage[JsArray] = Protectors

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = Some("protectors")

}
