package com.shrfid.api.domains.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.persistence.slick.auth.AuthPermissionEntity
import play.api.libs.json.Json

/**
  * Created by jiejin on 9/09/2016.
  */
case class AuthPermission(id: Int, appLabel: String, name: String, path: String)

object AuthPermission {
  implicit val authPermissionFmt = JsonNaming.snakecase(Json.format[AuthPermission])

  def toApp(permissions: Seq[AuthPermission]) = {
    permissions.groupBy(_.appLabel).map(a => AuthApp(a._1, a._2.map(p => AuthPermission(p.id, p.appLabel, p.name, p.path)))).toSeq
  }

  def toDomain(p: AuthPermissionEntity) = {
    AuthPermission(p.id, p.appLabel, p.name, p.path)
  }

  def toDomain(permissions: Seq[AuthPermissionEntity]) = {
    permissions.map(p => AuthPermission(p.id, p.appLabel, p.name, p.path))
  }
}