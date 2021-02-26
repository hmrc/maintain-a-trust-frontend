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

package views

class ViewUtilsSpec extends ViewSpecBase {

  "View utils" must {

    "render the subheading for a utr" in {
      val heading = ViewUtils.subheading("1234567890")
      heading mustBe "This trust’s UTR: 1234567890"
    }

    "render the subheading for a urn" in {
      val heading = ViewUtils.subheading("ABTRUST12345678")
      heading mustBe "This trust’s URN: ABTRUST12345678"
    }

  }

}
