package com.shrfid.api.http.Elastic.book.solicitedPeriodicals

import com.shrfid.api.Time
import com.twitter.finatra.request.Header

/**
  * Created by kuangyuan 5/9/2017
  */

case class PostSolicitedRequest(
                                 @Header Authorization: String,
                                 title: String,
                                 period: String,
                                 ISSN: String,
                                 publisher: String,
                                 solicitedDate: String,
                                 stack: String,
                                 batch: String,
                                 price: Double,
                                 lastSerial: Int,
                                 isActive: Boolean,
                                 isSubscribed: Boolean,
                                 description: String = "",
                                 datetime: String = Time.now.toString
                               )


