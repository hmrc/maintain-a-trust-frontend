/*
 * Copyright 2024 HM Revenue & Customs
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

sealed trait TrustMldStatus {
  def is5mldTrustIn5mldMode: Boolean =
    this == Underlying5mldTaxableTrustIn5mldMode || this == Underlying5mldNonTaxableTrustIn5mldMode
}

case object Underlying4mldTrustIn5mldMode extends TrustMldStatus
case object Underlying5mldTaxableTrustIn5mldMode extends TrustMldStatus
case object Underlying5mldNonTaxableTrustIn5mldMode extends TrustMldStatus
