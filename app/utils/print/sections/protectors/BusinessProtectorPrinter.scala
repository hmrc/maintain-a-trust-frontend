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

package utils.print.sections.protectors

import javax.inject.Inject
import models.UserAnswers
import pages.protectors.business._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class BusinessProtectorPrinter @Inject()(converter: AnswerRowConverter)
                                        (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Seq[AnswerSection] =
    userAnswers.get(BusinessProtectorNamePage(index)).map { protectorName =>
      Seq(
        AnswerSection(
          headingKey = Some(messages("answerPage.section.protector.subheading", index + 1)),
          Seq(
            converter.stringQuestion(BusinessProtectorNamePage(index), userAnswers, "companyProtectorName", protectorName),
            converter.yesNoQuestion(BusinessProtectorUtrYesNoPage(index), userAnswers, "companyProtectorUtrYesNo", protectorName),
            converter.stringQuestion(BusinessProtectorUtrPage(index), userAnswers, "companyProtectorUtr", protectorName),
            converter.yesNoQuestion(BusinessProtectorAddressYesNoPage(index), userAnswers, "companyProtectorAddressYesNo", protectorName),
            converter.yesNoQuestion(BusinessProtectorAddressUKYesNoPage(index), userAnswers, "companyProtectorAddressUkYesNo", protectorName),
            converter.addressQuestion(BusinessProtectorAddressPage(index), userAnswers, "companyProtectorAddress", protectorName)
          ).flatten,
          None
        )
      )
    }.getOrElse(Nil)

}
