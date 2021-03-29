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

package utils.print.sections.protectors

import models.UserAnswers
import pages.protectors.business._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.print.sections.{AnswerRowConverter, EntityPrinter}
import viewmodels.AnswerRow

import javax.inject.Inject

class BusinessProtectorPrinter @Inject()(converter: AnswerRowConverter) extends EntityPrinter[String] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(BusinessProtectorNamePage(index), userAnswers, "companyProtectorName", name),
    converter.yesNoQuestion(BusinessProtectorUtrYesNoPage(index), userAnswers, "companyProtectorUtrYesNo", name),
    converter.stringQuestion(BusinessProtectorUtrPage(index), userAnswers, "companyProtectorUtr", name),
    converter.yesNoQuestion(BusinessProtectorAddressYesNoPage(index), userAnswers, "companyProtectorAddressYesNo", name),
    converter.yesNoQuestion(BusinessProtectorAddressUKYesNoPage(index), userAnswers, "companyProtectorAddressUkYesNo", name),
    converter.addressQuestion(BusinessProtectorAddressPage(index), userAnswers, "companyProtectorAddress", name)
  )

  override def namePath(index: Int): JsPath = BusinessProtectorNamePage(index).path

  override val subHeadingKey: Option[String] = Some("protector")

}
