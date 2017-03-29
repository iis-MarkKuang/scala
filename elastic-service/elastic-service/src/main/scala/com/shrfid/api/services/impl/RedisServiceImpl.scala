package com.shrfid.api.services.impl

import javax.inject.Singleton

import com.google.inject.Inject
import com.shrfid.api.services.RedisService
import com.shrfid.api.{Config, _}
import com.twitter.finagle.redis.Client
import com.twitter.io.Buf
import com.twitter.util.Future
/**
  * Created by jiejin on 6/09/2016.
  */
@Singleton
object RedisServiceImpl

@Singleton
class RedisServiceImpl @Inject()(redisClient: Client) extends RedisService {

  private def get(key: String): Future[String] = {
    redisClient.get(Buf.Utf8(key)) flatMap {
      case None => Future.value(NotFound)
      case Some(Buf.Utf8(str)) => Future.value(str)
    }
  }

  private def mGet(keys: Seq[String]): Future[Seq[String]] = {
    redisClient.mGet(keys.map(key => Buf.Utf8(key))).flatMap(a =>
      Future.value(a.map {
        case None => NotFound
        case Some(Buf.Utf8(str)) => str
      }.filterNot(s => s == Empty)))
  }

  private def set(key: String, value: String): Future[Unit] = {
    redisClient.set(Buf.Utf8(key), Buf.Utf8(value))
  }

  private def setEx(key: String, seconds: Long, value: String): Future[Unit] = {
    redisClient.setEx(Buf.Utf8(key), seconds, Buf.Utf8(value))
  }

  private def setExXx(key: String, seconds: Long, value: String): Future[Boolean] = {
    val result = redisClient.setExXx(Buf.Utf8(key), seconds,
      Buf.Utf8(value))
    result.flatMap(Future.value(_))
  }

  private def setExNx(key: String, seconds: Long, value: String): Future[Boolean] = {
    val result = redisClient.setExNx(Buf.Utf8(key), seconds,
      Buf.Utf8(value))
    result.flatMap(Future.value(_))
  }

  override def setToken(token: String, value: String): Future[Unit] = {
    setEx(token, Config.tokenExpirationDelta, value)
  }

  override def getUserInfo(token: String): Future[String] = {
    get(token)
  }

  override def reserve(barcode: String, readerId: String): Future[Unit] = {
    setEx(barcode, Config.reserveExpirationDelta, readerId)
  }
}
