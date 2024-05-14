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

package views

import base.SpecBase
import models.UserAnswers
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.twirl.api.Html

import scala.reflect.ClassTag

trait ViewSpecBase extends SpecBase {

  def viewFor[A](data: Option[UserAnswers] = None)(implicit tag: ClassTag[A]): A = {
    val application = applicationBuilder(data).build()
    val view = application.injector.instanceOf[A]
    application.stop()
    view
  }

  def asDocument(html: Html): Document = Jsoup.parse(html.toString())

  def assertEqualsMessage(doc: Document, cssSelector: String, expectedMessageKey: String, args: Any*): Assertion =
    assertEqualsValue(doc, cssSelector, ViewUtils.breadcrumbTitle(messages(expectedMessageKey, args: _*)))

  def assertEqualsValue(doc: Document, cssSelector: String, expectedValue: String): Assertion = {
    val elements = doc.select(cssSelector)

    if (elements.isEmpty) throw new IllegalArgumentException(s"CSS Selector $cssSelector wasn't rendered.")

    //<p> HTML elements are rendered out with a carriage return on some pages, so discount for comparison
    assert(elements.first().html().replace("\n", "") == expectedValue)
  }

  def assertPageTitleEqualsMessage(doc: Document, expectedMessageKey: String, args: Any*): Assertion = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    val expectedHeading = messages(expectedMessageKey, args:_*).replaceAll("&nbsp;", " ")

    headers.first.text.replaceAll("\u00a0", " ") mustBe expectedHeading
  }

  def assertPageTitleWithCaptionEqualsMessages(
                                                doc: Document,
                                                expectedCaptionMessageKey: String,
                                                captionParam: String,
                                                expectedMessageKey: String
                                              ): Assertion = {
    val headers = doc.getElementsByTag("h1")
    headers.size mustBe 1
    val expectedSubheading =  messages(expectedCaptionMessageKey, captionParam).replaceAll("&nbsp;", " ")
    val expectedHeading =  messages(expectedMessageKey).replaceAll("&nbsp;", " ")

    val expected = s"$expectedSubheading $expectedHeading"

    headers.first.text.replaceAll("\u00a0", " ") mustBe expected
  }

  def assertContainsText(doc: Document, text: String): Assertion = assert(doc.toString.contains(text), "\n\ntext " + text + " was not rendered on the page.\n")

  def assertContainsMessages(doc: Document, expectedMessageKeys: String*): Unit = {
    for (key <- expectedMessageKeys) assertContainsText(doc, messages(key))
  }

  def assertAttributeValueForElement(element: Element, attribute: String, attributeValue: String): Assertion = {
    assert(element.attr(attribute) == attributeValue)
  }


  def assertRenderedById(doc: Document, id: String): Assertion = {
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")
  }

  def assertNotRenderedById(doc: Document, id: String): Assertion = {
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")
  }

  def assertContainsTextForId(doc: Document, id: String, expectedText: String): Assertion = {
    assert(doc.getElementById(id).text() == expectedText, s"\n\nElement $id does not have text $expectedText")
  }

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion = {
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")
  }

  def assertNotRenderedByCssSelector(doc: Document, cssSelector: String): Assertion = {
    assert(doc.select(cssSelector).isEmpty, "\n\nElement " + cssSelector + " was rendered on the page.\n")
  }

  def assertRenderedByClass(doc: Document, className: String): Assertion = {
    assert(!doc.getElementsByClass(className).isEmpty, "Element " + className + " was not rendered on the page.")
  }

  def assertNotRenderedByClass(doc: Document, className: String): Assertion = {
    assert(doc.getElementsByClass(className).isEmpty, "Element " + className + " was rendered on the page.")
  }

  def assertContainsLabel(doc: Document, forElement: String, expectedText: String, expectedHintText: Option[String] = None): Any = {
    val labels = doc.getElementsByAttributeValue("for", forElement)
    assert(labels.size == 1, s"\n\nLabel for $forElement was not rendered on the page.")
    val label = labels.first
    assert(label.text().contains(expectedText), s"\n\nLabel for $forElement was not $expectedText")

    if (expectedHintText.isDefined) {
      assert(doc.getElementById(s"$forElement-hint").getElementsByClass("govuk-hint").first.text == expectedHintText.get,
        s"\n\nLabel for $forElement did not contain hint text $expectedHintText")
    }
  }

  def assertContainsClass(doc: Document, className: String): Any = {
    assert(doc.getElementsByClass(className).size() > 0, s"\n\nPage did not contain element with class $className")
  }

  def assertElementHasClass(doc: Document, id: String, expectedClass: String): Assertion = {
    assert(doc.getElementById(id).hasClass(expectedClass), s"\n\nElement $id does not have class $expectedClass")
  }

  def assertContainsRadioButton(doc: Document, id: String, name: String, value: String, isChecked: Boolean): Assertion = {
    assertRenderedById(doc, id)
    val radio = doc.getElementById(id)
    assert(radio.attr("name") == name, s"\n\nElement $id does not have name $name")
    assert(radio.attr("value") == value, s"\n\nElement $id does not have value $value")
    if (isChecked) {
      assert(radio.hasAttr("checked"), s"\n\nElement $id is not checked")
    } else {
      assert(!radio.hasAttr("checked"), s"\n\nElement $id is checked")
    }
  }

  def assertRadioButtonContainsHint(doc: Document, id: String, text: String): Assertion = {
    assertRenderedById(doc, id)
    val hint = doc.getElementById(id)
    assert(hint.text() == text, s"\n\nElement $id does not have text $text")
  }

  def assertContainsLink(doc: Document, linkUrl: String, linkText: String): Assertion = {
    val links = doc.getElementsByTag("a")
    assert(
      links.stream().anyMatch(x => x.attr("href") == linkUrl && x.text() == linkText),
      s"\n\nLink with href $linkUrl and text $linkText was not rendered on the page.\n"
    )
  }

}
