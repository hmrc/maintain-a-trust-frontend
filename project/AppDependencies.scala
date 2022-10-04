import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"             % "0.73.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "3.5.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.11.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % "5.24.0",
    "uk.gov.hmrc"       %% "domain"                         % "8.0.0-play-28",
    "com.typesafe.play" %% "play-json-joda"                 % "2.9.2",
    "org.typelevel"     %% "cats-core"                      % "2.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"               %% "scalatest"                % "3.2.12",
    "uk.gov.hmrc.mongo"           %% "hmrc-mongo-test-play-28" %"0.73.0",
      "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"           %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "wolfendale"                  %% "scalacheck-gen-regexp"    % "0.1.2",
    "org.jsoup"                   %  "jsoup"                    % "1.15.1",
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current,
    "org.mockito"                 %  "mockito-all"              % "1.10.19",
    "org.mockito"                 %  "mockito-core"             % "4.6.1",
    "org.scalatestplus"           %% "mockito-3-12"             % "3.2.10.0",
    "org.scalacheck"              %% "scalacheck"               % "1.16.0",
    "com.github.tomakehurst"      %  "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"        %  "flexmark-all"             % "0.62.2"
  ).map(_ % "it, test")

  def apply(): Seq[ModuleID] = compile ++ test

  val akkaVersion = "2.6.12"
  val akkaHttpVersion = "10.2.3"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec"     % "commons-codec"   % "1.12"
  )
}
