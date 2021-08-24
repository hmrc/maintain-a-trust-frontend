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

package mapping

import models.http.PassportType
import models.{Address, MetaData}
import pages.{EmptyPage, QuestionPage}

import java.time.LocalDate

trait Pages {

  def countryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfResidenceYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfResidencePage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def countryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukCountryOfNationalityYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def countryOfNationalityPage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def mentalCapacityYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]

  def addressYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukAddressYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def addressPage(index: Int): QuestionPage[Address] = new EmptyPage[Address]

  def utrYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def utrPage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def ninoYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ninoPage(index: Int): QuestionPage[String] = new EmptyPage[String]

  def passportOrIdCardYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def passportOrIdCardPage(index: Int): QuestionPage[PassportType] = new EmptyPage[PassportType]

  def dateOfBirthYesNoPage(index: Int): QuestionPage[Boolean] = new EmptyPage[Boolean]
  def dateOfBirthPage(index: Int): QuestionPage[LocalDate] = new EmptyPage[LocalDate]

  def safeIdPage(index: Int): QuestionPage[String] = new EmptyPage[String]
  def metaDataPage(index: Int): QuestionPage[MetaData] = new EmptyPage[MetaData]

}
