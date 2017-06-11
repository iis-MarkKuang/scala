package com.shrfid.api.modules

/**
  * Created by jiejin on 6/09/2016.
  */

import com.google.inject.{Provides, Singleton}
import com.shrfid.api.Config
import com.twitter.finagle.redis.Client
import com.twitter.inject.TwitterModule

object RedisServiceModule extends TwitterModule {

  val redisUrl = flag("redis.url", Config.redisDb, "Default redis url")

  @Singleton
  @Provides
  def providesRedisReaderClient(): Client = {
    //RedisCluster.start(1)
    //Client(RedisCluster.hostAddresses())
    Client(redisUrl())
  }
}
