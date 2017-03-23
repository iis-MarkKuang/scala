package com.shrfid.api.domains.auth

import java.sql.Date

import com.github.tototoshi.play.json.JsonNaming
import com.shrfid.api.persistence.slick.auth.{AuthPermissionEntity, AuthUserEntity}
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by jiejin on 9/09/2016.
  */
case class AuthUserInfo(id: Int,
                        username: String,
                        identity: String,
                        fullName: String,
                        gender: String,
                        dob: Option[Date],
                        email: Option[String],
                        mobile: Option[String],
                        address: Option[String],
                        postcode: Option[String],
                        profileUrl: Option[String],
                        isSuperuser: Boolean,
                        isStaff: Boolean,
                        createAt: DateTime,
                        updateAt: DateTime,
                        lastLogin: DateTime,
                        permissions: Seq[AuthPermission])

object AuthUserInfo {
  implicit val authInfoFmt = JsonNaming.snakecase(Json.format[AuthUserInfo])
  java.sql.Date.valueOf("1990-01-01").toString
  def toDomain(u: AuthUserEntity, ps: Seq[AuthPermissionEntity]): AuthUserInfo = {
    AuthUserInfo(u.id, u.username, u.identity, u.fullName, u.gender, u.dob, u.email, u.mobile,
      u.address, u.postcode, u.profileUrl, u.isSuperuser, u.isStaff, u.createAt, u.updateAt, u.lastLogin,
      ps.map(p => AuthPermission(p.id, p.appLabel, p.name, p.path)))
  }
}

