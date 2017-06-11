package com.shrfid.api.http.Elastic.book.periodicalBatch

import com.twitter.finatra.request.{Header, RouteParam}
import com.shrfid.api._
import com.shrfid.api.http.Elastic.BasePatchByIdRequest

/**
  * Created by kuangyuan 5/12/2017
  */

case class PatchPeriodicalBatchRequest(
                                      @Header Authorization: String,
                                      @RouteParam id: String,
                                      creator: Option[String],
                                      periodNumber: Option[Int],
                                      itemsNumber: Option[Int],
                                      arrivedItemsNumber: Option[Int],
                                      datetime: String = Time.now.toString,
                                      description: Option[String],
                                      isActive: Option[Boolean]
                                      ) extends BasePatchByIdRequest(Authorization, id, datetime)