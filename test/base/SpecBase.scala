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

package base
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import org.scalatest.{BeforeAndAfter, EitherValues, TestSuite, TryValues}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.BodyParsers
import repositories.PlaybackRepository
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUserAnswers

trait SpecBaseHelpers extends GuiceOneAppPerSuite with TryValues with Mocked with BeforeAndAfter with FakeTrustsApp {
  this: TestSuite =>

  final val ENGLISH = "en"
  final val WELSH   = "cy"

  def emptyUserAnswersForUtr: UserAnswers = TestUserAnswers.emptyUserAnswersForUtr
  def emptyUserAnswersForUrn: UserAnswers = TestUserAnswers.emptyUserAnswersForUrn
  val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  val bodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]

  protected def applicationBuilder(
    userAnswers: Option[models.UserAnswers] = None,
    affinityGroup: AffinityGroup = AffinityGroup.Organisation,
    enrolments: Enrolments = Enrolments(Set.empty[Enrolment])
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers, affinityGroup, enrolments)),
        bind[PlaybackIdentifierAction].toInstance(new FakePlaybackIdentifierAction()),
        bind[DataRetrievalRefinerAction].toInstance(
          new FakeDataRetrievalRefinerAction(
            userAnswers,
            mockActiveSessionRepository,
            mockPlaybackRepository,
            mockErrorHandler,
            appConfig
          )
        ),
        bind[RefreshedDataRetrievalAction].toInstance(new FakeRefreshedDataRetrievalAction),
        bind[RefreshedDataPreSubmitRetrievalAction].toInstance(new FakeRefreshedDataPreSubmitRetrievalAction),
        bind[DataRequiredAction].toInstance(new FakeDataRequiredAction(userAnswers)),
        bind[PlaybackRepository].toInstance(mockPlaybackRepository)
      )

  val okResponse: HttpResponse = HttpResponse(OK, "")

}

trait SpecBase extends PlaySpec with SpecBaseHelpers with EitherValues
