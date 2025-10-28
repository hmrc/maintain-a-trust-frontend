/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBaseHelpers
import generators.Generators
import models.UKAddress
import models.http.{AddressType, Correspondence}
import org.scalatest.matchers.must.Matchers
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.EitherValues
import pages.correspondence._
import pages.trustdetails.TrustNamePage
import utils.Constants.GB

class CorrespondenceExtractorSpec extends AnyFreeSpec with Matchers with EitherValues with Generators with SpecBaseHelpers {

  val correspondenceExtractor: CorrespondenceExtractor =
    injector.instanceOf[CorrespondenceExtractor]

  "Correspondence Extractor" - {

    "when there is correspondence" in {

      val correspondence = Correspondence(
        abroadIndicator = false,
        name = "Trust Ltd",
        address = AddressType(
          "line1", "line2", None, None, Some("NE991NE"), GB
        ),
        bpMatchStatus = None,
        phoneNumber = "1225645"
      )

      val ua = emptyUserAnswersForUtr

      val extraction = correspondenceExtractor.extract(ua, correspondence)

      extraction.value.get(CorrespondenceAbroadIndicatorPage).get mustBe false
      extraction.value.get(TrustNamePage).get mustBe "Trust Ltd"
      extraction.value.get(CorrespondenceAddressPage).get mustBe UKAddress("line1", "line2", None, None, "NE991NE")
      extraction.value.get(CorrespondenceBpMatchStatusPage) mustNot be(defined)
      extraction.value.get(CorrespondencePhoneNumberPage).get mustBe "1225645"

    }

  }

}
