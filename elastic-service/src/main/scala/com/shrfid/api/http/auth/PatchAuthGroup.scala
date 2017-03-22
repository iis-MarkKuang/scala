package com.shrfid.api.http.auth

import com.shrfid.api.domains.auth.IdUpdating
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 8/10/16.
  */

case class PatchAuthGroupRequest(@Header Authorization: String,
                                 @RouteParam id: Int,
                                 name: String,
                                 permissionIds: IdUpdating)

