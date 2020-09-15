/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.Validation
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain.{SimpleObjectReads, SimpleObjectWrites}

import scala.util.matching.Regex

case class TelephoneNumber(value: String) {
  require(TelephoneNumber.isValid(value), s"$value is not a valid telephone number.")
}

object TelephoneNumber extends (String => TelephoneNumber) {

  implicit val writes: Writes[TelephoneNumber] = new SimpleObjectWrites[TelephoneNumber](_.value)
  implicit val reads: Reads[TelephoneNumber] = new SimpleObjectReads[TelephoneNumber]("value", TelephoneNumber.apply)

  implicit class TelephoneNumberRequirements(tel: String) {

    /** Removes instances of (0) in a telephone number
     *
     * We allow numbers like +44(0)151 666 1337 for user convenience (see TRUS-2545)
     * Any instances of (0) then get removed in the JsonOps applyRules method in the backend before submitting to DES
     */
    def removeParentheses(): String = tel.replaceFirst("\\(0\\)", "")

    /** Verifies that the telephone number contains at least six digits
     *
     * The regex in the schema does not enforce this, but we deem numbers with less than six digits to be invalid (see TRUS-3123)
     */
    def hasMinimumOfSixDigits: Boolean = {
      def digit: Regex = "[0-9]".r
      digit.findAllIn(tel).length >= 6
    }
  }

  def isValid(tel: String): Boolean =
    tel != null &&
      tel.removeParentheses().matches(Validation.telephoneRegex) &&
      tel.removeParentheses().hasMinimumOfSixDigits
}
