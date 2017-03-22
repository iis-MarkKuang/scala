package com.shrfid.api.http.auth

import com.twitter.finatra.request.RouteParam

/**
  * Created by jiejin on 9/12/16.
  */

case class DocsRequest(@RouteParam file: String)