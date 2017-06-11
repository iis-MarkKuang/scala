package com.shrfid.api.http.Elastic.reader.group

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 10/10/16.
  */
case class DeleteReaderGroupByIdRequest(@Header Authorization: String,
                                        @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteReaderGroupBulkRequest(@Header Authorization: String,
                                        ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)
