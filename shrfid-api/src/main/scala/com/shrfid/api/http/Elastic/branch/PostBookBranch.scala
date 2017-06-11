package com.shrfid.api.http.Elastic.branch

import javax.inject.Inject

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.NotEmpty

/**
  * Created by jiejin on 25/11/16.
  */
case class PostBookBranchRequest @Inject() (@Header Authorization: String,
                                 @NotEmpty name: String,
                                 isActive: Boolean = true,
                                 isRoot: Boolean = false,
                                 address: String = "",
                                 telephone: String = "",
                                 description: String = "") {

}
