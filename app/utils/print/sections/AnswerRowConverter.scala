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

package utils.print.sections

import java.time.LocalDate

import javax.inject.Inject
import models.{Address, FullName, HowManyBeneficiaries, InternationalAddress, PassportOrIdCardDetails, UKAddress, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.HtmlFormat
import queries.Gettable
import utils.CheckAnswersFormatters
import viewmodels.AnswerRow

class AnswerRowConverter @Inject()(checkAnswersFormatters: CheckAnswersFormatters) {

  def ninoQuestion(query: Gettable[String],
                   userAnswers: UserAnswers,
                   labelKey: String,
                   messageArg: String = "")
                  (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(checkAnswersFormatters.formatNino(x)),
        None
      )
    }
  }

  def utr(userAnswers: UserAnswers,
          labelKey: String,
          messageArg: String = "")
         (implicit messages: Messages): Option[AnswerRow] = {

    Some(AnswerRow(
      messages(s"$labelKey.checkYourAnswersLabel", messageArg),
      checkAnswersFormatters.utr(userAnswers.utr),
      None
    ))
  }

  def utrQuestion(query: Gettable[String],
                  userAnswers: UserAnswers,
                  labelKey: String,
                  messageArg: String = "")
                 (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        checkAnswersFormatters.utr(x),
        None
      )
    }
  }

  def addressQuestion[T <: Address](query: Gettable[T],
                                    userAnswers: UserAnswers,
                                    labelKey: String,
                                    messageArg: String = "")
                                   (implicit messages: Messages, reads: Reads[T]): Option[AnswerRow] = {

    userAnswers.get(query) map { x =>
        AnswerRow(
          messages(s"$labelKey.checkYourAnswersLabel", messageArg),
          checkAnswersFormatters.addressFormatter(x),
          None
        )
    }
  }

  def internationalAddressQuestion(query: Gettable[InternationalAddress],
                                   userAnswers: UserAnswers,
                                   labelKey: String,
                                   messageArg: String = "")
                                  (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {
      international =>
        AnswerRow(
          messages(s"$labelKey.checkYourAnswersLabel", messageArg),
          checkAnswersFormatters.internationalAddress(international),
          None
        )
    }
  }

  def ukAddressQuestion(query: Gettable[UKAddress],
                        userAnswers: UserAnswers,
                        labelKey: String,
                        messageArg: String = "")
                       (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {
      uk =>
        AnswerRow(
          messages(s"$labelKey.checkYourAnswersLabel", messageArg),
          checkAnswersFormatters.ukAddress(uk),
          None
        )
    }
  }

  def percentageQuestion(query: Gettable[String],
                         userAnswers: UserAnswers,
                         labelKey: String,
                         messageArg: String = "")
                        (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        checkAnswersFormatters.percentage(x),
        None
      )
    }
  }

  def dateQuestion(query: Gettable[LocalDate],
                   userAnswers: UserAnswers,
                   labelKey: String,
                   messageArg: String = "")
                  (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(checkAnswersFormatters.formatDate(x)),
        None
      )
    }
  }

  def yesNoQuestion(query: Gettable[Boolean],
                    userAnswers: UserAnswers,
                    labelKey: String,
                    messageArg: String = "")
                   (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        checkAnswersFormatters.yesOrNo(x),
        None
      )
    }
  }

  def fullNameQuestion(query: Gettable[FullName],
                       userAnswers: UserAnswers,
                       labelKey: String,
                       messageArg: String = "")
                      (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(x.displayFullName),
        None
      )
    }
  }

  def stringQuestion[T](query: Gettable[T],
                        userAnswers: UserAnswers,
                        labelKey: String,
                        messageArg: String = "")
                       (implicit messages: Messages, rds: Reads[T]): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(x.toString),
        None
      )
    }
  }

  def numberOfBeneficiariesQuestion(query: Gettable[HowManyBeneficiaries],
                        userAnswers: UserAnswers,
                        labelKey: String,
                        messageArg: String = "")
                       (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        HtmlFormat.escape(messages(s"numberOfBeneficiaries.$x")),
        None
      )
    }
  }

  def passportOrIdCardQuestion(query: Gettable[PassportOrIdCardDetails],
                               userAnswers: UserAnswers,
                               labelKey: String,
                               messageArg: String = "")
                              (implicit messages: Messages): Option[AnswerRow] = {

    userAnswers.get(query) map {x =>
      AnswerRow(
        messages(s"$labelKey.checkYourAnswersLabel", messageArg),
        checkAnswersFormatters.passportOrIDCard(x),
        None
      )
    }
  }

}
