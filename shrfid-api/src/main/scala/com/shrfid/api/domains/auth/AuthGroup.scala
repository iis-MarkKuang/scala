package com.shrfid.api.domains.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.persistence.slick.auth.{AuthGroupEntity, AuthGroupNestedEntity}
import play.api.libs.json.Json

/**
  * Created by jiejin on 19/09/2016.
  */
case class AuthGroup(id: Int, name: String)

object AuthGroup {
  implicit val authGroupFmt = JsonNaming.snakecase(Json.format[AuthGroup])

  def toDomain(group: AuthGroupEntity): AuthGroup = {
    AuthGroup(group.id, group.name)
  }

  def toDomain(groups: Seq[AuthGroupEntity]): Seq[AuthGroup] = {
    groups.map(g => AuthGroup(g.id, g.name))
  }
}

case class AuthGroupNested(id: Int, name: String, permissions: Seq[AuthPermission])

object AuthGroupNested {
  implicit val authGroupNestedFmt = JsonNaming.snakecase(Json.format[AuthGroupNested])

  def toDomain(group: AuthGroupNestedEntity): AuthGroupNested = {
    AuthGroupNested(group.id, group.name, group.permissions.map(p => AuthPermission.toDomain(p)))
  }

  def toDomain(groups: Seq[AuthGroupNestedEntity]): Seq[AuthGroupNested] = {
    groups.map(g => AuthGroupNested(g.id, g.name, g.permissions.map(p => AuthPermission.toDomain(p))))
  }
}