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

package mapping.settlors

import mapping.PlaybackExtractor
import models.Address
import models.http.SettlorType
import pages.QuestionPage
import pages.settlors.living_settlor.{SettlorAddressPage, SettlorAddressUKYesNoPage, SettlorAddressYesNoPage}

trait SettlorPlaybackExtractor[T <: SettlorType] extends PlaybackExtractor[T] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = SettlorAddressUKYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = SettlorAddressPage(index)

}
