package com.shrfid.api.http.Elastic.vendor.member

import com.twitter.finatra.request.Header

/**
  * Created by jiejin on 6/11/16.
  */
case class PostVendorMemberRequest(@Header Authorization: String,
                                   name: String,
                                   isActive: Boolean = true,
                                   description: String = "")
