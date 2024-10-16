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

import com.google.inject.Inject
import models.UserAnswers
import models.UserAnswersCombinator._
import models.errors.{FailedToExtractData, TrustErrors}
import models.http.DisplayTrustAssets

class AssetsExtractor @Inject()(moneyAssetExtractor: MoneyAssetExtractor,
                                propertyOrLandAssetExtractor: PropertyOrLandAssetExtractor,
                                shareAssetExtractor: ShareAssetExtractor,
                                businessAssetExtractor: BusinessAssetExtractor,
                                partnershipAssetExtractor: PartnershipAssetExtractor,
                                otherAssetExtractor: OtherAssetExtractor,
                                nonEeaBusinessAssetExtractor: NonEeaBusinessAssetExtractor) {

  def extract(answers: UserAnswers, data: Option[DisplayTrustAssets]): Either[TrustErrors, UserAnswers] = {

    data match {

      case Some(a) =>

        val assets: List[UserAnswers] = List(
          moneyAssetExtractor.extract(answers, a.monetary),
          propertyOrLandAssetExtractor.extract(answers, a.propertyOrLand), // property or land not being extracted here
          shareAssetExtractor.extract(answers, a.shares),
          businessAssetExtractor.extract(answers, a.business),
          partnershipAssetExtractor.extract(answers, a.partnerShip),
          otherAssetExtractor.extract(answers, a.other),
          nonEeaBusinessAssetExtractor.extract(answers, a.nonEEABusiness)
        ).collect {
          case Right(z) => z
        }

        assets match {
          case Nil => Left(FailedToExtractData("Assets Extraction Error - No assets"))
          case _ => assets.combine match {
            case Some(value) =>
              Right(value)
            case None => Left(FailedToExtractData("Assets Extraction Error - Failed to combine asset answers"))
          }
        }

      case None =>
        Right(answers)

    }

  }
}
