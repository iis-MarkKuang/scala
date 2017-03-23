package com.shrfid.api.modules

import com.google.inject.{Provides, Singleton}
import com.shrfid.api.Config
import com.twitter.inject.TwitterModule

/**
  * Created by jiejin on 6/09/2016.
  */
object SlickDatabaseModule extends TwitterModule {

  import slick.driver.MySQLDriver.api._

  type SlickDatabaseSource = slick.driver.MySQLDriver.api.Database

  @Singleton
  @Provides
  def provideDatabase: SlickDatabaseSource = Database.forConfig("slick.db", Config.conf)

}
