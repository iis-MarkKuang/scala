package com.shrfid.api.http.Elastic.branch

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 8/12/16.
  */
case class DeleteBookBranchByIdRequest(@Header Authorization: String,
                                       @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteBookBranchBulkRequest(@Header Authorization: String,
                                       ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)

