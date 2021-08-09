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

package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import base.SpecBase
import connectors.TrustsObligedEntityOutputConnector
import play.api.http.Status._
import play.api.http.HeaderNames._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.libs.json.JsValue
import play.api.libs.ws
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, route, status, writeableOf_AnyContentAsEmpty}

import java.net.URI
import scala.concurrent.Future
import scala.xml.Elem

class ObligedEntityPdfControllerSpec extends SpecBase {

  private val mockConnector: TrustsObligedEntityOutputConnector = mock[TrustsObligedEntityOutputConnector]

  private case class MockResponse(s: Int, h: Map[String, Seq[String]]) extends WSResponse {
    override def status: Int = s
    override def headers: Map[String, Seq[String]] = h
    override def bodyAsSource: Source[ByteString, _] = Source(List(ByteString("responseBody")))
    override def statusText: String = ???
    override def underlying[T]: T = ???
    override def cookies: Seq[ws.WSCookie] = ???
    override def cookie(name: String): Option[ws.WSCookie] = ???
    override def body: String = ???
    override def bodyAsBytes: ByteString = ???
    override def allHeaders: Map[String, Seq[String]] = ???
    override def xml: Elem = ???
    override def json: JsValue = ???
    override def uri: URI = ???
  }

  private val headers: Map[String, Seq[String]] = Map(
    CONTENT_DISPOSITION -> "inline; file-name.pdf",
    CONTENT_TYPE -> "application/pdf",
    CONTENT_LENGTH -> "12345"
  ).map(x => x._1 -> Seq(x._2))

  private val identifier: String = "1234567890"

  private lazy val getPdfRoute: String = routes.ObligedEntityPdfController.getPdf(identifier).url

  "TestTrustsObligedEntityOutputController" must {

    "return Ok" when {

      "OK response status from connector" when {

        "all headers present" in {

          when(mockConnector.getPdf(any())(any())).thenReturn(Future.successful(MockResponse(OK, headers)))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
            .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
            .build()

          val request = FakeRequest(GET, getPdfRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          application.stop()
        }
      }
    }

    "return InternalServerError" when {

      "non-OK response from connector" in {

        when(mockConnector.getPdf(any())(any())).thenReturn(Future.successful(MockResponse(INTERNAL_SERVER_ERROR, headers)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, getPdfRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "Content-Disposition header missing" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(MockResponse(INTERNAL_SERVER_ERROR, headers.filterNot(_._1 != CONTENT_DISPOSITION))))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, getPdfRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "Content-Type header missing" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(MockResponse(INTERNAL_SERVER_ERROR, headers.filterNot(_._1 != CONTENT_TYPE))))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, getPdfRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "Content-Length header missing" in {

        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.successful(MockResponse(INTERNAL_SERVER_ERROR, headers.filterNot(_._1 != CONTENT_LENGTH))))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, getPdfRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }
    }

    "exception is thrown" must {

      "render a technical difficulties page" in {
        when(mockConnector.getPdf(any())(any()))
          .thenReturn(Future.failed(new Exception("gateway timeout")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersForUtr))
          .overrides(bind[TrustsObligedEntityOutputConnector].toInstance(mockConnector))
          .build()

        val request = FakeRequest(GET, getPdfRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

    }
  }
}
