package com.shrfid.api.http.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.http._
import com.shrfid.api.persistence.slick.auth.AuthUserEntity
import com.twitter.finatra.request.{Header, QueryParam}
import play.api.libs.json.Json

/**
  * Created by jiejin on 23/9/16.
  */

case class GetAuthUserListRequest(@Header Authorization: String,
                                  @QueryParam limit: Int = 100,
                                  @QueryParam offset: Int = 0,
                                  @QueryParam username: Option[String],
                                  @QueryParam identity: Option[String],
                                  @QueryParam fullName: Option[String],
                                  @QueryParam gender: Option[String],
                                  @QueryParam isSuperuser: Option[Boolean],
                                  @QueryParam isActive: Option[Boolean],
                                  @QueryParam ordering: Option[String])

case class AuthUserListRow(id: Int,
                           username: String,
                           identity: String,
                           fullName: String,
                           gender: String,
                           isSuperuser: Boolean,
                           isStaff: Boolean,
                           isActive: Boolean,
                           createAt: String,
                           updateAt: String,
                           lastLogin: String)

object AuthUserListRow {
  implicit val authUserListRowFmt = JsonNaming.snakecase(Json.format[AuthUserListRow])
}

case class GetAuthUserListResponse(count: Int, prev: String, next: String, content: Seq[AuthUserListRow])

object GetAuthUserListResponse {

  implicit val authUserListFmt = JsonNaming.snakecase(Json.format[GetAuthUserListResponse])

  def toHttpResponse(content: Seq[AuthUserEntity], path: String, limit: Int, offset: Int, count: Int): String = {
    val obj = GetAuthUserListResponse(count = count,
      prev = prev(path, limit, offset, count),
      next = next(path, limit, offset, count),
      content = content.map(c => AuthUserListRow(
        c.id,
        c.username,
        c.identity,
        c.fullName,
        c.gender,
        c.isSuperuser,
        c.isStaff,
        c.isActive,
        c.createAt.toString,
        c.updateAt.toString,
        c.lastLogin.toString
      )))
    Json.stringify(Json.toJson(obj))
  }
}