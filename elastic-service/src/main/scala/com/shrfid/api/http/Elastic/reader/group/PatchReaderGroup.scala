package com.shrfid.api.http.Elastic.reader.group

import com.shrfid.api.Time
import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 10/10/16.
  */
case class PatchReaderGroupByIdRequest(@Header Authorization: String,
                                       @RouteParam id: String,
                                       name: Option[String],
                                       isActive: Option[Boolean],
                                       description: Option[String],
                                       datetime: String = Time.now.toString)
  extends BasePatchByIdRequest(Authorization, id, datetime)