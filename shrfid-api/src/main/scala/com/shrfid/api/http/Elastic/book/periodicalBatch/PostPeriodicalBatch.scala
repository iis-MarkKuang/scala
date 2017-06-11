package com.shrfid.api.http.Elastic.book.periodicalBatch

import com.twitter.finatra.request.Header


/**
  * Created by kuangyuan 5/12/2017
  */

case class PostPeriodicalBatchRequest(
                                      @Header Authorization: String,
                                      creator: String,
                                      periodNumber: Int,
                                      itemsNumber: Int,
                                      arrivedItemsNumber: Int,
                                      datetime: String,
                                      description: String,
                                      isActive: Boolean
                                     )