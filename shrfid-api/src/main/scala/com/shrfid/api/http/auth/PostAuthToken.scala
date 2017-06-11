package com.shrfid.api.http.auth

import com.twitter.finatra.validation.Size
import play.api.libs.json.Json

/**
  * Created by jiejin on 9/09/2016.
  */
case class PostAuthTokenRequest(@Size(min = 1, max = 30) username: String,
                                @Size(min = 1, max = 128) password: String)

case class PostAuthTokenResponse(token: String)

object PostAuthTokenResponse {
  implicit val postAuthTokenResponseFmt = Json.format[PostAuthTokenResponse]
}