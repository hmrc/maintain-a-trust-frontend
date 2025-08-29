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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class URNFormProviderSpec extends StringFieldBehaviours {

  private val requiredLength = 15

  private val requiredKey = "urn.error.required"
  private val lengthKey = "urn.error.length"

  private val urnRegex = Validation.urnRegex

  private val form: Form[String] = new URNFormProvider()()

  "URNFormProvider" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = RegexpGen.from(urnRegex),
      fieldLength = Some(requiredLength)
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = requiredLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like fieldWithMinLength(
      form = form,
      fieldName = fieldName,
      minLength = requiredLength,
      lengthError = FormError(fieldName, lengthKey)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
