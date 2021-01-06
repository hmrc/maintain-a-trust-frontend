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

package forms.declaration

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import models.IndividualDeclaration
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class IndividualDeclarationFormProviderSpec extends StringFieldBehaviours {

  private val prefix: String = "declaration.error"

  private val form: Form[IndividualDeclaration] = new IndividualDeclarationFormProvider()()

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = s"$prefix.$fieldName.required"
    val lengthKey = s"$prefix.$fieldName.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(Validation.nameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )
  }

  ".middleName" must {

    val fieldName = "middleName"
    val lengthKey = s"$prefix.$fieldName.length"
    val maxLength = 35

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like optionalField(
      form,
      fieldName,
      validDataGenerator = RegexpGen.from(Validation.nameRegex)
    )
  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = s"$prefix.$fieldName.required"
    val lengthKey = s"$prefix.$fieldName.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(Validation.nameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )
  }

  "email" must {

    val fieldName = "email"

    behave like optionalField(
      form,
      fieldName,
      validDataGenerator = RegexpGen.from(Validation.emailRegex)
    )
  }

}
