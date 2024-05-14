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

package mapping


import mapping.PlaybackImplicits._
import models.errors.{FailedToExtractData, TrustErrors}
import models.http.Correspondence
import models.{Address, InternationalAddress, UKAddress, UserAnswers}
import pages.correspondence._
import pages.trustdetails.TrustNamePage
import play.api.Logging

class CorrespondenceExtractor extends Logging {

  def extract(answers: UserAnswers, data: Correspondence): Either[TrustErrors, UserAnswers] = {
    val updated = answers
      .set(CorrespondenceAbroadIndicatorPage, data.abroadIndicator)
      .flatMap(_.set(TrustNamePage, data.name))
      .flatMap(answers => extractAddress(data.address.convert, answers))
      .flatMap(_.set(CorrespondenceBpMatchStatusPage, data.bpMatchStatus))
      .flatMap(_.set(CorrespondencePhoneNumberPage, data.phoneNumber))

    updated match {
      case Right(a) =>
        Right(a)
      case Left(_) =>
        logger.error(s"[CorrespondenceExtractor][extract][UTR/URN: ${answers.identifier}] failed to extract data.")
        Left(FailedToExtractData(Correspondence.toString))
    }
  }

  private def extractAddress(address: Address, answers: UserAnswers): Either[TrustErrors, UserAnswers] = address match {
    case uk: UKAddress => answers
      .set(CorrespondenceAddressInTheUKPage, true)
      .flatMap(_.set(CorrespondenceAddressPage, uk))
    case nonUk: InternationalAddress => answers
      .set(CorrespondenceAddressInTheUKPage, false)
      .flatMap(_.set(CorrespondenceAddressPage, nonUk))
  }

}
