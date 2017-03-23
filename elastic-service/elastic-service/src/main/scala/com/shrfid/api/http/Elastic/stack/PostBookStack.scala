package com.shrfid.api.http.Elastic.stack

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.NotEmpty

/**
  * Created by jiejin on 9/10/16.
  */
case class PostBookStackRequest(@Header Authorization: String,
                                @NotEmpty name: String,
                                isActive: Boolean = true,
                                @NotEmpty branchId: String,
                                description: String = "")

