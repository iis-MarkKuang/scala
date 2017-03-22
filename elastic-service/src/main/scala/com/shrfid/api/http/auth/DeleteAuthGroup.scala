package com.shrfid.api.http.auth

import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 9/10/16.
  */
case class DeleteAuthGroupRequest(@Header Authorization: String,
                                  @RouteParam id: Int)
