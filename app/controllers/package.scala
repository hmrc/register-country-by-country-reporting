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

import models.ErrorDetails
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Success, Try}

package object controllers {

  def convertToResult(
    httpResponse: HttpResponse
  )(implicit logger: Logger): Result =
    httpResponse.status match {
      case OK        => Ok(httpResponse.body)
      case NOT_FOUND => NotFound(httpResponse.body)
      case BAD_REQUEST =>
        logDownStreamError(httpResponse.body)
        BadRequest(httpResponse.body)

      case FORBIDDEN =>
        logDownStreamError(httpResponse.body)
        Forbidden(httpResponse.body)

      case SERVICE_UNAVAILABLE =>
        logDownStreamError(httpResponse.body)
        ServiceUnavailable(httpResponse.body)

      case CONFLICT =>
        logDownStreamError(httpResponse.body)
        Conflict(httpResponse.body)

      case _ =>
        logDownStreamError(httpResponse.body)
        InternalServerError(httpResponse.body)

    }

  private def logDownStreamError(
    body: String
  )(implicit logger: Logger): Unit = {
    val error = Try(Json.parse(body).validate[ErrorDetails])
    error match {
      case Success(JsSuccess(value, _)) =>
        logger.warn(
          s"Error with submission: ${value.errorDetail.sourceFaultDetail.map(_.detail.mkString)}"
        )
      case _ =>
        logger.warn("Error with submission but return is not a valid json")
    }
  }
}
