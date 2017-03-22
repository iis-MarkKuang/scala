package com.shrfid.api.http.Elastic.reader.level

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 28/9/16.
  */
case class DeleteReaderLevelByIdRequest(@Header Authorization: String,
                                        @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteReaderLevelBulkRequest(@Header Authorization: String,
                                        ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)
