package com.shrfid.api

import com.typesafe.config.ConfigFactory

/**
  * Created by jiejin on 16/02/2016.
  */
object Config {
  private val configPath = "com.shrfid.api.config"
  lazy val conf = ConfigFactory.load().getConfig(configPath)

  lazy val name: String = conf.getString("name")
  lazy val address: String = conf.getString("address")
  lazy val port: String = conf.getString("port")
  lazy val disableAdmin: Boolean = conf.getBoolean("disableAdminHttpServer")
  lazy val isTraceEnabled: Boolean = conf.getBoolean("isTraceEnabled")
  lazy val proxy: String = conf.getString("proxy")

  lazy val thriftRetryTimeout = conf.getInt("thrift.retry.timeout")
  lazy val thriftRetryStart = conf.getInt("thrift.retry.start")
  lazy val thriftRetryMultiplier = conf.getInt("thrift.retry.multiplier")
  lazy val thriftRetryRetries = conf.getInt("thrift.retry.retries")

  lazy val slickDb = conf.getConfig("slick.db")

  lazy val redisDb: String = conf.getString("redis.url")
  lazy val tokenExpirationDelta: Long = conf.getLong("redis.tokenExpirationDelta")
  lazy val cacheExpirationDelta: Long = conf.getLong("redis.cacheExpirationDelta")
  lazy val reserveExpirationDelta: Long = conf.getLong("redis.reserveExpirationDelta")

  lazy val elasticUrl: String = conf.getString("elastic.url")

  lazy val jwtSecretKey: String = conf.getString("jwt.secretKey")
  lazy val jwtIssuer: String = conf.getString("jwt.issuer")

  lazy val defaultPassword: String = conf.getString("defaultPassword")

  lazy val profileDir: String = conf.getString("profileDir")

  lazy val writingMethod = conf.getStringList("writingMethod")
  lazy val language = conf.getStringList("language")

  lazy val clcLength = conf.getInt("clcLength")
}
