package com.shrfid.api.services

import com.twitter.util.Future
import java.lang.Long
/**
  * Created by jiejin on 6/09/2016.
  */
trait RedisService {
  def setToken(token: String, value: String): Future[Unit]

  def getUserInfo(token: String): Future[String]

  // Added by kuang 4/20/2017
  def get_new(key: String): Future[String]

  def set_new(key: String, value: String): Future[Unit]

  def dels(keys: Seq[String]): Future[Long]
}
