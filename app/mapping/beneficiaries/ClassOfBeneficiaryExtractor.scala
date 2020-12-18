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

package mapping.beneficiaries

import com.google.inject.Inject
import mapping.PlaybackExtractionErrors.{FailedToExtractData, PlaybackExtractionError}
import mapping.PlaybackExtractor
import models.{MetaData, UserAnswers}
import models.http.DisplayTrustUnidentifiedType
import pages.beneficiaries.classOfBeneficiary._
import play.api.Logging

import scala.util.{Failure, Success, Try}

class ClassOfBeneficiaryExtractor @Inject() extends PlaybackExtractor[Option[List[DisplayTrustUnidentifiedType]]] with Logging {

  override def extract(answers: UserAnswers, data: Option[List[DisplayTrustUnidentifiedType]]): Either[PlaybackExtractionError, UserAnswers] =
    {
      data match {
        case None => Left(FailedToExtractData("No Class Of Beneficiary"))
        case Some(classOfBeneficiaries) =>

          val updated = classOfBeneficiaries.zipWithIndex.foldLeft[Try[UserAnswers]](Success(answers)){
            case (answers, (classOfBeneficiary, index)) =>

            answers
              .flatMap(_.set(ClassOfBeneficiaryDescriptionPage(index), classOfBeneficiary.description))
              .flatMap(answers => extractShareOfIncome(classOfBeneficiary, index, answers))
              .flatMap {
                _.set(
                  ClassOfBeneficiaryMetaData(index),
                  MetaData(
                    lineNo = classOfBeneficiary.lineNo.getOrElse(""),
                    bpMatchStatus = classOfBeneficiary.bpMatchStatus.fold(Some("98"))(x => Some(x)),
                    entityStart = classOfBeneficiary.entityStart
                  )
                )
              }
          }

          updated match {
            case Success(a) =>
              Right(a)
            case Failure(exception) =>
              logger.warn(s"[UTR: ${answers.utr}] failed to extract data due to ${exception.getMessage}")
              Left(FailedToExtractData(DisplayTrustUnidentifiedType.toString))
          }
      }
    }

  private def extractShareOfIncome(classOfBeneficiary: DisplayTrustUnidentifiedType, index: Int, answers: UserAnswers) = {
    classOfBeneficiary.beneficiaryShareOfIncome match {
      case Some(income) =>
        answers.set(ClassOfBeneficiaryDiscretionYesNoPage(index), false)
          .flatMap(_.set(ClassOfBeneficiaryShareOfIncomePage(index), income))
      case None =>
        // Assumption that user answered yes as the share of income is not provided
        answers.set(ClassOfBeneficiaryDiscretionYesNoPage(index), true)
    }
  }
}