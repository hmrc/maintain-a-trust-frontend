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

import models.UserAnswers
import pages.QuestionPage
import pages.assets.shares._
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, JsPath}
import sections.assets.ShareAsset
import utils.print.sections.{AnswerRowConverter, EntitiesPrinter, EntityPrinter}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class ShareAssetPrinter @Inject()(converter: AnswerRowConverter) extends EntitiesPrinter[JsArray] with EntityPrinter[String] {

  override def printSection(index: Int, userAnswers: UserAnswers)
                           (implicit messages: Messages): Option[AnswerSection] = {
    printAnswerRows(index, userAnswers)
  }

  override def answerRows(index: Int, userAnswers: UserAnswers, name: String)
                         (implicit messages: Messages): Seq[Option[AnswerRow]] = {

    userAnswers
      .get(SharesInAPortfolioPage(index))
      .fold[Seq[Option[AnswerRow]]](Nil)(inPortfolio => {
        converter.yesNoQuestion(SharesInAPortfolioPage(index), userAnswers, "asset.shares.inAPortfolioYesNo") +:
          (if (inPortfolio) {
            Seq(
              converter.stringQuestion(ShareNamePage(index), userAnswers, "asset.shares.portfolioName"),
              converter.yesNoQuestion(ShareOnStockExchangePage(index), userAnswers, "asset.shares.portfolioOnStockExchangeYesNo"),
              converter.stringQuestion(ShareQuantityInTrustPage(index), userAnswers, "asset.shares.portfolioQuantityInTrust"),
              converter.currencyQuestion(ShareValueInTrustPage(index), userAnswers, "asset.shares.portfolioValueInTrust")
            )
          } else {
            Seq(
              converter.stringQuestion(ShareNamePage(index), userAnswers, "asset.shares.companyName"),
              converter.enumQuestion(ShareClassPage(index), userAnswers, "asset.shares.class", "shares.class", name),
              converter.yesNoQuestion(ShareOnStockExchangePage(index), userAnswers, "asset.shares.onStockExchangeYesNo", name),
              converter.stringQuestion(ShareQuantityInTrustPage(index), userAnswers, "asset.shares.quantityInTrust", name),
              converter.currencyQuestion(ShareValueInTrustPage(index), userAnswers, "asset.shares.valueInTrust", name)
            )
          })
      })
  }

  override def namePath(index: Int): JsPath = ShareNamePage(index).path

  override val optionalName: Boolean = true

  override def section: QuestionPage[JsArray] = ShareAsset

  override def headingKey(migratingFromNonTaxableToTaxable: Boolean): Option[String] = None

  override val subHeadingKey: Option[String] = Some("asset.share")

}
