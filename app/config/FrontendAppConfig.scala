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

package config

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.Call

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration) {

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trusts"

  private def loadConfig(key: String) = configuration.get[String](key)

  def maintainTrusteesUrl(utr: String) = s"$maintainATrusteeFrontendUrl/$utr"

  def maintainBeneficiariesUrl(utr: String) = s"$maintainBeneficiariesFrontendUrl/$utr"

  def maintainSettlorsUrl(utr: String) = s"$maintainSettlorsFrontendUrl/$utr"

  def claimATrustUrl(utr: String) =
    configuration.get[Service]("microservice.services.claim-a-trust-frontend").baseUrl + s"/claim-a-trust/save/$utr"

  val analyticsToken: String = configuration.get[String](s"google-analytics.token")
  val analyticsHost: String = configuration.get[String](s"google-analytics.host")
  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val agentsSubscriptionsUrl: String = configuration.get[String]("urls.agentSubscriptions")
  lazy val agentServiceRegistrationUrl = s"$agentsSubscriptionsUrl?continue=$loginContinueUrl"

  lazy val agentInvitationsUrl: String = configuration.get[String]("urls.agentInvitations")

  lazy val declarationEmailEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.declaration.email.enabled")

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = loadConfig("urls.logout")

  lazy val trustsUrl: String = configuration.get[Service]("microservice.services.trusts").baseUrl
  lazy val trustsStoreUrl: String = configuration.get[Service]("microservice.services.trusts-store").baseUrl + "/trusts-store"
  lazy val trustAuthUrl: String = configuration.get[Service]("microservice.services.trusts-auth").baseUrl

  lazy val agentOverviewUrl: String = configuration.get[String]("urls.agentOverview")

  lazy val enrolmentStoreProxyUrl: String = configuration.get[Service]("microservice.services.enrolment-store-proxy").baseUrl

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListNonUK: String = loadConfig("location.canonical.list.nonUK")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val countdownLength: String = configuration.get[String]("timeout.countdown")
  lazy val timeoutLength: String = configuration.get[String]("timeout.length")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val playbackEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.playback.enabled")

  lazy val maintainTrusteesEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-trustees.enabled")
  lazy val maintainBeneficiariesEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-beneficiaries.enabled")
  lazy val maintainSettlorsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-settlors.enabled")
  lazy val maintainProtectorsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-protectors.enabled")
  lazy val maintainOtherIndividualsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-other-individuals.enabled")

  lazy val maintainATrusteeFrontendUrl : String =
    configuration.get[String]("urls.maintainATrustee")

  lazy val maintainBeneficiariesFrontendUrl : String =
    configuration.get[String]("urls.maintainABeneficiary")

  lazy val maintainSettlorsFrontendUrl : String =
    configuration.get[String]("urls.maintainASettlor")

  lazy val accessibilityLinkUrl: String = configuration.get[String]("urls.accessibility")
}
