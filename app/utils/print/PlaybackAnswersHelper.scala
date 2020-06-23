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

package utils.print

import models.UserAnswers
import models.pages.IndividualOrBusiness
import models.pages.IndividualOrBusiness.{Business, Individual}
import pages.protectors.ProtectorIndividualOrBusinessPage
import pages.settlors.living_settlor.SettlorIndividualOrBusinessPage
import pages.trustees._
import play.api.i18n.Messages
import utils.countryoptions.CountryOptions
import utils.print.sections.OtherIndividual
import utils.print.sections.beneficiaries._
import utils.print.sections.protectors.{BusinessProtector, IndividualProtector}
import utils.print.sections.settlors._
import utils.print.sections.trustees._
import utils.print.sections.trustees.lead_trustee._
import viewmodels.AnswerSection

class PlaybackAnswersHelper(countryOptions: CountryOptions, userAnswers: UserAnswers)
                           (implicit messages: Messages) {

  def trustee(index: Int): Seq[AnswerSection] = {

    userAnswers.get(IsThisLeadTrusteePage(index)) flatMap { isLeadTrustee =>
      userAnswers.get(TrusteeIndividualOrBusinessPage(index)) flatMap { individualOrBusiness =>
        if (isLeadTrustee) {
          individualOrBusiness match {
            case IndividualOrBusiness.Individual => LeadTrusteeIndividual(index, userAnswers, countryOptions)
            case IndividualOrBusiness.Business => LeadTrusteeBusiness(index, userAnswers, countryOptions)
          }
        } else {
          individualOrBusiness match {
            case IndividualOrBusiness.Individual => TrusteeIndividual(index, userAnswers, countryOptions)
            case IndividualOrBusiness.Business => TrusteeOrganisation(index, userAnswers, countryOptions)
          }
        }
      }
    }

  }.getOrElse(Nil)

  def allTrustees : Seq[AnswerSection] = {

    val size = userAnswers.get(_root_.sections.Trustees).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield trustee(index)).flatten
    }
  }

  def settlors: Seq[AnswerSection] = deceasedSettlors ++ livingSettlors

  def deceasedSettlors: Seq[AnswerSection] = {

    DeceasedSettlor(userAnswers, countryOptions) match {
      case Nil => Nil
      case x => AnswerSection(sectionKey = Some("answerPage.section.deceasedSettlor.heading")) +: x
    }
  }

  def livingSettlors : Seq[AnswerSection] = {
    val size = userAnswers.get(_root_.sections.settlors.LivingSettlors).map(_.value.size).getOrElse(0)
    size match {
      case 0 => Nil
      case _ =>
        AnswerSection(sectionKey = Some("answerPage.section.settlors.heading")) +:
        (for (index <- 0 to size) yield livingSettlor(index)).flatten
    }
  }

  def livingSettlor(index: Int): Seq[AnswerSection] = {
    userAnswers.get(SettlorIndividualOrBusinessPage(index)).flatMap { individualOrBusiness =>
      individualOrBusiness match {
        case IndividualOrBusiness.Individual => SettlorIndividual(index, userAnswers, countryOptions)
        case IndividualOrBusiness.Business => SettlorCompany(index, userAnswers, countryOptions)
      }
    }.getOrElse(Nil)
  }


  def beneficiaries : Seq[AnswerSection] = {

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

  private def classOfBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.ClassOfBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield ClassOfBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def otherBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.OtherBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield OtherBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def trustBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.TrustBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield TrustBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def companyBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CompanyBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield CompanyBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def charityBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.CharityBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield CharityBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def individualBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.beneficiaries.IndividualBeneficiaries)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield IndividualBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  private def largeBeneficiaries : Seq[AnswerSection] = {
    val size = userAnswers.get(_root_.sections.beneficiaries.LargeBeneficiaries).map(_.value.size).getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        (for (index <- 0 to size) yield LargeBeneficiary(index, userAnswers, countryOptions)).flatten
    }
  }

  def protectors : Seq[AnswerSection] = {

    val size = userAnswers
      .get(_root_.sections.Protectors)
      .map(_.value.size)
      .getOrElse(0)

    val protectors = size match {
      case 0 => Nil
      case _ =>

        (for (index <- 0 to size) yield {
          userAnswers.get(ProtectorIndividualOrBusinessPage(index)).map {
            case Individual =>
              IndividualProtector(index, userAnswers, countryOptions)
            case Business =>
              BusinessProtector(index, userAnswers, countryOptions)
          }.getOrElse(Nil)
        }).flatten
    }

    if (protectors.nonEmpty) {
      Seq(
        Seq(AnswerSection(sectionKey = Some(messages("answerPage.section.protectors.heading")))),
        protectors
      ).flatten
    } else {
      Nil
    }
  }

  def otherIndividual : Seq[AnswerSection] = {
    val size = userAnswers
      .get(_root_.sections.natural.Individual)
      .map(_.value.size)
      .getOrElse(0)

    size match {
      case 0 => Nil
      case _ =>
        Seq(AnswerSection(sectionKey = Some(messages("answerPage.section.otherIndividuals.heading")))) ++
          (for (index <- 0 to size) yield OtherIndividual(index, userAnswers, countryOptions)).flatten
    }
  }

}
