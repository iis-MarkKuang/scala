package com.shrfid.api.http.Elastic.vendor.member

import com.shrfid.api.http.Elastic.{BaseDeleteBulk, BaseDeleteById}
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 6/11/16.
  */
case class DeleteVendorMemberByIdRequest(@Header Authorization: String,
                                         @RouteParam id: String) extends BaseDeleteById(Authorization, id)

case class DeleteVendorMemberBulkRequest(@Header Authorization: String,
                                         ids: Seq[String]) extends BaseDeleteBulk(Authorization, ids)
