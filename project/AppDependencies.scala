import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapVersion = "7.15.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % "0.74.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "6.2.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.12.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc"       %% "domain"                         % "8.1.0-play-28",
    "com.typesafe.play" %% "play-json-joda"                 % "2.9.4",
    "org.typelevel"     %% "cats-core"                      % "2.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"               %% "scalatest"                % "3.2.15",
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-28"  % "0.74.0",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"           %% "scalacheck-1-17"          % "3.2.15.0",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "org.jsoup"                   %  "jsoup"                    % "1.15.4",
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current,
    "org.mockito"                 %  "mockito-core"             % "4.6.1",
    "org.scalatestplus"           %% "mockito-4-6"              % "3.2.15.0",
    "org.scalacheck"              %% "scalacheck"               % "1.17.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.64.0"
  ).map(_ % "it, test")

  def apply(): Seq[ModuleID] = compile ++ test

}
