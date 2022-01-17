/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.print

import org.scalatest.matchers.{MatchResult, Matcher}
import play.twirl.api.Html
import viewmodels.AnswerSection

trait AnswerSectionMatchers {
  class ContainsHeadingSection(expectedHeadingKey: String) extends Matcher[Seq[AnswerSection]] {
    override def apply(left: Seq[AnswerSection]): MatchResult =
      MatchResult(
        left.exists(section => section.sectionKey.contains(expectedHeadingKey)),
        s"""sections did not contains a section with section key "${expectedHeadingKey}"""",
        s"""sections contained a section with section key "${expectedHeadingKey}""""
      )
  }


  class ContainsSectionWithHeadingAndValues(expectedHeading: String, expectedValues: Seq[(String, Html)]) extends Matcher[Seq[AnswerSection]] {
    private def sectionContainsValue(answerSection: AnswerSection, value: (String, Html)) = {
      answerSection.rows.exists(r => r.label == value._1 && r.answer == value._2)
    }

    private def sectionContainsValues(section: AnswerSection, values: Seq[(String, Html)]): Boolean = {
      values.forall(sectionContainsValue(section, _))
    }

    override def apply(left: Seq[AnswerSection]): MatchResult = {
      MatchResult(
        left.exists(section =>
          section.headingKey.contains(expectedHeading) &&
          sectionContainsValues(section, expectedValues)),
        s"${left} did not contain a section with the heading ${expectedHeading} which contains all values from ${expectedValues}",
        s"A section with the expected heading was found containing all of the expected values"
      )
    }
  }

  def containHeadingSection(expectedHeadingKey: String) = new ContainsHeadingSection(expectedHeadingKey)

  def containSectionWithHeadingAndValues(expectedHeading: String, expectedValues: (String, Html)*) =
    new ContainsSectionWithHeadingAndValues(expectedHeading, expectedValues)
}
