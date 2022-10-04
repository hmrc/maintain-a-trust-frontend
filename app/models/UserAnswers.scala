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

package models

import _root_.pages.WhatIsNextPage
import _root_.pages.trustees._
import models.pages.WhatIsNext.{NeedsToPayTax, NoLongerTaxable}
import play.api.Logging
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable}
import sections.Trustees
import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

final case class UserAnswers(internalId: String,
                             identifier: String,
                             sessionId: String,
                             newId: String,
                             data: JsObject = Json.obj(),
                             isUnderlyingData5mld: Boolean = false,
                             isUnderlyingDataTaxable: Boolean = true,
                             updatedAt: LocalDateTime = LocalDateTime.now) extends Logging {

  def identifierType: IdentifierType = IdentifierType(identifier)

  def trustTaxability: TrustTaxability = (this.get(WhatIsNextPage), isUnderlyingDataTaxable) match {
    case (Some(NeedsToPayTax), _) => MigratingFromNonTaxableToTaxable
    case (Some(NoLongerTaxable), _) => MigratingFromTaxableToNonTaxable
    case (_, true) => Taxable
    case (_, false) => NonTaxable
  }

  def isTrustTaxable: Boolean = trustTaxability.isTrustTaxable
  def isTrustMigratingFromNonTaxableToTaxable: Boolean = trustTaxability.isTrustMigratingFromNonTaxableToTaxable
  def isTrustTaxableOrMigratingToTaxable: Boolean = isTrustTaxable || isTrustMigratingFromNonTaxableToTaxable

  def trustMldStatus: TrustMldStatus = (isUnderlyingData5mld, isUnderlyingDataTaxable) match {
    case (false, _) => Underlying4mldTrustIn5mldMode
    case (true, true) => Underlying5mldTaxableTrustIn5mldMode
    case (true, false) => Underlying5mldNonTaxableTrustIn5mldMode
  }

  def is5mldTrustIn5mldMode: Boolean = trustMldStatus.is5mldTrustIn5mldMode

  def leadTrusteeName(implicit messages: Messages): String = this.get(Trustees).getOrElse(JsArray())
    .value
    .zipWithIndex
    .find(x => x._1.transform((__ \ IsThisLeadTrusteePage(x._2)).json.pick[JsBoolean]).contains(JsBoolean(true)))
    .map { x =>
      for {
        ind <- x._1.transform((__ \ TrusteeNamePage(x._2)).json.pick)
        org <- x._1.transform((__ \ TrusteeOrgNamePage(x._2)).json.pick)
      } yield {
        ind.asOpt[FullName].map(_.toString) orElse org.asOpt[String]
      }
    } match {
    case Some(JsSuccess(Some(name), _)) => name
    case _ => messages("leadTrustee.default")
  }

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] = {
    getAtPath(page.path)
  }

  def getAtPath[A](path: JsPath)(implicit rds: Reads[A]): Option[A] = {
    Reads.at(path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(_) => None
    }
  }

  def getWithDefault[A](page: Gettable[A], default: A)(implicit rds: Reads[A]): Option[A] = {
    get(page).orElse(Some(default))
  }

  def set[A](page: Settable[A], value: Option[A])(implicit writes: Writes[A], reads: Reads[A]): Try[UserAnswers] = {
    value match {
      case Some(v) => setValue(page, v)
      case None =>
        val updatedAnswers = this
        page.cleanup(value, updatedAnswers)
    }
  }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A], reads: Reads[A]): Try[UserAnswers] = setValue(page, value)

  private def setValue[A](page: Settable[A], value: A)(implicit writes: Writes[A], reads: Reads[A]): Try[UserAnswers] = {
    val hasValueChanged: Boolean = !getAtPath(page.path).contains(value)

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        val errorPaths = errors.collectFirst { case (path, e) => s"$path $e" }
        logger.warn(s"[UserAnswers][setValue] Unable to set path ${page.path} due to errors $errorPaths")
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        if (hasValueChanged) page.cleanup(Some(value), updatedAnswers) else Success(updatedAnswers)
    }
  }

  def remove[A](query: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(query.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        query.cleanup(None, updatedAnswers)
    }
  }

  def deleteAtPath(path: JsPath): Try[UserAnswers] = {
    data.removeObject(path).map(obj => copy(data = obj)).fold(
      _ => Success(this),
      result => Success(result)
    )
  }

  def clearData: UserAnswers = this.copy(data = Json.obj())

}

object UserAnswers {

  def startNewSession(internalId: String,
                      identifier: String,
                      sessionId: String,
                      isUnderlyingData5mld: Boolean,
                      isUnderlyingDataTaxable: Boolean): UserAnswers =
    UserAnswers(
      internalId = internalId,
      identifier = identifier,
      sessionId = sessionId,
      newId = s"$internalId-$identifier-$sessionId",
      isUnderlyingData5mld = isUnderlyingData5mld,
      isUnderlyingDataTaxable = isUnderlyingDataTaxable
    )

  implicit lazy val reads: Reads[UserAnswers] = (
    (__ \ "internalId").read[String] and
      (__ \ "identifier").read[String] and
      (__ \ "sessionId").read[String] and
      (__ \ "newId").read[String] and
      (__ \ "data").read[JsObject] and
      (__ \ "isUnderlyingData5mld").readWithDefault[Boolean](false) and
      (__ \ "isUnderlyingDataTaxable").readWithDefault[Boolean](true) and
      (__ \ "updatedAt").read(MongoDateTimeFormats.localDateTimeRead)
    )(UserAnswers.apply _)

  implicit lazy val writes: Writes[UserAnswers] = (
    (__ \ "internalId").write[String] and
      (__ \ "identifier").write[String] and
      (__ \ "sessionId").write[String] and
      (__ \ "newId").write[String] and
      (__ \ "data").write[JsObject] and
      (__ \ "isUnderlyingData5mld").write[Boolean] and
      (__ \ "isUnderlyingDataTaxable").write[Boolean] and
      (__ \ "updatedAt").write(MongoDateTimeFormats.localDateTimeWrite)
    )(unlift(UserAnswers.unapply))
}