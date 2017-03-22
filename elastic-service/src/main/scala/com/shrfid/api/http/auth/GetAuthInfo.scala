package com.shrfid.api.http.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.domains.auth.{AuthApp, AuthPermission, AuthUserInfo}
import com.twitter.finatra.request.Header
import play.api.libs.json.Json

/**
  * Created by jiejin on 9/09/2016.
  */
case class GetAuthInfoRequest(@Header Authorization: String)

case class GetAuthInfoResponse(id: Int,
                               username: String,
                               identity: String,
                               fullName: String,
                               gender: String,
                               dob: String,
                               email: String,
                               mobile: String,
                               address: String,
                               postcode: String,
                               profileUrl: String,
                               isSuperuser: Boolean,
                               isStaff: Boolean,
                               createAt: String,
                               updateAt: String,
                               lastLogin: String,
                               apps: Seq[AuthApp])

object GetAuthInfoResponse {

  implicit val getAuthInfoResponseFmt = JsonNaming.snakecase(Json.format[GetAuthInfoResponse])

  def toHttpResponse(userStr: String): String = {
    val u = Json.parse(userStr).as[AuthUserInfo]
    Json.stringify(Json.toJson(GetAuthInfoResponse(u.id, u.username, u.identity, u.fullName, u.gender,
      u.dob match {
        case Some(d) => d.toString
        case None => ""
      },
      u.email.getOrElse(""),
      u.mobile.getOrElse(""),
      u.address.getOrElse(""),
      u.postcode.getOrElse(""),
      u.profileUrl.getOrElse(""),
      u.isSuperuser,
      u.isStaff,
      u.createAt.toLocalDateTime.toString(),
      u.updateAt.toLocalDateTime.toString(),
      u.lastLogin.toLocalDateTime.toString(),
      AuthPermission.toApp(u.permissions))))
  }

  def toHttpResponse(u: AuthUserInfo): String = {
    Json.stringify(Json.toJson(GetAuthInfoResponse(u.id, u.username, u.identity, u.fullName, u.gender,
      u.dob match {
        case Some(d) => d.toString
        case None => ""
      },
      u.email.getOrElse(""),
      u.mobile.getOrElse(""),
      u.address.getOrElse(""),
      u.postcode.getOrElse(""),
      u.profileUrl.getOrElse(""),
      u.isSuperuser,
      u.isStaff,
      u.createAt.toLocalDateTime.toString(),
      u.updateAt.toLocalDateTime.toString(),
      u.lastLogin.toLocalDateTime.toString(),
      AuthPermission.toApp(u.permissions))))
  }

}