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

import base.SpecBaseHelpers
import generators.Generators
import models.http.{AddressType, Correspondence}
import models.{UKAddress, UserAnswers}
import org.scalatest.{EitherValues, FreeSpec, MustMatchers}
import pages.correspondence._
import pages.trustdetails.TrustNamePage
import utils.Constants.GB

class CorrespondenceExtractorSpec extends FreeSpec with MustMatchers with EitherValues with Generators with SpecBaseHelpers {

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

      val ua = UserAnswers("fakeId", "utr")

      val extraction = correspondenceExtractor.extract(ua, correspondence)

      extraction.right.value.get(CorrespondenceAbroadIndicatorPage).get mustBe false
      extraction.right.value.get(TrustNamePage).get mustBe "Trust Ltd"
      extraction.right.value.get(CorrespondenceAddressPage).get mustBe UKAddress("line1", "line2", None, None, "NE991NE")
      extraction.right.value.get(CorrespondenceBpMatchStatusPage) mustNot be(defined)
      extraction.right.value.get(CorrespondencePhoneNumberPage).get mustBe "1225645"

    }

  }

}
