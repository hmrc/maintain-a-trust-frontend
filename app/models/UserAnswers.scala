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

package models

import _root_.pages.trustdetails.ExpressTrustYesNoPage
import forms.Validation
import play.api.Logging
import play.api.libs.json._
import queries.{Gettable, Settable}

import java.time.LocalDateTime
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              internalId: String,
                              identifier: String,
                              data: JsObject = Json.obj(),
                              is5mldEnabled: Boolean = false,
                              isTrustTaxable: Boolean = true,
                              updatedAt: LocalDateTime = LocalDateTime.now
                            ) extends Logging {

  def identifierType: IdentifierType = if (identifier.matches(Validation.utrRegex)) UTR else URN

  def trustMldStatus: TrustMldStatus = (is5mldEnabled, isUnderlyingTrust5mld, isTrustTaxable) match {
    case (false, _, _) => Underlying4mldTrustIn4mldMode
    case (true, false, _) => Underlying4mldTrustIn5mldMode
    case (true, true, true) => Underlying5mldTaxableTrustIn5mldMode
    case (true, true, false) => Underlying5mldNonTaxableTrustIn5mldMode
  }

  def isUnderlyingTrust5mld: Boolean = this.get(ExpressTrustYesNoPage).isDefined // TODO - amend usages and make private

  def isTrust5mldTaxable: Boolean = isUnderlyingTrust5mld && isTrustTaxable // TODO - amend usages and delete

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] = {
    Reads.at(page.path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(_) => None
    }
  }

  def getWithDefault[A](page: Gettable[A], default: A)(implicit rds: Reads[A]): Option[A] = {
    get(page) match {
      case None => Some(default)
      case x => x
    }
  }

  def set[A](page: Settable[A], value: Option[A])(implicit writes: Writes[A]): Try[UserAnswers] = {
    value match {
      case Some(v) => setValue(page, v)
      case None =>
        val updatedAnswers = this
        page.cleanup(value, updatedAnswers)
    }
  }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = setValue(page, value)

  private def setValue[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        val errorPaths = errors.collectFirst { case (path, e) => s"$path $e" }
        logger.warn(s"Unable to set path ${page.path} due to errors $errorPaths")
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
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
}

object UserAnswers {

  def startNewSession(internalId: String, identifier: String, is5mldEnabled: Boolean = false) : UserAnswers =
    UserAnswers(internalId = internalId, identifier = identifier, is5mldEnabled = is5mldEnabled)

  implicit lazy val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "internalId").read[String] and
        (__ \ "identifier").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "is5mldEnabled").readWithDefault[Boolean](false) and
        (__ \ "isTrustTaxable").readWithDefault[Boolean](true) and
        (__ \ "updatedAt").read(MongoDateTimeFormats.localDateTimeRead)
      ) (UserAnswers.apply _)
  }

  implicit lazy val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "internalId").write[String] and
        (__ \ "identifier").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "is5mldEnabled").write[Boolean] and
        (__ \ "isTrustTaxable").write[Boolean] and
        (__ \ "updatedAt").write(MongoDateTimeFormats.localDateTimeWrite)
      ) (unlift(UserAnswers.unapply))
  }
}
