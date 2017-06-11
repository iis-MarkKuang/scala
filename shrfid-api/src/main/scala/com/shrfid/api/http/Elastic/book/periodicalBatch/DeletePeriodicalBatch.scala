package com.shrfid.api.http.Elastic.book.periodicalBatch

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

// Created by kuangyuan 5/11/2017

case class DeletePeriodicalBatchByIdRequest(@Header Authorization: String,
                                      @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeletePeriodicalBatchBulkRequest(@Header Authorization: String,
                                            ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)