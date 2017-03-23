package com.shrfid.api.http.auth

import com.twitter.finatra.request.{Header, RouteParam}
import com.twitter.finatra.validation._

/**
  * Created by jiejin on 9/09/2016.
  */
case class PatchAuthUserPasswordForceRequest(@Header Authorization: String,
                                             @RouteParam id: Int,
                                             @Size(min = 1, max = 128) password: String)

case class PatchAuthUserPasswordRequest(@Header Authorization: String,
                                        @Size(min = 1, max = 128) oldPassword: String,
                                        @Size(min = 1, max = 128) newPassword: String)