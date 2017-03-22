package com.shrfid.api.http.auth

import com.shrfid.api.domains.auth.{AuthGroup, AuthGroupNested}
import com.twitter.finatra.request.{Header, QueryParam}

/**
  * Created by jiejin on 19/09/2016.
  */
case class GetAuthGroupListRequest(@Header Authorization: String,
                                   @QueryParam limit: Int = 100,
                                   @QueryParam offset: Int = 0,
                                   @QueryParam nested: Boolean = false) extends BaseGetListRequest(limit, offset) {

  override val queryFilter: Map[String, _] = Map(
    "nested" -> nested,
    "limit" -> limit,
    "offset" -> offset)

  override protected def requestPath(path: String): String = s"""$path?nested=$nested"""

  def response(result: (Int, Seq[AuthGroup]), path: String): String = {
    Paging(count = result._1,
      prev = prev(requestPath(path), result._1),
      next = next(requestPath(path), result._1),
      content = result._2).stringify
  }

  def nestedResponse(result: (Int, Seq[AuthGroupNested]), path: String): String = {
    Paging(count = result._1,
      prev = prev(requestPath(path), result._1),
      next = next(requestPath(path), result._1),
      content = result._2).stringify
  }
}




