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

package utils.print.sections.assets

import javax.inject.Inject
import models.UserAnswers
import pages.QuestionPage
import pages.assets.propertyOrLand._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.assets.PropertyOrLandAsset
import utils.print.sections.{AnswerRowConverter, EntitiesPrinter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

class PropertyOrLandAssetPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[String] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.yesNoQuestion(PropertyOrLandAddressYesNoPage(index), userAnswers, "asset.propertyOrLand.addressYesNo"),
    converter.yesNoQuestion(PropertyOrLandAddressUkYesNoPage(index), userAnswers, "asset.propertyOrLand.addressUkYesNo"),
    converter.addressQuestion(PropertyOrLandAddressPage(index), userAnswers, "asset.propertyOrLand.address"),
    converter.stringQuestion(PropertyOrLandDescriptionPage(index), userAnswers, "asset.propertyOrLand.description"),
    converter.currencyQuestion(PropertyOrLandTotalValuePage(index), userAnswers, "asset.propertyOrLand.totalValue"),
    converter.yesNoQuestion(TrustOwnAllThePropertyOrLandPage(index), userAnswers, "asset.propertyOrLand.trustOwnAllYesNo"),
    converter.currencyQuestion(PropertyLandValueTrustPage(index), userAnswers, "asset.propertyOrLand.valueInTrust")
  )

  override def namePath(index: Int): JsPath = PropertyOrLandDescriptionPage(index).path

  override val optionalName: Boolean = true

  override def section: QuestionPage[JsArray] = PropertyOrLandAsset

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = None

  override val subHeadingKey: Option[String] = Some("asset.propertyOrLand")

}
