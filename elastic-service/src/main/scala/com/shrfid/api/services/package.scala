package com.shrfid.api

import com.shrfid.api.domains.auth.AuthUserInfo
import com.twitter.util.Future

/**
  * Created by jiejin on 13/09/2016.
  */
package object services {

  def checkUserPermissions(userInfo: AuthUserInfo, permissionId: Int): Future[Boolean] = {
    userInfo.isStaff match {
      case true => Future.value(userInfo.permissions.map(u => u.id).contains(permissionId))
      case false => Future.value(false)
    }
  }

  def add(a: (Int, Int)) = {
    a._1 + a._2
  }

  def A[T](a: T)(callback: (T) => Int): Int = {
    callback(a)
  }

  A((1, 2))(add)
}
