package com.shrfid.api.http.auth

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.persistence.slick.auth.{AuthGroupPermissionEntity, AuthUserEntity}
import com.twitter.finatra.request.{Header, RouteParam}
import play.api.libs.json.Json

/**
  * Created by jiejin on 6/09/2016.
  */
case class GetAuthUserRequest(@Header Authorization: String, @RouteParam id: Int)

case class UserPermission(permissionIds: Seq[Int])

object UserPermission {
  implicit val userPermissionFmt = JsonNaming.snakecase(Json.format[UserPermission])
}

case class GroupPermission(id: Int, permissionIds: Seq[Int])

object GroupPermission {
  implicit val groupPermissionFmt = JsonNaming.snakecase(Json.format[GroupPermission])
}

case class Permission(user: UserPermission, groups: Seq[GroupPermission])

object Permission {
  implicit val permissionFmt = JsonNaming.snakecase(Json.format[Permission])
}

case class GetAuthUserResponse(id: Int,
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
                               isActive: Boolean,
                               createAt: String,
                               updateAt: String,
                               lastLogin: String,
                               permission: Permission)

object GetAuthUserResponse {
  implicit val getAuthUserResponseFmt = JsonNaming.snakecase(Json.format[GetAuthUserResponse])

  def toHttpResponse(u: AuthUserEntity, userPermissionIds: Seq[Int], groupPermissionEntity: Seq[AuthGroupPermissionEntity]) = {

    val obj = GetAuthUserResponse(
      u.id, u.username, u.identity, u.fullName, u.gender,
      u.dob match {
        case Some(a) => a.toString
        case None => ""
      },
      u.email.getOrElse(""),
      u.mobile.getOrElse(""),
      u.address.getOrElse(""),
      u.postcode.getOrElse(""),
      u.profileUrl.getOrElse(""),
      u.isSuperuser,
      u.isStaff,
      u.isActive,
      u.createAt.toLocalDateTime.toString(),
      u.updateAt.toLocalDateTime.toString(),
      u.lastLogin.toLocalDateTime.toString(),
      Permission(UserPermission(userPermissionIds.sorted), groupPermissionEntity.groupBy(_.groupId).map(g => GroupPermission(g._1, g._2.map(_.permissionId))).toSeq.sortBy(_.id))
    )
    Json.stringify(Json.toJson(obj))
  }

}