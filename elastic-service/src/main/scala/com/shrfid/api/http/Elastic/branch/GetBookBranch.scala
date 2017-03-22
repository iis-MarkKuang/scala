package com.shrfid.api.http.Elastic.branch

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 25/11/16.
  */

case class GetBookBranchListRequest(@Header Authorization: String,
                                    @QueryParam limit: Int = 100,
                                    @QueryParam offset: Int = 0,
                                    @QueryParam id: Option[String],
                                    @QueryParam name: Option[String],
                                    @QueryParam isActive: Option[Boolean],
                                    @QueryParam isRoot: Option[Boolean],
                                    @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering) {


  val idQueryParam = UrlQueryParam("_id", id)
  val nameQueryParam = UrlQueryParam("name", name)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)
  val isRootQueryParam = UrlQueryParam("is_root", isRoot)

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    nameQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ,
    isRootQueryParam.matchPhraseQ
  ))

  val path = Router.Branch.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, nameQueryParam, isActiveQueryParam, isRootQueryParam
  ))
}

case class GetBookBranchByIdRequest(@Header Authorization: String,
                                    @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)
