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

package mapping.assets

import mapping.PlaybackExtractor
import models.http.DisplaySharesType
import models.pages.{ShareClass, ShareType}
import models.pages.ShareType.Quoted
import models.UserAnswers
import pages.assets.shares._

import scala.util.Try

class ShareAssetExtractor extends PlaybackExtractor[DisplaySharesType] {

  override val optionalEntity: Boolean = true

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplaySharesType,
                                 index: Int): Try[UserAnswers] = {

    answers
      .flatMap(answers => extractPortfolioOrNonPortfolio(entity, index, answers))
      .flatMap(_.set(ShareNamePage(index), entity.orgName))
  }

  private def extractPortfolioOrNonPortfolio(entity: DisplaySharesType, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    entity.isPortfolio match {
      case Some(true) =>
        answers.set(SharesInAPortfolioPage(index), true)
          .flatMap(_.set(SharePortfolioNamePage(index), entity.orgName))
          .flatMap(_.set(SharePortfolioOnStockExchangePage(index), onStockExchange(entity.typeOfShare)))
          .flatMap(_.set(SharePortfolioQuantityInTrustPage(index), entity.numberOfShares))
          .flatMap(_.set(SharePortfolioValueInTrustPage(index), entity.value))
      case _ =>
//        val shareClass = ShareClass.fromDES(entity.shareClass)
        answers.set(SharesInAPortfolioPage(index), false)
          .flatMap(_.set(ShareCompanyNamePage(index), entity.orgName))
          .flatMap(_.set(ShareOnStockExchangePage(index), onStockExchange(entity.typeOfShare)))
          .flatMap(_.set(ShareClassPage(index), entity.shareClass))
          .flatMap(_.set(ShareQuantityInTrustPage(index), entity.numberOfShares))
          .flatMap(_.set(ShareValueInTrustPage(index), entity.value))
    }
  }

  private def onStockExchange(shareType: Option[ShareType]): Boolean = {
    shareType match {
      case Some(Quoted) => true
      case _ => false
    }
  }


}
