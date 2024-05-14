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

package forms.mappings

import base.SpecBase

class TelephoneNumberSpec extends SpecBase {

  "Telephone number" must {

    "not have less than six digits" in {

      TelephoneNumber.isValid(" ") mustBe false
      TelephoneNumber.isValid("07543") mustBe false
      TelephoneNumber.isValid("+07543") mustBe false
      TelephoneNumber.isValid("+(0)07543") mustBe false
      TelephoneNumber.isValid("07 543") mustBe false
    }

    "not be more than 19 characters once first instance of (0) has been removed" in {

      TelephoneNumber.isValid("07700 900 982 12345") mustBe true
      TelephoneNumber.isValid("07700 900 982 123456") mustBe false
      TelephoneNumber.isValid("+44 0808 157 019234") mustBe true
      TelephoneNumber.isValid("+44 0808 157 0192345") mustBe false
      TelephoneNumber.isValid("+44(0) 0808 157 019234") mustBe true
      TelephoneNumber.isValid("+44(0) 0808 157 0192345") mustBe false
    }

    "not have multiple instances of (0)" in {
      TelephoneNumber.isValid("+44(0)151(0)666 1337") mustBe false
    }

    "be a valid telephone number" in {

      TelephoneNumber.isValid("01632 960 001") mustBe true
      TelephoneNumber.isValid("07700 900 982") mustBe true
      TelephoneNumber.isValid("+44 0808 157 0192") mustBe true
      TelephoneNumber.isValid("+44(0)151 666 1337") mustBe true
    }
  }
}
