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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import models.errors.ServerError
import models.{CompletedMaintenanceTasks, FeatureResponse, UserAnswers}
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.TrustEnvelope.TrustEnvelope
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrustsStoreConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig) extends ConnectorErrorResponseHandler {

  override val className: String = getClass.getSimpleName

  private val trustLockedUrl: String = config.trustsStoreUrl + "/claim"

  private def maintainTasksUrl(identifier: String) = s"${config.trustsStoreUrl}/maintain/tasks/$identifier"

  private def featuresUrl(feature: String) = s"${config.trustsStoreUrl}/features/$feature"

  def get(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[Option[TrustClaim]] = EitherT {
    http
      .get(url"$trustLockedUrl")
      .execute[Option[TrustClaim]](TrustClaim.httpReads(identifier), ec)
      .map(Right(_)).recover {
      case ex =>
        Left(handleError(ex, "get"))
    }
  }

  def set(identifier: String, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[CompletedMaintenanceTasks] = EitherT {
    CompletedMaintenanceTasks.from(userAnswers) match {
      case Some(taskStatusTag) =>
        val maintainUrl = maintainTasksUrl(identifier)
        http
          .post(url"$maintainUrl")
          .withBody(Json.toJson(taskStatusTag))
          .execute[CompletedMaintenanceTasks]
          .map(_ => Right(taskStatusTag))
          .recover {
            case ex => Left(handleError(ex, "set"))
          }
      case None =>
        logger.error(s"[$className][set] Unable to set status tags for Tasks.")
        Future.successful(Left(ServerError()))
    }
  }

  def getStatusOfTasks(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[CompletedMaintenanceTasks] = EitherT {
    val maintainUrl = maintainTasksUrl(identifier)
    http
      .get(url"$maintainUrl")
      .execute[CompletedMaintenanceTasks]
      .map(Right(_))
      .recover {
        case _ => Right(CompletedMaintenanceTasks())
      }
  }

  def resetTasks(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[HttpResponse] = EitherT {
    val maintainUrl = maintainTasksUrl(identifier)
    http
      .delete(url"$maintainUrl")
      .execute[HttpResponse]
      .map(Right(_))
      .recover {
        case ex =>
          Left(handleError(ex, "resetTasks"))
      }
  }

  def getFeature(feature: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[FeatureResponse] = EitherT {
    val featureVal = featuresUrl(feature)
    http
      .get(url"$featureVal")
      .execute[FeatureResponse]
      .map(Right(_))
      .recover {
        case ex =>
          Left(handleError(ex, "getFeature"))
      }
  }

  def setFeature(feature: String, state: Boolean)(implicit hc: HeaderCarrier, ec: ExecutionContext): TrustEnvelope[HttpResponse] = EitherT {
    val featureVal = featuresUrl(feature)
    http
      .put(url"$featureVal")
      .withBody(JsBoolean(state))
      .execute[HttpResponse]
      .map(Right(_))
      .recover {
        case ex =>
          Left(handleError(ex, "setFeature"))
      }
  }
}
