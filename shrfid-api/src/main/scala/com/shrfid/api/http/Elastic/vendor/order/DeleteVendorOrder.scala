package com.shrfid.api.http.Elastic.vendor.order

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 13/11/16.
  */

case class DeleteVendorOrderByIdRequest(@Header Authorization: String,
                                        @RouteParam id: String) extends BaseDeleteById(Authorization, id)


case class DeleteVendorOrderBulkRequest(@Header Authorization: String,
                                        ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)
