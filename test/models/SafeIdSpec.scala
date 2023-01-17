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

package models

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import play.api.mvc.PathBindable

class SafeIdSpec extends AnyFreeSpec with Matchers with EitherValues {

  val safeId: SafeId = SafeId("XE0000123456789")

  "Safe Id" - {
    "must bind from url" in {
      val pathBindable = implicitly[PathBindable[SafeId]]

      val bind: Either[String, SafeId] =
        pathBindable.bind("safeId", "XE0000123456789")
      bind.value mustBe safeId
    }

    "unbind to path value" in {
      val pathBindable = implicitly[PathBindable[SafeId]]

      val bindValue = pathBindable.unbind("safeId", safeId)
      bindValue mustBe "XE0000123456789"
    }

    "must serialize and deserialize" in {
      Json.toJson(safeId).as[SafeId] mustBe safeId
    }
  }
}
