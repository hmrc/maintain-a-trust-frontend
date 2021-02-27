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

package mapping.settlors

import models.http.DisplayTrustSettlorCompany
import models.pages.IndividualOrBusiness
import models.pages.Tag.UpToDate
import models.{MetaData, UserAnswers}
import pages.QuestionPage
import pages.entitystatus.LivingSettlorStatus
import pages.settlors.living_settlor._

import scala.util.Try

class BusinessSettlorExtractor extends SettlorPlaybackExtractor[DisplayTrustSettlorCompany] {

  override def utrYesNoPage(index: Int): QuestionPage[Boolean] = SettlorUtrYesNoPage(index)
  override def utrPage(index: Int): QuestionPage[String] = SettlorUtrPage(index)

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustSettlorCompany,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(SettlorIndividualOrBusinessPage(index), IndividualOrBusiness.Business))
      .flatMap(_.set(SettlorBusinessNamePage(index), entity.name))
      .flatMap(answers => extractOrgIdentification(entity.identification, index, answers))
      .flatMap {
        _.set(
          SettlorMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
      .flatMap(_.set(SettlorCompanyTypePage(index), entity.companyType))
      .flatMap(_.set(SettlorCompanyTimePage(index), entity.companyTime))
      .flatMap(_.set(SettlorSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(LivingSettlorStatus(index), UpToDate))
  }
}
