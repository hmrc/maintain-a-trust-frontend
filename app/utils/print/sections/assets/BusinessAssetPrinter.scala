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

package utils.print.sections.assets

import javax.inject.Inject
import models.UserAnswers
import pages.QuestionPage
import pages.assets.business._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.assets.BusinessAsset
import utils.print.sections.{AnswerRowConverter, EntitiesPrinter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

class BusinessAssetPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[String] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = Seq(
    converter.stringQuestion(BusinessNamePage(index), userAnswers, "asset.business.name"),
    converter.stringQuestion(BusinessDescriptionPage(index), userAnswers, "asset.business.description", name),
    converter.addressQuestion(BusinessAddressPage(index), userAnswers, "asset.business.address", name),
    converter.currencyQuestion(BusinessValuePage(index), userAnswers, "asset.business.value", name)
  )

  override def namePath(index: Int): JsPath = BusinessNamePage(index).path

  override def section: QuestionPage[JsArray] = BusinessAsset

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = None

  override val subHeadingKey: Option[String] = Some("asset.business")

}
