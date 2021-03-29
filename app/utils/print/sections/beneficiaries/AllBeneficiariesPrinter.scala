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

package utils.print.sections.beneficiaries

import models.UserAnswers
import play.api.i18n.Messages
import viewmodels.AnswerSection

import javax.inject.Inject

class AllBeneficiariesPrinter @Inject()(classOfBeneficiaryPrinter: ClassOfBeneficiaryPrinter,
                                        otherBeneficiaryPrinter: OtherBeneficiaryPrinter,
                                        trustBeneficiaryPrinter: TrustBeneficiaryPrinter,
                                        companyBeneficiaryPrinter: CompanyBeneficiaryPrinter,
                                        charityBeneficiaryPrinter: CharityBeneficiaryPrinter,
                                        individualBeneficiaryPrinter: IndividualBeneficiaryPrinter,
                                        largeBeneficiaryPrinter: LargeBeneficiaryPrinter) {

  def allBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val beneficiaries = Seq(
      individualBeneficiaries(userAnswers),
      classOfBeneficiaries(userAnswers),
      charityBeneficiaries(userAnswers),
      companyBeneficiaries(userAnswers),
      largeBeneficiaries(userAnswers),
      trustBeneficiaries(userAnswers),
      otherBeneficiaries(userAnswers)
    ).flatten

    if (beneficiaries.nonEmpty) {
      Seq(
        Seq(AnswerSection(sectionKey = Some("answerPage.section.beneficiaries.heading"))),
        beneficiaries
      ).flatten
    } else {
      Nil
    }
  }

  private def classOfBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.ClassOfBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield classOfBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def otherBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.OtherBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield otherBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def trustBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.TrustBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield trustBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def companyBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CompanyBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield companyBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def charityBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CharityBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield charityBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def individualBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.IndividualBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield individualBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

  private def largeBeneficiaries(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {
    val size = userAnswers.get(_root_.sections.beneficiaries.LargeBeneficiaries).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield largeBeneficiaryPrinter.print(index, userAnswers)).flatten
    }
  }

}
