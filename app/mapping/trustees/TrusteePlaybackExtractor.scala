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

package mapping.trustees

import mapping.PlaybackExtractor
import models.http.{PassportType, TrusteeType}
import models.{Address, MetaData, UserAnswers}
import pages.QuestionPage
import pages.trustees._

import java.time.LocalDate
import scala.util.Try

trait TrusteePlaybackExtractor[T <: TrusteeType] extends PlaybackExtractor[T] {

  override def addressYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeAddressYesNoPage(index)
  override def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeAddressInTheUKPage(index)
  override def addressPage(index: Int): QuestionPage[Address] = TrusteeAddressPage(index)

  override def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeCountryOfNationalityYesNoPage(index)
  override def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeCountryOfNationalityInTheUkYesNoPage(index)
  override def countryOfNationalityPage(index: Int): QuestionPage[String] = TrusteeCountryOfNationalityPage(index)

  override def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeCountryOfResidenceYesNoPage(index)
  override def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeCountryOfResidenceInTheUkYesNoPage(index)
  override def countryOfResidencePage(index: Int): QuestionPage[String] = TrusteeCountryOfResidencePage(index)

  override def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeMentalCapacityYesNoPage(index)

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = TrusteeUtrPage(index)

  override def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeDateOfBirthYesNoPage(index)
  override def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = TrusteeDateOfBirthPage(index)

  override def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = TrusteePassportIDCardYesNoPage(index)
  override def passportOrIdCardPage(index: Int): QuestionPage[PassportType] = TrusteePassportIDCardPage(index)

  override def ninoYesNoPage(index: Int): QuestionPage[Boolean] = TrusteeNinoYesNoPage(index)
  override def ninoPage(index: Int): QuestionPage[String] = TrusteeNinoPage(index)

  override def metaDataPage(index: Int): QuestionPage[MetaData] = TrusteeMetaData(index)

  def extractEmail(email: Option[String],
                   index: Int,
                   answers: UserAnswers): Try[UserAnswers] = {
    email match {
      case Some(value) =>
        answers.set(TrusteeEmailYesNoPage(index), true)
          .flatMap(_.set(TrusteeEmailPage(index), value))
      case _ =>
        answers.set(TrusteeEmailYesNoPage(index), false)
    }
  }
}
