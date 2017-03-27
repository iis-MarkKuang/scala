package com.shrfid.api.http.Elastic.reader.member

import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by kuang on 2017/3/27.
  */
case class PostReaderMemberReserveItemRequest(@Header Authorization: String,
                                        @RouteParam id: String,
                                        bookBarcode: String,
                                        location: String)
