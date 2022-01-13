/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.TrustsStoreConnector
import controllers.actions.Actions
import controllers.testOnlyDoNotUseInAppConf.FourOrFiveMLD.FiveMLD
import forms.testOnlyDoNotUseInAppConf.{TestWizardForm, TestWizardFormProvider}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testOnlyDoNotUseInAppConf.WizardView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class TestWizardController @Inject()(
                                      val controllerComponents: MessagesControllerComponents,
                                      actions: Actions,
                                      view: WizardView,
                                      formProvider: TestWizardFormProvider,
                                      userConnector: TestUserConnector,
                                      trustStoreConnector: TrustsStoreConnector
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport
  with Logging {

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
      userConnector.delete().map(_ => ())
    } else {
      Future.successful(())
    }
  }

  private def insertTestUser(user: Option[String])(implicit req: Request[AnyContent]): Future[Unit] = {
    user match {
      case Some(value) =>
        Try(Json.parse(value))
          .fold(
            _ =>
              Future.successful(()),
            json =>
              userConnector.insert(json).map(_ => ())
          )
      case None =>
        Future.successful(())
    }
  }

  private def setMode(mode: FourOrFiveMLD)(implicit req: Request[AnyContent]): Future[Unit] = {
    val state = mode == FiveMLD
    trustStoreConnector.setFeature("5mld", state).map(_ => ())
  }

}
