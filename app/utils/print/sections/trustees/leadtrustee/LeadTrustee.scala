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

package utils.print.sections.trustees.leadtrustee

import models.UserAnswers
import pages.correspondence.CorrespondenceAddressPage
import pages.trustees._
import play.api.i18n.Messages
import utils.print.sections.AnswerRowConverter
import viewmodels.AnswerRow

class LeadTrustee(converter: AnswerRowConverter) {

  def addressAnswers(index: Int, userAnswers: UserAnswers, name: String)
                    (implicit messages: Messages): Seq[Option[AnswerRow]] = {

    userAnswers.get(TrusteeAddressPage(index)) match {
      case Some(_) =>
        Seq(
          converter.yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
          converter.addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteeUkAddress", name)
        )
      case _ =>
        Seq(
          converter.addressQuestion(CorrespondenceAddressPage, userAnswers, "trusteeUkAddress", name)
        )
    }
  }

}
