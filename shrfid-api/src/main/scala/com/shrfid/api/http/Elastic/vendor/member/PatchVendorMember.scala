package com.shrfid.api.http.Elastic.vendor.member

import com.shrfid.api._
import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 6/11/16.
  */
case class PatchVendorMemberByIdRequest(@Header Authorization: String,
                                        @RouteParam id: String,
                                        name: Option[String],
                                        isActive: Option[Boolean],
                                        description: Option[String],
                                        datetime: String = Time.now.toString)
  extends BasePatchByIdRequest(Authorization, id, datetime) {

}
