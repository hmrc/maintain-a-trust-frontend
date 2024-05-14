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

package mapping.assets

import mapping.PlaybackExtractor
import models.UserAnswers
import models.errors.TrustErrors
import models.http.DisplaySharesType
import models.pages.ShareClass
import models.pages.ShareType.Quoted
import pages.assets.shares._

class ShareAssetExtractor extends PlaybackExtractor[DisplaySharesType] {

  override val optionalEntity: Boolean = true

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplaySharesType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    answers
      .flatMap(answers => extractPortfolioOrNonPortfolio(entity, index, answers))
  }

  private def extractPortfolioOrNonPortfolio(entity: DisplaySharesType, index: Int, answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    answers.set(SharesInAPortfolioPage(index), entity.isPortfolio)
      .flatMap(_.set(ShareNamePage(index), entity.orgName))
      .flatMap(_.set(ShareOnStockExchangePage(index), entity.typeOfShare.contains(Quoted)))
      .flatMap(_.set(ShareClassPage(index), shareClass(entity)))
      .flatMap(_.set(ShareQuantityInTrustPage(index), entity.numberOfShares))
      .flatMap(_.set(ShareValueInTrustPage(index), entity.value))
  }

  private def shareClass(entity: DisplaySharesType): Option[ShareClass] = {
    if (entity.isPortfolio.contains(true)) None else entity.shareClassDisplay
  }

}
