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


import models.pages.RoleInCompany
import models.{Address, Description, FullName, PassportOrIdCardDetails, URN, UTR, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import utils.CheckAnswersFormatters
import viewmodels.AnswerRow

import java.time.LocalDate
import javax.inject.Inject

class AnswerRowConverter @Inject()(checkAnswersFormatters: CheckAnswersFormatters) {

  def ninoQuestion(query: Gettable[String],
                   userAnswers: UserAnswers,
                   labelKey: String,
                   messageArg: String = "")
                  (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: String) => checkAnswersFormatters.formatNino(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def whichIdentifier(userAnswers: UserAnswers)
                     (implicit messages: Messages): Option[AnswerRow] = {

    val key = userAnswers.identifierType match {
      case UTR => "uniqueTaxReference.checkYourAnswersLabel"
      case URN => "uniqueReferenceNumber.checkYourAnswersLabel"
    }

    Some(AnswerRow(messages(s"whichIdentifier.checkYourAnswersLabel"), HtmlFormat.escape(messages(key)), None))
  }

  def identifier(userAnswers: UserAnswers,
                 labelKey: String,
                 messageArg: String = "")
                (implicit messages: Messages): Option[AnswerRow] = {
    Some(AnswerRow(
      messages(s"$labelKey.checkYourAnswersLabel", messageArg),
      HtmlFormat.escape(userAnswers.identifier),
      None
    ))
  }

  def addressQuestion[T <: Address](query: Gettable[T],
                                    userAnswers: UserAnswers,
                                    labelKey: String,
                                    messageArg: String = "")
                                   (implicit messages: Messages, reads: Reads[T]): Option[AnswerRow] = {
    val format = (x: T) => checkAnswersFormatters.addressFormatter(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def percentageQuestion(query: Gettable[String],
                         userAnswers: UserAnswers,
                         labelKey: String,
                         messageArg: String = "")
                        (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: String) => checkAnswersFormatters.percentage(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def currencyQuestion(query: Gettable[Long],
                       userAnswers: UserAnswers,
                       labelKey: String,
                       messageArg: String = "")
                      (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: Long) => checkAnswersFormatters.currency(x.toString)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def dateQuestion(query: Gettable[LocalDate],
                   userAnswers: UserAnswers,
                   labelKey: String,
                   messageArg: String = "")
                  (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: LocalDate) => HtmlFormat.escape(checkAnswersFormatters.formatDate(x))
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def yesNoQuestion(query: Gettable[Boolean],
                    userAnswers: UserAnswers,
                    labelKey: String,
                    messageArg: String = "")
                   (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: Boolean) => checkAnswersFormatters.yesOrNo(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def fullNameQuestion(query: Gettable[FullName],
                       userAnswers: UserAnswers,
                       labelKey: String,
                       messageArg: String = "")
                      (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: FullName) => HtmlFormat.escape(x.displayFullName)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def stringQuestion[T](query: Gettable[T],
                        userAnswers: UserAnswers,
                        labelKey: String,
                        messageArg: String = "")
                       (implicit messages: Messages, rds: Reads[T]): Option[AnswerRow] = {
    val format = (x: T) => HtmlFormat.escape(x.toString)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def roleInCompanyQuestion(query: Gettable[RoleInCompany],
                            userAnswers: UserAnswers,
                            labelKey: String,
                            messageArg: String = "")
                           (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: RoleInCompany) => checkAnswersFormatters.formatRoleInCompany(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def passportOrIdCardQuestion(query: Gettable[PassportOrIdCardDetails],
                               userAnswers: UserAnswers,
                               labelKey: String,
                               messageArg: String = "")
                              (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: PassportOrIdCardDetails) => checkAnswersFormatters.passportOrIDCard(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def descriptionQuestion(query: Gettable[Description],
                          userAnswers: UserAnswers,
                          labelKey: String,
                          messageArg: String)
                         (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: Description) => checkAnswersFormatters.description(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def enumQuestion[T](query: Gettable[T],
                      userAnswers: UserAnswers,
                      labelKey: String,
                      enumKey: String,
                      messageArg: String = "")
                     (implicit messages: Messages, reads: Reads[T]): Option[AnswerRow] = {
    val format = (x: T) => checkAnswersFormatters.formatEnum(enumKey, x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  def countryQuestion(isUkQuery: Gettable[Boolean],
                      query: Gettable[String],
                      userAnswers: UserAnswers,
                      labelKey: String,
                      messageArg: String)
                     (implicit messages: Messages): Option[AnswerRow] = {
    userAnswers.get(isUkQuery) flatMap {
      case false =>
        countryQuestion(query, userAnswers, labelKey, messageArg)
      case _ =>
        None
    }
  }

  def countryQuestion(query: Gettable[String],
                      userAnswers: UserAnswers,
                      labelKey: String,
                      messageArg: String = "")
                     (implicit messages: Messages): Option[AnswerRow] = {
    val format = (x: String) => checkAnswersFormatters.country(x)
    question(query, userAnswers, labelKey, format, messageArg)
  }

  private def answerRow[T](labelKey: String, messageArg: String, answer: Html)
                          (implicit messages: Messages): AnswerRow = {
    AnswerRow(
      label = messages(s"$labelKey.checkYourAnswersLabel", messageArg),
      answer = answer,
      changeUrl = None
    )
  }

  private def question[T](query: Gettable[T],
                          userAnswers: UserAnswers,
                          labelKey: String,
                          format: T => Html,
                          messageArg: String)
                         (implicit messages: Messages, rds: Reads[T]): Option[AnswerRow] = {
    userAnswers.get(query) map { x =>
      answerRow(labelKey, messageArg, format(x))
    }
  }

}
