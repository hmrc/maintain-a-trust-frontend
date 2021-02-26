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

package mapping.beneficiaries

import mapping.PlaybackExtractionErrors.InvalidExtractorState
import mapping.PlaybackImplicits._
import models.HowManyBeneficiaries.{Over1, Over1001, Over101, Over201, Over501}
import models.http.{DisplayTrustIdentificationOrgType, DisplayTrustLargeType}
import models.{Address, Description, InternationalAddress, MetaData, UKAddress, UserAnswers}
import pages.QuestionPage
import pages.beneficiaries.large._

import scala.util.{Failure, Try}

class LargeBeneficiaryExtractor extends BeneficiaryPlaybackExtractor[DisplayTrustLargeType] {

  override def shareOfIncomeYesNoPage(index: Int): QuestionPage[Boolean] = LargeBeneficiaryDiscretionYesNoPage(index)
  override def shareOfIncomePage(index: Int): QuestionPage[String] = LargeBeneficiaryShareOfIncomePage(index)

  override def updateUserAnswers(answers: Try[UserAnswers], entity: DisplayTrustLargeType, index: Int): Try[UserAnswers] = {
    answers
      .flatMap(_.set(LargeBeneficiaryNamePage(index), entity.organisationName))
      .flatMap(answers => extractShareOfIncome(entity.beneficiaryShareOfIncome, index, answers))
      .flatMap(answers => extractIdentification(entity.identification, index, answers))
      .flatMap(
        _.set(
          LargeBeneficiaryDescriptionPage(index),
          Description(
            entity.description,
            entity.description1,
            entity.description2,
            entity.description3,
            entity.description4
          )
        )
      )
      .flatMap(answers => extractNumberOfBeneficiaries(entity.numberOfBeneficiary, index, answers))
      .flatMap(_.set(LargeBeneficiarySafeIdPage(index), entity.identification.flatMap(_.safeId)))
      .flatMap {
        _.set(
          LargeBeneficiaryMetaData(index),
          MetaData(
            lineNo = entity.lineNo.getOrElse(""),
            bpMatchStatus = entity.bpMatchStatus,
            entityStart = entity.entityStart
          )
        )
      }
  }

  private def extractIdentification(identification: Option[DisplayTrustIdentificationOrgType], index: Int, answers: UserAnswers) = {
    identification map {
      case DisplayTrustIdentificationOrgType(_, Some(utr), None) =>
        answers.set(LargeBeneficiaryUtrPage(index), utr)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), false))

      case DisplayTrustIdentificationOrgType(_, None, Some(address)) =>
        extractAddress(address.convert, index, answers)

      case _ =>
        logger.error(s"[UTR/URN: ${answers.identifier}] only both utr and address parsed")
        Failure(InvalidExtractorState)

    } getOrElse {
      answers.set(LargeBeneficiaryAddressYesNoPage(index), false)
    }
  }

  private def extractNumberOfBeneficiaries(numberOfBeneficiary: String, index: Int, answers: UserAnswers): Try[UserAnswers] = {
    numberOfBeneficiary.toInt match {
      case x if 0 to 100 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over1)
      case x if 101 to 200 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over101)
      case x if 201 to 500 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over201)
      case x if 501 to 999 contains x => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over501)
      case _ => answers.set(LargeBeneficiaryNumberOfBeneficiariesPage(index), Over1001)
    }
  }

  private def extractAddress(address: Address, index: Int, answers: UserAnswers) = {
    address match {
      case uk: UKAddress =>
        answers.set(LargeBeneficiaryAddressPage(index), uk)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(LargeBeneficiaryAddressUKYesNoPage(index), true))
      case nonUk: InternationalAddress =>
        answers.set(LargeBeneficiaryAddressPage(index), nonUk)
          .flatMap(_.set(LargeBeneficiaryAddressYesNoPage(index), true))
          .flatMap(_.set(LargeBeneficiaryAddressUKYesNoPage(index), false))
    }
  }
}
