package com.shrfid.api.http.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.domains.auth.AuthPermission
import com.shrfid.api.persistence.slick.auth.AuthPermissionEntity
import com.twitter.finatra.request.Header
import play.api.libs.json.Json

/**
  * Created by jiejin on 19/09/2016.
  */
case class GetAuthPermissionListRequest(@Header Authorization: String)

case class GetAuthPermissionListResponse(appLabel: String, permissions: Seq[AuthPermission])


object GetAuthPermissionListResponse {

  implicit val getAuthPermissionListResponseFmt = JsonNaming.snakecase(Json.format[GetAuthPermissionListResponse])

  def toHttpResponse(permissions: Seq[AuthPermissionEntity]): String = {
    Json.stringify(Json.toJson(AuthPermission.toApp(AuthPermission.toDomain(permissions))))
  }
}