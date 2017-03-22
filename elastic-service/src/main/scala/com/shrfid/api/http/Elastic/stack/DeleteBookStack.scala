package com.shrfid.api.http.Elastic.stack

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 9/10/16.
  */
case class DeleteBookStackByIdRequest(@Header Authorization: String,
                                      @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteBookStackBulkRequest(@Header Authorization: String,
                                      ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)