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
import models.errors.TrustErrors
import models.http.PropertyLandType
import models.{Address, UserAnswers}
import pages.QuestionPage
import pages.assets.propertyOrLand._

class PropertyOrLandAssetExtractor extends PlaybackExtractor[PropertyLandType] {

  override val optionalEntity: Boolean = true

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = PropertyOrLandAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = PropertyOrLandAddressUkYesNoPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = PropertyOrLandAddressPage(index)

  override def updateUserAnswers(answers: Either[TrustErrors, UserAnswers],
                                 entity: PropertyLandType,
                                 index: Int): Either[TrustErrors, UserAnswers] = {
    answers
      .flatMap(_.set(PropertyOrLandDescriptionPage(index), entity.buildingLandName))
      .flatMap(answers => extractOptionalAddress(entity.address, index, answers))
      .flatMap(_.set(PropertyOrLandTotalValuePage(index), entity.valueFull))
      .flatMap(answers => extractOptionalPreviousValue(entity.valuePrevious, entity.valueFull, index, answers))
  }

  private def extractOptionalPreviousValue(valuePrevious: Option[Long],
                                           valueFull: Long,
                                           index: Int,
                                           answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    valuePrevious match {
      // if a trust owns part of a property or land
      case Some(valuePrevious) if valuePrevious < valueFull =>
        answers
          .set(TrustOwnAllThePropertyOrLandPage(index), false)
          .flatMap(_.set(PropertyLandValueTrustPage(index), valuePrevious))
      case _ =>
        answers.set(TrustOwnAllThePropertyOrLandPage(index), true)
    }
  }

}
