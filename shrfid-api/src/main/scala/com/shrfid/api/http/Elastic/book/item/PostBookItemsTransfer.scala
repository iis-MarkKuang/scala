package com.shrfid.api.http.Elastic.book.item

import com.twitter.finatra.request.Header

/**
  * Created by jiejin on 23/12/16.
  */
case class PostBookItemsTransferRequest(@Header Authorization: String,
                                        bookBarcodes: Seq[String],
                                        stackId: String = "",
                                        description: String)