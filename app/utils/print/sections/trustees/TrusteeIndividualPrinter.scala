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

package utils.print.sections.trustees

import javax.inject.Inject
import models.UserAnswers
import pages.trustees._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerSection

class TrusteeIndividualPrinter @Inject()(converter: AnswerRowConverter)
                                        (implicit messages: Messages) {

  def print(index: Int, userAnswers: UserAnswers): Option[Seq[AnswerSection]] = {

    userAnswers
      .get(TrusteeNamePage(index))
      .map(_.toString)
      .flatMap { name =>
        Some(
          Seq(
          AnswerSection(
            headingKey = Some(messages("answerPage.section.trustee.subheading", index + 1)),
            Seq(
              converter.fullNameQuestion(TrusteeNamePage(index), userAnswers, "trusteeName"),
              converter.yesNoQuestion(TrusteeDateOfBirthYesNoPage(index), userAnswers, "trusteeDateOfBirthYesNo", name),
              converter.dateQuestion(TrusteeDateOfBirthPage(index), userAnswers, "trusteeDateOfBirth", name),
              converter.yesNoQuestion(TrusteeNinoYesNoPage(index), userAnswers, "trusteeNinoYesNo", name),
              converter.ninoQuestion(TrusteeNinoPage(index), userAnswers, "trusteeNino", name),
              converter.yesNoQuestion(TrusteeAddressYesNoPage(index), userAnswers, "trusteeUkAddressYesNo", name),
              converter.yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
              converter.addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteeUkAddress", name),
              converter.yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
              converter.passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name)
            ).flatten,
            sectionKey = None
          ))
        )
      }
  }

}
