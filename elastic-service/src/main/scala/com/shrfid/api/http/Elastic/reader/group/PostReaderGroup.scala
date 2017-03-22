package com.shrfid.api.http.Elastic.reader.group

import com.twitter.finatra.request.Header

/**
  * Created by jiejin on 10/10/16.
  */
case class PostReaderGroupRequest(@Header Authorization: String,
                                  name: String,
                                  isActive: Boolean = true,
                                  description: String = "")
