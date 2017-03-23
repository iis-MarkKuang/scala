package com.shrfid.api.services

import com.shrfid.api._
import com.shrfid.api.controllers.Permission._
import com.shrfid.api.persistence.slick.auth.AuthUserEntity
import com.twitter.util.Future

/**
  * Created by jiejin on 9/09/2016.
  */
trait TokenService {
  def validatePassword(a: String, b: String): Future[Boolean]

  def getToken(password: String, u: Option[AuthUserEntity]): Future[String]

  def checkPermissionWithCallBack[T](token: String, permission: PermissionCode)(args: T, callback: (Username, T) => Future[(Int, String)]): Future[(Int, String)]
}
