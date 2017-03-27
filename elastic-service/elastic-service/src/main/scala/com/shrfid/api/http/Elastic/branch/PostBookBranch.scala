package com.shrfid.api.http.Elastic.branch

import javax.inject.Inject

import com.elastic_service.requestStructs.PostBookBranchRequestThrift
import com.twitter.finatra.conversions.option
import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.NotEmpty
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 25/11/16.
  */
object PostBookBranchRequest {
  implicit val fmt = Json.format[PostBookBranchRequest]

  def wrap(requestThrift: PostBookBranchRequestThrift): PostBookBranchRequest = {
    PostBookBranchRequest(
      requestThrift.authorization,
      requestThrift.name,
      requestThrift.isActive,
      requestThrift.isRoot,
      requestThrift.description
    )
  }
}


case class PostBookBranchRequest @Inject() (@Header Authorization: String,
                                 @NotEmpty name: String,
                                 isActive: Boolean = true,
                                 isRoot: Boolean = false,
                                 description: String = "") {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}
