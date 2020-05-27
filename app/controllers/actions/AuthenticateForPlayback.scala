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

package controllers.actions

import com.google.inject.Inject
import models.requests.{DataRequest, OptionalDataRequest, WhatNextRequest}
import play.api.mvc.{ActionBuilder, AnyContent}

class AuthenticateForPlayback @Inject()(
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         playbackIdentifier: PlaybackIdentifierAction,
                                         refreshedDataRetrieval: RefreshedDataRetrievalAction,
                                         requireWhatNextAnswer: WhatNextRequiredAction
                                       ) {

  def authWithSession: ActionBuilder[OptionalDataRequest, AnyContent] =
    identify andThen getData

  def authWithData: ActionBuilder[DataRequest, AnyContent] =
    authWithSession andThen requireData

  def verifiedForUtr: ActionBuilder[DataRequest, AnyContent] =
    authWithData andThen playbackIdentifier

  def refreshedData: ActionBuilder[DataRequest, AnyContent] =
    verifiedForUtr andThen refreshedDataRetrieval

  def requireAnswer: ActionBuilder[WhatNextRequest, AnyContent] =
    verifiedForUtr andThen requireWhatNextAnswer
}
