/*
 * Copyright 2023 HM Revenue & Customs
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

package models.errors

import play.api.mvc.Result

sealed trait TrustErrors

final case class ServerError() extends TrustErrors
final case class TrustErrorWithRedirect(call: Result) extends TrustErrors
final case class FormValidationError(call: Result) extends TrustErrors
final case class DeclarationError() extends TrustErrors
case object NoData extends TrustErrors
final case class WrongUserType() extends TrustErrors


sealed trait PlaybackExtractionErrors extends TrustErrors

case class FailedToExtractData(reason: String) extends PlaybackExtractionErrors
case object FailedToCombineAnswers extends PlaybackExtractionErrors
case object InvalidExtractorState extends PlaybackExtractionErrors


sealed trait DatabaseErrors extends TrustErrors

case object MongoError extends DatabaseErrors