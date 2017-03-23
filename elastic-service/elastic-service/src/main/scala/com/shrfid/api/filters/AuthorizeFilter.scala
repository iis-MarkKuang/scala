package com.shrfid.api.filters

import javax.inject.Inject

import com.shrfid.api._
import com.shrfid.api.domains.auth.AuthUserInfo
import com.shrfid.api.services.AuthService
import com.twitter.finagle.http._
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 19/01/2016.
  */


object AuthUserInfoContext {
  private val AuthUserInfoField = Request.Schema.newField[AuthUserInfo]()

  implicit class UserContextSyntax(val request: Request) extends AnyVal {
    def authUserInfo: AuthUserInfo = request.ctx(AuthUserInfoField)
  }

  private[shrfid] def setUser(request: Request, value: String) = {
    val userInfo = Json.parse(value).as[AuthUserInfo]
    request.ctx.update(AuthUserInfoField, userInfo)
  }
}

class AuthorizeFilter @Inject()(authService: AuthService, response: ResponseBuilder)
  extends SimpleFilter[Request, Response] {

  override def apply(request: Request, continue: Service[Request, Response]): Future[Response] = {
    val authHeader = request.headerMap.getOrElse(Fields.Authorization, "")
    if (!authHeader.startsWith("Bearer ") && authHeader.length > 7)
      response.unauthorized(UnauthorizedResponse).contentTypeJson().toFuture
    else
      authService.getUserInfo(authHeader.substring(7)) flatMap {
        case InvalidToken =>
          response.unauthorized(UnauthorizedResponse).contentTypeJson().toFuture
        case a =>
          AuthUserInfoContext.setUser(request, a)
          continue(request)
      }
  }
}
