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

package config

import java.net.{URI, URLEncoder}

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, Request}

@Singleton
class FrontendAppConfig @Inject()(val configuration: Configuration) {

  final val ENGLISH = "en"
  final val WELSH = "cy"

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trusts"

  private def loadConfig(key: String) = configuration.get[String](key)

  def maintainTrustDetailsUrl(identifier: String) = s"$maintainTrustDetailsFrontendUrl/$identifier"
  def maintainTrustAssetsUrl(identifier: String) = s"$maintainTrustAssetsFrontendUrl/$identifier"
  def maintainTaxLiabilityUrl(identifier: String) = s"$maintainTaxLiabilityFrontendUrl/$identifier"
  def maintainTrusteesUrl(identifier: String) = s"$maintainTrusteesFrontendUrl/$identifier"
  def maintainBeneficiariesUrl(identifier: String) = s"$maintainBeneficiariesFrontendUrl/$identifier"
  def maintainSettlorsUrl(identifier: String) = s"$maintainSettlorsFrontendUrl/$identifier"
  def maintainProtectorsUrl(identifier: String) = s"$maintainProtectorsFrontendUrl/$identifier"
  def maintainOtherIndividualsUrl(identifier: String) = s"$maintainOtherIndividualsFrontendUrl/$identifier"
  def maintainNonEeaCompanyUrl(identifier: String) = s"$maintainNonEeaCompaniesFrontendUrl/$identifier"

  val analyticsToken: String = configuration.get[String](s"google-analytics.token")

  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val agentsSubscriptionsUrl: String = configuration.get[String]("urls.agentSubscriptions")
  lazy val agentServiceRegistrationUrl = s"$agentsSubscriptionsUrl?continue=$loginContinueUrl"

  lazy val agentInvitationsUrl: String = configuration.get[String]("urls.agentInvitations")

  lazy val declarationEmailEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.declaration.email.enabled")

  lazy val trustsIndividualCheck: String = configuration.get[Service]("microservice.services.trusts-individual-check").baseUrl

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  lazy val logoutUrl: String = loadConfig("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val trustsUrl: String = configuration.get[Service]("microservice.services.trusts").baseUrl
  lazy val trustsStoreUrl: String = configuration.get[Service]("microservice.services.trusts-store").baseUrl + "/trusts-store"
  lazy val trustAuthUrl: String = configuration.get[Service]("microservice.services.trusts-auth").baseUrl

  lazy val trustsObligedEntityOutputUrl: String = configuration.get[Service]("microservice.services.trusts-obliged-entity-output").baseUrl

  lazy val agentOverviewUrl: String = configuration.get[String]("urls.agentOverview")
  lazy val serviceDownContactUrl: String = "/contact/problem_reports_nonjs?service=trusts"

  lazy val enrolmentStoreProxyUrl: String = configuration.get[Service]("microservice.services.enrolment-store-proxy").baseUrl

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListCY: String = loadConfig("location.canonical.list.allCY")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val countdownLength: String = configuration.get[String]("timeout.countdown")
  lazy val timeoutLength: String = configuration.get[String]("timeout.length")

  def verifyIdentityForATrustUrl(utr: String) =
    s"${configuration.get[String]("urls.startVerifyIdentity")}/$utr"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)

  lazy val playbackEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.playback.enabled")

  lazy val maintainTrustDetailsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-trust-details.enabled")
  lazy val maintainTrustAssetsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-trust-assets.enabled")
  lazy val maintainTaxLiabilityEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-tax-liability.enabled")
  lazy val maintainTrusteesEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-trustees.enabled")
  lazy val maintainBeneficiariesEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-beneficiaries.enabled")
  lazy val maintainSettlorsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-settlors.enabled")
  lazy val maintainProtectorsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-protectors.enabled")
  lazy val maintainOtherIndividualsEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-other-individuals.enabled")
  lazy val maintainNonEeaCompaniesEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.maintain-non-eea-companies.enabled")

  lazy val closeATrustEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.close-a-trust.enabled")
  lazy val migrateATrustEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.migrate-a-trust.enabled")

  private lazy val maintainTrustDetailsFrontendUrl: String =
    configuration.get[String]("urls.maintainTrustDetails")

  private lazy val maintainTrustAssetsFrontendUrl: String =
    configuration.get[String]("urls.maintainTrustAssets")

  private lazy val maintainTaxLiabilityFrontendUrl: String =
    configuration.get[String]("urls.maintainTaxLiability")

  private lazy val maintainTrusteesFrontendUrl: String =
    configuration.get[String]("urls.maintainATrustee")

  private lazy val maintainBeneficiariesFrontendUrl: String =
    configuration.get[String]("urls.maintainABeneficiary")

  private lazy val maintainSettlorsFrontendUrl: String =
    configuration.get[String]("urls.maintainASettlor")

  private lazy val maintainProtectorsFrontendUrl: String =
    configuration.get[String]("urls.maintainAProtector")

  private lazy val maintainOtherIndividualsFrontendUrl: String =
    configuration.get[String]("urls.maintainAnOtherIndividual")

  private lazy val maintainNonEeaCompaniesFrontendUrl: String =
    configuration.get[String]("urls.maintainANonEeaCompany")

  lazy val sa900Link: String =
    configuration.get[String]("urls.sa900")

  private lazy val accessibilityBaseLinkUrl: String = configuration.get[String]("urls.accessibility")

  def accessibilityLinkUrl(implicit request: Request[_]): String = {
    val userAction = URLEncoder.encode(new URI(request.uri).getPath, "UTF-8")
    s"$accessibilityBaseLinkUrl?userAction=$userAction"
  }

  def helplineUrl(implicit messages: Messages): String = {
    val path = messages.lang.code match {
      case WELSH => "urls.welshHelpline"
      case _ => "urls.trustsHelpline"
    }

    configuration.get[String](path)
  }
}
