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

package mapping

import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackImplicits.AddressConverter
import models.UserAnswers
import models.http.{DisplayNonEEABusinessType, DisplayTrustAssets}
import pages.assets.nonEeaBusiness._
import play.api.Logging

import scala.util.{Failure, Success, Try}

class AssetExtractor extends Logging {

  def extract(answers: UserAnswers, data: Option[DisplayTrustAssets]): Either[PlaybackExtractionError, UserAnswers] = {
    if (data.isDefined) {
      val updated = data.get.nonEEABusiness.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)) {
        case (answers, (entity, index)) =>
          updateUserAnswers(answers, entity, index)
      }

      updated match {
        case Success(a) =>
          Right(a)
        case Failure(exception) =>
          logger.warn(s"[UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
          Left(FailedToExtractData(DisplayNonEEABusinessType.toString))
      }
    } else {
      Right(answers)
    }
  }

  def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayNonEEABusinessType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(NonEeaBusinessLineNoPage(index), entity.lineNo))
      .flatMap(_.set(NonEeaBusinessNamePage(index), entity.orgName))
      .flatMap(_.set(NonEeaBusinessGoverningCountryPage(index), entity.govLawCountry))
      .flatMap(_.set(NonEeaBusinessAddressPage(index), entity.address.convert))
      .flatMap(_.set(NonEeaBusinessStartDatePage(index), entity.startDate))
      .flatMap(_.set(NonEeaBusinessEndDatePage(index), entity.endDate))
  }

}
