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

package forms.declaration

import forms.Validation
import forms.helpers.WhitespaceHelper._
import models.IndividualDeclaration
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

class IndividualDeclarationFormProvider extends DeclarationFormProvider {

  def apply(): Form[IndividualDeclaration] =
    Form(
      mapping(
        "" -> fullName,
        "email" -> optional(text()
          .transform(trimWhitespace, identity[String])
          .verifying(
            firstError(
              regexp(Validation.emailRegex, "declaration.error.email.invalid"))
          )).transform(emptyToNone, identity[Option[String]])
      )(IndividualDeclaration.apply)(IndividualDeclaration.unapply)
    )
}
