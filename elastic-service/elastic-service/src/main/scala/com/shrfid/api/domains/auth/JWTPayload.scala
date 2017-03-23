package com.shrfid.api.domains.auth

import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by jiejin on 9/09/2016.
  */

case class JWTPayload(iss: String, user: AuthUserInfo, nbf: DateTime)

object JWTPayload {
  implicit val jwtPayloadFmt = Json.format[JWTPayload]
}