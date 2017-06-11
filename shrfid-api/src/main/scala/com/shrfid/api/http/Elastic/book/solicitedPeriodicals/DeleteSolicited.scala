package com.shrfid.api.http.Elastic.book.solicitedPeriodicals

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by kuangyuan on 5/9/2017.
  */
case class DeleteSolicitedByIdRequest(@Header Authorization: String,
                                       @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteSolicitedBulkRequest(@Header Authorization: String,
                                       ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)

