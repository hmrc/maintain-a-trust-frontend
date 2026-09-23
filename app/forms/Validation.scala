/*
 * Copyright 2026 HM Revenue & Customs
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

package forms

object Validation {

  val postcodeRegex = """^[a-zA-Z]{1,2}[0-9][0-9a-zA-Z]?\s?[0-9][a-zA-Z]{2}$"""
  val nameRegex     = "^[A-Za-z0-9 ,.()/&'-]*$"
  val utrRegex      = "^[0-9]*$"
  val urnRegex      = "^(?i)[a-z]{2}(trust)[0-9]{8}$"

  val telephoneRegex   = """^\+[0-9 ]{1,18}$|^[0-9 ]{1,19}$"""
  val addressLineRegex = "^[A-Za-z0-9 ,.()/&'-]*$"
  val clientRefRegex   = "^[A-Za-z0-9 ,.()/&'-]*$"

  val emailRegex =
    """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$"""

}
