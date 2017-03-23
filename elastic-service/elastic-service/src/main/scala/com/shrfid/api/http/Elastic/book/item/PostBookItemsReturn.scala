package com.shrfid.api.http.Elastic.book.item

import com.twitter.finatra.request.Header

/**
  * Created by jiejin on 8/11/16.
  */
case class PostBookItemsReturnRequest(@Header Authorization: String,
                                      bookBarcodes: Seq[String],
                                      location: String)
