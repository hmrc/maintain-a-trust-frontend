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
import mapping.PlaybackImplicits._
import models.http.Correspondence
import models.{Address, InternationalAddress, UKAddress, UserAnswers}
import pages.correspondence._
import pages.trustdetails.TrustNamePage
import play.api.Logging

import scala.util.{Failure, Success, Try}

class CorrespondenceExtractor extends Logging {

  def extract(answers: UserAnswers, data: Correspondence): Either[PlaybackExtractionError, UserAnswers] = {
    val updated = answers
      .set(CorrespondenceAbroadIndicatorPage, data.abroadIndicator)
      .flatMap(_.set(TrustNamePage, data.name))
      .flatMap(answers => extractAddress(data.address.convert, answers))
      .flatMap(_.set(CorrespondenceBpMatchStatusPage, data.bpMatchStatus))
      .flatMap(_.set(CorrespondencePhoneNumberPage, data.phoneNumber))

    updated match {
      case Success(a) =>
        Right(a)
      case Failure(exception) =>
        logger.warn(s"[UTR/URN: ${answers.identifier}] failed to extract data due to ${exception.getMessage}")
        Left(FailedToExtractData(Correspondence.toString))
    }
  }

  private def extractAddress(address: Address, answers: UserAnswers): Try[UserAnswers] = address match {
    case uk: UKAddress => answers
      .set(CorrespondenceAddressInTheUKPage, true)
      .flatMap(_.set(CorrespondenceAddressPage, uk))
    case nonUk: InternationalAddress => answers
      .set(CorrespondenceAddressInTheUKPage, false)
      .flatMap(_.set(CorrespondenceAddressPage, nonUk))
  }

}
