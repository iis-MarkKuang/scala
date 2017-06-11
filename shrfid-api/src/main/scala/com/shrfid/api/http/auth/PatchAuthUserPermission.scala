package com.shrfid.api.http.auth

import com.shrfid.api.domains.auth.IdUpdating
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 10/10/16.
  */
case class PatchAuthUserPermissionRequest(@Header Authorization: String,
                                          @RouteParam id: Int,
                                          permissionIds: IdUpdating,
                                          groupIds: IdUpdating)
