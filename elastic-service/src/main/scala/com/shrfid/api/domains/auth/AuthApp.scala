package com.shrfid.api.domains.auth

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

/**
  * Created by jiejin on 19/09/2016.
  */
case class AuthApp(appLabel: String, permissions: Seq[AuthPermission])

object AuthApp {
  implicit val authAppFmt = JsonNaming.snakecase(Json.format[AuthApp])
}
