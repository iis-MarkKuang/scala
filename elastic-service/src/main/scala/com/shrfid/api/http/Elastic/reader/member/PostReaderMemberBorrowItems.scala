package com.shrfid.api.http.Elastic.reader.member

import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 7/11/16.
  */
case class PostReaderMemberBorrowItemsRequest(@Header Authorization: String,
                                              @RouteParam id: String,
                                              bookBarcodes: Seq[String],
                                              location: String)


