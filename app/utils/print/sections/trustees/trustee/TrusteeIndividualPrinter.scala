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

package utils.print.sections.trustees.trustee

import models.{FullName, UserAnswers}
import pages.trustees._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.print.sections.{AnswerRowConverter, EntityPrinter}
import viewmodels.AnswerRow

import javax.inject.Inject

class TrusteeIndividualPrinter @Inject()(converter: AnswerRowConverter) extends EntityPrinter[FullName] {

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.fullNameQuestion(TrusteeNamePage(index), userAnswers, "trusteeName"),
    converter.yesNoQuestion(TrusteeDateOfBirthYesNoPage(index), userAnswers, "trusteeDateOfBirthYesNo", name),
    converter.dateQuestion(TrusteeDateOfBirthPage(index), userAnswers, "trusteeDateOfBirth", name),
    converter.yesNoQuestion(TrusteeCountryOfNationalityYesNoPage(index), userAnswers, "trusteeCountryOfNationalityYesNo", name),
    converter.yesNoQuestion(TrusteeCountryOfNationalityInTheUkYesNoPage(index), userAnswers, "trusteeCountryOfNationalityUkYesNo", name),
    converter.countryQuestion(
      TrusteeCountryOfNationalityInTheUkYesNoPage(index),
      TrusteeCountryOfNationalityPage(index),
      userAnswers,
      "trusteeCountryOfNationality",
      name
    ),
    converter.yesNoQuestion(TrusteeNinoYesNoPage(index), userAnswers, "trusteeNinoYesNo", name),
    converter.ninoQuestion(TrusteeNinoPage(index), userAnswers, "trusteeNino", name),
    converter.yesNoQuestion(TrusteeCountryOfResidenceYesNoPage(index), userAnswers, "trusteeCountryOfResidenceYesNo", name),
    converter.yesNoQuestion(TrusteeCountryOfResidenceInTheUkYesNoPage(index), userAnswers, "trusteeCountryOfResidenceUkYesNo", name),
    converter.countryQuestion(
      TrusteeCountryOfResidenceInTheUkYesNoPage(index),
      TrusteeCountryOfResidencePage(index),
      userAnswers,
      "trusteeCountryOfResidence",
      name
    ),
    converter.yesNoQuestion(TrusteeAddressYesNoPage(index), userAnswers, "trusteeUkAddressYesNo", name),
    converter.yesNoQuestion(TrusteeAddressInTheUKPage(index), userAnswers, "trusteeLiveInTheUK", name),
    converter.addressQuestion(TrusteeAddressPage(index), userAnswers, "trusteeUkAddress", name),
    converter.yesNoQuestion(TrusteePassportIDCardYesNoPage(index), userAnswers, "trusteePassportOrIdCardYesNo", name),
    converter.passportOrIdCardQuestion(TrusteePassportIDCardPage(index), userAnswers, "trusteePassportOrIdCard", name),
    converter.yesNoDontKnowQuestion(TrusteeMentalCapacityYesNoPage(index), userAnswers, "trusteeMentalCapacityYesNo", name)
  )

  override def namePath(index: Int): JsPath = TrusteeNamePage(index).path

  override val subHeadingKey: Option[String] = Some("trustee")

}
