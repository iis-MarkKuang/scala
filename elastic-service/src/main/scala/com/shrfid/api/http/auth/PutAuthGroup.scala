package com.shrfid.api.http.auth

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.NotEmpty

/**
  * Created by jiejin on 28/9/16.
  */
case class PutAuthGroupRequest(@Header Authorization: String,
                               name: String,
                               @NotEmpty permissionIds: Seq[Int])
