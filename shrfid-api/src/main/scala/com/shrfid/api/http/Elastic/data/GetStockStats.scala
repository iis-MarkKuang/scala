package com.shrfid.api.http.Elastic.data

import javax.inject.Inject

import com.twitter.finatra.request.Header

/**
  * Created by kuang on 2017/3/31.
  */
case class GetStockStatisticsRequest @Inject() (@Header Authorization: String)