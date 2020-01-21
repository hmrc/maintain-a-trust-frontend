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

import models.requests.IdentifierRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.AnyContent
import repositories.PlaybackRepository

import scala.concurrent.Future

trait Mocked extends MockitoSugar {

//  val registrationsRepository : RegistrationsRepository = mock[RegistrationsRepository]
  val playbackRepository : PlaybackRepository = mock[PlaybackRepository]

//  val mockSubmissionService : SubmissionService = mock[SubmissionService]
//  val mockCreateDraftRegistrationService : CreateDraftRegistrationService = mock[CreateDraftRegistrationService]

//  when(mockCreateDraftRegistrationService.create(any[OptionalRegistrationDataRequest[AnyContent]])(any()))
//    .thenReturn(Future.successful(TestUserAnswers.draftId))
//
//  when(mockCreateDraftRegistrationService.create(any[IdentifierRequest[AnyContent]])(any()))
//      .thenReturn(Future.successful(TestUserAnswers.draftId))
//
//  when(registrationsRepository.set(any())).thenReturn(Future.successful(true))
  when(playbackRepository.set(any())).thenReturn(Future.successful(true))

}
