package com.shrfid.api.http.Elastic.reader.member

import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 22/12/16.
  */
case class PostReaderMemberRenewItemsRequest(@Header Authorization: String,
                                        @RouteParam id: String,
                                        bookBarcodes: Seq[String],
                                        location: String)
