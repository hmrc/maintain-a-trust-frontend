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

package services

import base.SpecBase
import cats.data.EitherT
import connectors.{TrustConnector, TrustsStoreConnector}
import models.errors.TrustErrors
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{verify, when}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MaintainATrustServiceSpec extends SpecBase {

  "MaintainATrustService" when {

    ".removeTransformsAndResetTaskList" must {
      "remove transforms and reset task list" in {

        implicit val hc: HeaderCarrier = HeaderCarrier()

        val identifier = "identifier"

        val mockTrustsConnector = mock[TrustConnector]
        val mockTrustsStoreConnector = mock[TrustsStoreConnector]

        when(mockTrustsConnector.removeTransforms(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))
        when(mockTrustsStoreConnector.resetTasks(any())(any(), any()))
          .thenReturn(EitherT[Future, TrustErrors, HttpResponse](Future.successful(Right(okResponse))))

        val service = new MaintainATrustService(mockTrustsConnector, mockTrustsStoreConnector)

        Await.result(service.removeTransformsAndResetTaskList(identifier).value, Duration.Inf)

        verify(mockTrustsConnector).removeTransforms(eqTo(identifier))(any(), any())
        verify(mockTrustsStoreConnector).resetTasks(eqTo(identifier))(any(), any())
      }
    }
  }

}
