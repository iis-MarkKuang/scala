package com.shrfid.api.http.Elastic.stack

import com.shrfid.api._
import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, RouteParam}

/**
  * Created by jiejin on 9/10/16.
  */
case class PatchBookStackByIdRequest(@Header Authorization: String,
                                     @RouteParam id: String,
                                     name: Option[String],
                                     isActive: Option[Boolean],
                                     branchId: Option[String],
                                     description: Option[String],
                                     datetime: String = Time.now.toString)
  extends BasePatchByIdRequest(Authorization, id, datetime) {

  override def patchRequest = {
    val a = getCCParams(this, Seq("id", "Authorization", "branchId"))
    branchId match {
      case Some(b) => a + ("branch" -> b)
      case None => a
    }
  }


}


