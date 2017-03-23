package com.shrfid.api.modules

import com.shrfid.api.services.impl._
import com.shrfid.api.services._
import com.twitter.inject.TwitterModule

/**
  * Created by jiejin on 18/02/2016.
  */
object ServicesModule extends TwitterModule {

  override val modules = Seq(RedisServiceModule, SlickDatabaseModule, Elastic4SModule, DouBanModule)

  override def configure: Unit = {
//    bind[MysqlService].to[MysqlServiceImpl]
//    bind[RedisService].to[RedisServiceImpl]
//    bind[TokenService].to[TokenServiceImpl]
    bind[ElasticService].to[ElasticServiceImpl]
//    bind[DouBanService].to[DouBanServiceImpl]
  }
}
