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
import models.{Address, UserAnswers}
import models.http.PropertyLandType
import pages.QuestionPage
import pages.assets.propertyOrLand._

import scala.util.Try

class PropertyOrLandAssetExtractor extends PlaybackExtractor[PropertyLandType] {

  override val optionalEntity: Boolean = true

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = PropertyOrLandAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = PropertyOrLandAddressUkYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = PropertyOrLandAddressPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: PropertyLandType,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(PropertyOrLandDescriptionPage(index), entity.buildingLandName))
      .flatMap(answers => extractOptionalAddress(entity.address, index, answers))
      .flatMap(_.set(PropertyOrLandTotalValuePage(index), entity.valueFull))
      .flatMap(answers => extractPreviousValue(entity.valuePrevious, index, answers))
  }

  def extractPreviousValue(valuePrevious: Option[Long], index: Int, answers: UserAnswers): Try[UserAnswers] = {
    valuePrevious match {
      case Some(value) =>
        answers.set(TrustOwnAllThePropertyOrLandPage(index), false)
          .flatMap(_.set(PropertyLandValueTrustPage(index), value))
      case _ =>
        answers.set(TrustOwnAllThePropertyOrLandPage(index), true)
    }
  }

}
