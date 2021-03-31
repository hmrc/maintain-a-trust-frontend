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

package utils.print.sections.settlors

import models.UserAnswers
import pages.settlors.living_settlor._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.print.sections.{AnswerRowConverter, EntityPrinter}
import viewmodels.AnswerRow

import javax.inject.Inject

class SettlorCompanyPrinter @Inject()(converter: AnswerRowConverter) extends EntityPrinter[String] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(SettlorBusinessNamePage(index), userAnswers, "settlorBusinessName"),
    converter.yesNoQuestion(SettlorUtrYesNoPage(index), userAnswers, "settlorBusinessUtrYesNo", name),
    converter.stringQuestion(SettlorUtrPage(index), userAnswers, "settlorBusinessUtr", name),
    converter.yesNoQuestion(SettlorAddressYesNoPage(index), userAnswers, "settlorBusinessAddressYesNo", name),
    converter.yesNoQuestion(SettlorAddressUKYesNoPage(index), userAnswers, "settlorBusinessAddressUKYesNo", name),
    converter.addressQuestion(SettlorAddressPage(index), userAnswers, "settlorBusinessAddressUK", name),
    converter.kindOfBusinessQuestion(SettlorCompanyTypePage(index), userAnswers, "settlorBusinessType", name),
    converter.yesNoQuestion(SettlorCompanyTimePage(index), userAnswers, "settlorBusinessTime", name)
  )

  override def namePath(index: Int): JsPath = SettlorBusinessNamePage(index).path

  override val subHeadingKey: Option[String] = Some("settlor")

}