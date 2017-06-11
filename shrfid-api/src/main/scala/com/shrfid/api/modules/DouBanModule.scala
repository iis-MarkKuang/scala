package com.shrfid.api.modules

import com.twitter.finatra.conversions.time._
import com.twitter.finatra.httpclient.modules.HttpClientModule
import com.twitter.finatra.utils.RetryPolicyUtils._
/**
  * Created by Administrator on 2017/3/13.
  */
object DouBanModule extends HttpClientModule {

  override val dest = "api.douban.com:443"

  override def sslHostname = Some("api.douban.com")

  override def retryPolicy = Some(exponentialRetry(
  start = 10.millis,
  multiplier = 2,
  numRetries = 3,
  shouldRetry = Http4xxOr5xxResponses))

  override def defaultHeaders = Map("Accept" -> "*/*",
  "Connection" -> "Keep-Alive",
  "Host" ->"api.douban.com",
  "Content-Type"-> "application/json")
}
