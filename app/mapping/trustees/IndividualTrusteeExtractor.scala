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

package mapping.trustees

import models.UserAnswers
import models.http.DisplayTrustTrusteeIndividualType
import models.pages.IndividualOrBusiness
import models.pages.Tag.UpToDate
import pages.entitystatus.TrusteeStatus
import pages.trustees._

import scala.util.Try

class IndividualTrusteeExtractor extends TrusteePlaybackExtractor[DisplayTrustTrusteeIndividualType] {

  override def updateUserAnswers(answers: Try[UserAnswers],
                                 entity: DisplayTrustTrusteeIndividualType,
                                 index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(IsThisLeadTrusteePage(index), false))
      .flatMap(_.set(TrusteeIndividualOrBusinessPage(index), IndividualOrBusiness.Individual))
      .flatMap(_.set(TrusteeNamePage(index), entity.name))
      .flatMap(answers => extractDateOfBirth(entity.dateOfBirth, index, answers))
      .flatMap(_.set(TrusteeTelephoneNumberPage(index), entity.phoneNumber))
      .flatMap(answers => extractIndIdentification(entity.identification, index, answers))
      .flatMap(answers => extractMetaData(entity, index, answers))
      .flatMap(_.set(TrusteeSafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap(_.set(TrusteeStatus(index), UpToDate))
  }
}
