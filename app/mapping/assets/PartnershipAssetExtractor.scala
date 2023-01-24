/*
 * Copyright 2023 HM Revenue & Customs
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
import models.http.DisplayTrustPartnershipType
import pages.assets.partnership._

import scala.util.Try

class PartnershipAssetExtractor extends PlaybackExtractor[DisplayTrustPartnershipType] {

  override val optionalEntity: Boolean = true

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustPartnershipType,
                                 index: Int): Try[UserAnswers] = {

    answers
      .flatMap(_.set(PartnershipDescriptionPage(index), entity.description))
      .flatMap(_.set(PartnershipStartDatePage(index), entity.partnershipStart))
  }

}
