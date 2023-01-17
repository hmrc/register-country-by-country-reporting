/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import com.google.inject.Inject
import config.AppConfig
import connectors.RegistrationConnector
import controllers.auth.AuthAction
import models.{ErrorDetails, RegisterWithID, RegisterWithoutId}
import play.api.Logging
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Result}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class RegistrationController @Inject() (
  val config: AppConfig,
  authenticate: AuthAction,
  registrationConnector: RegistrationConnector,
  override val controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends BackendController(controllerComponents)
    with Logging {

  def withoutOrgID: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      logger.info("Organisation without ID")
      request.body
        .validate[RegisterWithoutId]
        .fold(
          _ => Future.successful(BadRequest("")),
          sub =>
            registrationConnector
              .sendWithoutIDInformation(sub)
              .map(convertToResult)
        )
  }

  def withOrgUTR: Action[JsValue] = authenticate(parse.json).async {
    implicit request =>
      logger.info("Organisation having UTR")
      request.body
        .validate[RegisterWithID]
        .fold(
          _ => Future.successful(BadRequest("")),
          sub => registrationConnector.sendWithID(sub).map(convertToResult)
        )
  }

  private def convertToResult(httpResponse: HttpResponse): Result =
    httpResponse.status match {
      case OK        => Ok(httpResponse.body)
      case NOT_FOUND => NotFound(httpResponse.body)

      case BAD_REQUEST =>
        logDownStreamError(httpResponse.body)

        BadRequest(httpResponse.body)

      case FORBIDDEN =>
        logDownStreamError(httpResponse.body)

        Forbidden(httpResponse.body)

      case _ =>
        logDownStreamError(httpResponse.body)
        InternalServerError(httpResponse.body)
    }

  private def logDownStreamError(body: String): Unit = {
    val error = Try(Json.parse(body).validate[ErrorDetails])
    error match {
      case Success(JsSuccess(value, _)) =>
        logger.error(
          s"Error with submission: ${value.errorDetail.sourceFaultDetail.map(_.detail.mkString)}"
        )
      case _ =>
        logger.error("Error with submission but return is not a valid json")
    }
  }
}
