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

package forms

import forms.helpers.WhitespaceHelper._
import forms.mappings.Mappings

import javax.inject.Inject
import models.InternationalAddress
import play.api.data.Forms._
import play.api.data.{Form, Forms}

class InternationalAddressFormProvider @Inject() extends Mappings {

  private val maximumLength = 35

  def apply(): Form[InternationalAddress] = Form(
    mapping(
      "line1" ->
        text("internationalAddress.error.line1.required")
          .verifying(
            firstError(
              isNotEmpty("line1", "internationalAddress.error.line1.required"),
              maxLength(maximumLength, "internationalAddress.error.line1.length"),
              regexp(Validation.addressLineRegex, "internationalAddress.error.line1.invalidCharacters")
            )),
      "line2" ->
        text("internationalAddress.error.line2.required")
          .verifying(
            firstError(
              isNotEmpty("line2", "internationalAddress.error.line2.required"),
              maxLength(maximumLength, "internationalAddress.error.line2.length"),
              regexp(Validation.addressLineRegex, "internationalAddress.error.line2.invalidCharacters")
            )),
      "line3" ->
        optional(text()
          .verifying(
            firstError(
              maxLength(maximumLength, "internationalAddress.error.line3.length"),
              regexp(Validation.addressLineRegex, "internationalAddress.error.line3.invalidCharacters")
            ))).transform(emptyToNone, identity[Option[String]]),
      "country" ->
        text("internationalAddress.error.country.required")
          .verifying(
            firstError(
              maxLength(maximumLength, "internationalAddress.error.country.length"),
              isNotEmpty("country", "internationalAddress.error.country.required")
            ))
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}
