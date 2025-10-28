/*
 * Copyright 2025 HM Revenue & Customs
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
import mapping.PlaybackImplicits.AddressConverter
import models.UserAnswers
import models.errors.TrustErrors
import models.http.DisplayNonEEABusinessType
import pages.assets.nonEeaBusiness._

class NonEeaBusinessAssetExtractor extends PlaybackExtractor[DisplayNonEEABusinessType] {

  override val optionalEntity: Boolean = true

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: DisplayNonEEABusinessType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    answers
      .flatMap(_.set(NonEeaBusinessLineNoPage(index), entity.lineNo))
      .flatMap(_.set(NonEeaBusinessNamePage(index), entity.orgName))
      .flatMap(_.set(NonEeaBusinessGoverningCountryPage(index), entity.govLawCountry))
      .flatMap(_.set(NonEeaBusinessAddressPage(index), entity.address.convert))
      .flatMap(_.set(NonEeaBusinessStartDatePage(index), entity.startDate))
      .flatMap(_.set(NonEeaBusinessEndDatePage(index), entity.endDate))
  }

}
