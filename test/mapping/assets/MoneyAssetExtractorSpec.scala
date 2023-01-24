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

import base.SpecBaseHelpers
import generators.Generators
import models.http._
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.assets.money.MoneyValuePage

class MoneyAssetExtractorSpec extends AnyFreeSpec with Matchers
  with EitherValues with Generators with SpecBaseHelpers {

  val assetExtractor: MoneyAssetExtractor = injector.instanceOf[MoneyAssetExtractor]

  "Business Asset Extractor" - {

    "when no assets" - {
      "must return user answers" in {

        val assets = Nil

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, assets)

        extraction mustBe 'right
        extraction.right.value.data mustBe ua.data

      }
    }

    "when there is a money asset" - {
      "must return user answers updated" in {

        val value: Long = 4000L

        val businessAssets = List(AssetMonetaryAmount(
          assetMonetaryAmount = value
        ))

        val ua = emptyUserAnswersForUtr

        val extraction = assetExtractor.extract(ua, businessAssets)

        extraction.right.value.get(MoneyValuePage(0)).get mustBe value
      }
    }
  }
}
