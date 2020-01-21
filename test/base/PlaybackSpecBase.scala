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

package base

import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, FakeDataRetrievalAction, FakeIdentifierAction, IdentifierAction}
import org.scalatest.{BeforeAndAfter, TestSuite, TryValues}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, PlayBodyParsers}
import repositories.PlaybackRepository
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}
import utils.TestUserAnswers

trait PlaybackSpecBaseHelpers extends GuiceOneAppPerSuite with TryValues with Mocked with BeforeAndAfter with FakeTrustsApp {
  this: TestSuite =>

  def emptyUserAnswers = models.UserAnswers(TestUserAnswers.userInternalId)


  def bodyParsers = injector.instanceOf[PlayBodyParsers]


  protected def applicationBuilder(userAnswers: Option[models.UserAnswers] = None,
                                   affinityGroup: AffinityGroup = AffinityGroup.Organisation,
                                   enrolments: Enrolments = Enrolments(Set.empty[Enrolment])
                                  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
//        bind[RegistrationIdentifierAction].toInstance(new FakeIdentifyForRegistration(affinityGroup)(injectedParsers, trustsAuth, enrolments)),
        bind[IdentifierAction].toInstance(new FakeIdentifierAction(bodyParsers)),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[PlaybackRepository].toInstance(playbackRepository)
//        bind[RegistrationsRepository].toInstance(registrationsRepository),
//        bind[SubmissionService].toInstance(mockSubmissionService),
//        bind[Navigator].qualifiedWith(classOf[PropertyOrLand]).toInstance(navigator),
//        bind[Navigator].qualifiedWith(classOf[LivingSettlor]).toInstance(navigator)
      )

}

trait PlaybackSpecBase extends PlaySpec with PlaybackSpecBaseHelpers