package com.shrfid.api.http.Elastic.reader.member

import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by  WEIJING 2017/3/31
  */
case class PostReaderMemberReservationItemsRequest(@Header Authorization: String,
                                        @RouteParam id: String,
                                        bookBarcodes: Seq[String],
                                        location: String)
