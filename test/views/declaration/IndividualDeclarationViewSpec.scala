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

package views.declaration

import config.FrontendAppConfig
import forms.declaration.IndividualDeclarationFormProvider
import models.{IndividualDeclaration, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.declaration.IndividualDeclarationView

class IndividualDeclarationViewSpec extends QuestionViewBehaviours[IndividualDeclaration] {

  val messageKeyPrefix = "declaration"

  val form = new IndividualDeclarationFormProvider()()

  val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  "DeclarationView view for individual or organisation" must {

    val view = viewFor[IndividualDeclarationView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, UpdateMode)(fakeRequest, messages)

    if(appConfig.declarationEmailEnabled) {
      behave like normalPage(applyView(form), messageKeyPrefix, "paragraph1", "paragraph2")
    }

    behave like pageWithBackLink(applyView(form))

    if(appConfig.declarationEmailEnabled) {
      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        "firstName", "middleName", "lastName", "email"
      )
    } else {
      behave like pageWithTextFields(
        form,
        applyView,
        messageKeyPrefix,
        "firstName", "middleName", "lastName"
      )
    }

  }
}
