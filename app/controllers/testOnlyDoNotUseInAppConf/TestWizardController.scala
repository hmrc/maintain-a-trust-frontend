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

package controllers.testOnlyDoNotUseInAppConf

import config.FrontendAppConfig
import controllers.actions.Actions
import controllers.testOnlyDoNotUseInAppConf.FourOrFiveMLD.{FiveMLD, FourMLD}
import forms.testOnlyDoNotUseInAppConf.{TestWizardForm, TestWizardFormProvider}
import javax.inject.Inject
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.http.{HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testOnlyDoNotUseInAppConf.WizardView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class TestWizardController @Inject()(val controllerComponents: MessagesControllerComponents,
                                     actions: Actions,
                                     view: WizardView,
                                     formProvider: TestWizardFormProvider,
                                     http: HttpClient,
                                     appConfig: FrontendAppConfig
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport
  with Logging {

  import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

  lazy val host: String = appConfig.testWizardHost

  def onPageLoad : Action[AnyContent] = actions.auth.async {
    implicit request =>
      Future.successful(Ok(view(formProvider())))
  }

  def onSubmit : Action[AnyContent] = actions.auth.async {
    implicit request =>

      val form: Form[TestWizardForm] = formProvider()

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          logger.info(s"[Wizard] error validating the form")
          Future.successful(BadRequest(view(formWithErrors)))
        },
        values =>
          for {
            _ <- setMode(values.mode)
            _ <- flushTestUsers(values.flushTestUsers)
            _ <- insertTestUser(values.testUser)
          } yield {
            Redirect(routes.TestWizardController.onPageLoad()).flashing("wizard" -> "Wizard has completed")
          }
      )
  }

  private def flushTestUsers(cleanup: Boolean)(implicit req: Request[AnyContent]): Future[Unit] = {
    if (cleanup){
      val url = s"$host${routes.EnrolmentStoreStubController.flush().url}"
      http.DELETE[HttpResponse](url)
        .map(_ => ())
    } else {
      Future.successful(())
    }
  }

  private def insertTestUser(user: Option[String])(implicit req: Request[AnyContent]): Future[Unit] = {
    user match {
      case Some(value) =>
        Try(Json.parse(value)).fold(
          _ => {
            Future.successful(())
          },
          json => {
            val url = s"$host${routes.EnrolmentStoreStubController.insertTestUserIntoEnrolmentStore().url}"
            http.POST[JsValue, HttpResponse](url, json).map(_ => ())
          }
        )
      case None =>
        Future.successful(())
    }
  }

  private def setMode(mode: FourOrFiveMLD)(implicit req: Request[AnyContent]): Future[Unit] = {
    mode match {
      case FourMLD =>
        val url = s"$host${routes.TestTrustsStoreController.set4Mld().url}"
        http.PUT[Boolean, HttpResponse](url, false).map(_ => ())
      case FiveMLD =>
        val url = s"$host${routes.TestTrustsStoreController.set5Mld().url}"
        http.PUT[Boolean, HttpResponse](url, true).map(_ => ())
    }
  }

}
