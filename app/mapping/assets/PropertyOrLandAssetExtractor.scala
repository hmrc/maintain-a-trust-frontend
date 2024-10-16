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
      .flatMap(answers => extractPreviousValue(entity.valuePrevious, entity.valueFull, index, answers))
  }

  /** TODO
   * This method is confusing.
   * valueFull is always set by the question 'What is the current total value of the property or land?'
   * valuePrevious is set both by the above question, and if the user answers no to the question
   * 'Does the trust own all of the property or land?'
   * it is also set by the question 'What is the value of the property or land owned by the trust?'
   */
  private def extractPreviousValue(valuePrevious: Option[Long],
                                   valueFull: Long,
                                   index: Int,
                                   answers: UserAnswers): Either[TrustErrors, UserAnswers] = {
    valuePrevious match {
      // case for if a trust owns part of a property or land
      case Some(previousValue) if previousValue < valueFull =>
        answers
          .set(TrustOwnAllThePropertyOrLandPage(index), false)
          .flatMap(_.set(PropertyLandValueTrustPage(index), previousValue))
      case _ =>
        answers.set(TrustOwnAllThePropertyOrLandPage(index), true)
    }
  }

}
