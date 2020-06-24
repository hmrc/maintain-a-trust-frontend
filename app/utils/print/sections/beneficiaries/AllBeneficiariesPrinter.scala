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

package utils.print.sections.beneficiaries

import models.UserAnswers
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import viewmodels.AnswerSection

class AllBeneficiariesPrinter(userAnswers: UserAnswers, countryOptions: CountryOptions)(implicit messages: Messages) {

  def allBeneficiaries : Seq[AnswerSection] = {
    val beneficiaries = Seq(
      individualBeneficiaries,
      classOfBeneficiaries,
      charityBeneficiaries,
      companyBeneficiaries,
      largeBeneficiaries,
      trustBeneficiaries,
      otherBeneficiaries
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

  private lazy val classOfBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.ClassOfBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield ClassOfBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val otherBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.OtherBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield OtherBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val trustBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.TrustBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield TrustBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val companyBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CompanyBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield CompanyBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val charityBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CharityBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield CharityBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val individualBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.IndividualBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield IndividualBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

  private lazy val largeBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers.get(_root_.sections.beneficiaries.LargeBeneficiaries).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield LargeBeneficiaryPrinter.print(index, userAnswers, countryOptions)).flatten
    }
  }

}
