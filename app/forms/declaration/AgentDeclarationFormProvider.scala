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

package forms.declaration

import com.google.inject.Inject
import forms.Validation
import models.AgentDeclaration
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

class AgentDeclarationFormProvider @Inject() extends DeclarationFormProvider {

  def apply(): Form[AgentDeclaration] =
    Form(
      mapping(
        "" -> fullName,
        "telephoneNumber" -> text("declaration.error.telephoneNumber.required").verifying(
          firstError(
            isNotEmpty("crn", s"declaration.error.telephoneNumber.required"),
            regexp(Validation.telephoneRegex, s"declaration.error.telephoneNumber.invalid")
          )
        ),
        "crn" -> text("declaration.error.crn.required").verifying(
          firstError(
            maxLength(56, s"declaration.error.crn.length"),
            isNotEmpty("crn", s"declaration.error.crn.required"),
            regexp(Validation.clientRefRegex, s"declaration.error.crn.invalid")
          )
        ),
        "email" -> optional(text().verifying(
          firstError(
            maxLength(35, s"declaration.error.email.length"),
            regexp(Validation.emailRegex, s"declaration.error.email.invalid"))
        ))
      )(AgentDeclaration.apply)(AgentDeclaration.unapply)
    )
}
