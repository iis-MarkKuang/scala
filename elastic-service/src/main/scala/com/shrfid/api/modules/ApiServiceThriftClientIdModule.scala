package com.shrfid.api.modules

import com.google.inject.{Provides, Singleton}
import com.shrfid.api.Config
import com.twitter.finagle.thrift.ClientId
import com.twitter.inject.TwitterModule

/**
  * Created by jiejin on 28/01/2016.
  */
object ApiServiceThriftClientIdModule extends TwitterModule {
  private val clientIdFlag = flag("api.thrift.clientId", Config.name, "API service Thrift client id")

  @Provides
  @Singleton
  def providesClientId: ClientId = ClientId(clientIdFlag())
}

