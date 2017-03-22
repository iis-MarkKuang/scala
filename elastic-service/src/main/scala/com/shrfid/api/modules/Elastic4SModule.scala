package com.shrfid.api.modules

import javax.inject.Singleton

import com.google.inject.Provides
import com.shrfid.api.Config
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import com.twitter.inject.TwitterModule

/**
  * Created by jiejin on 9/11/16.
  */
object Elastic4SModule extends TwitterModule {
  type Elastic4SDatabaseSource = ElasticClient

  @Singleton
  @Provides
  def provideDatabase: Elastic4SDatabaseSource = ElasticClient.transport(ElasticsearchClientUri(Config.elasticUrl))
}
