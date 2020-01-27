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

package views

import forms.declaration.IndividualDeclarationFormProvider
import models.declaration.DeclarationIndividual
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.AffinityGroup
import views.behaviours.QuestionViewBehaviours
import views.html.DeclarationView

class DeclarationIndividualViewSpec extends QuestionViewBehaviours[DeclarationIndividual] {

  val messageKeyPrefix = "declaration"

  val form = new IndividualDeclarationFormProvider()()

  "DeclarationView view for organisation or agent " ignore {

    val view = viewFor[DeclarationView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, AffinityGroup.Agent, Call("POST", ""))(fakeRequest, messages)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithTextFields(
      form,
      applyView,
      messageKeyPrefix,
      "firstName", "middleName", "lastName"
    )
  }

  "rendered for an Organisation" must {

    "render declaration warning for organisation" in {
      val view = viewFor[DeclarationView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, AffinityGroup.Organisation, Call("POST", ""))(fakeRequest, messages)

      val doc = asDocument(applyView(form))
      assertContainsText(doc, "I confirm that the information given is true and complete to the best of my knowledge and belief. " +
        "I will ensure it is regularly kept up to date as required including changes of address. " +
        "If I discover that I have made an error or something has changed, " +
        "I will update this register.")
    }

  }

  "rendered for an Agent" must {

    "render declaration warning for Agent" in {
      val view = viewFor[DeclarationView](Some(emptyUserAnswers))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, AffinityGroup.Agent, Call("POST", ""))(fakeRequest, messages)

      val doc = asDocument(applyView(form))

      assertContainsText(doc, "I confirm that my client has taken all reasonable steps to obtain up " +
        "to date and accurate information for all of the entities given in this registration. " +
        "I understand that if my client knowingly provides false information and cannot demonstrate " +
        "that they have taken all reasonable steps, they could be subject to penalties.")
    }

  }
}
