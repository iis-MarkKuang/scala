package com.shrfid.api.http.Elastic.branch

import com.shrfid.api._
import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 2/12/16.
  */

case class PatchBookBranchByIdRequest(@Header Authorization: String,
                                      @RouteParam id: String,
                                      name: Option[String],
                                      isActive: Option[Boolean],
                                      isRoot: Option[Boolean],
                                      description: Option[String],
                                      datetime: String = Time.now.toString)
  extends BasePatchByIdRequest(Authorization, id, datetime)

