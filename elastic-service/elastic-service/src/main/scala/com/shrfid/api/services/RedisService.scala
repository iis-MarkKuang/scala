package com.shrfid.api.services

import com.twitter.util.Future

/**
  * Created by jiejin on 6/09/2016.
  */
trait RedisService {
  def setToken(token: String, value: String): Future[Unit]

  def getUserInfo(token: String): Future[String]

}
