package com.shrfid.api.http.Elastic.stack

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 25/11/16.
  */
case class GetBookStackListRequest(@Header Authorization: String,
                                   @QueryParam limit: Int = 100,
                                   @QueryParam offset: Int = 0,
                                   @QueryParam id: Option[String],
                                   @QueryParam name: Option[String],
                                   @QueryParam branch: Option[String],
                                   @QueryParam isActive: Option[Boolean],
                                   @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering){

  val idQueryParam = UrlQueryParam("_id", id)
  val nameQueryParam = UrlQueryParam("name", name)
  val branchQueryParam = UrlQueryParam("branch", branch)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)


  //override protected def requestPath: String = s"""$path?${optionQueryToUrl(id, "id")}${optionQueryToUrl(name,"name")}${optionQueryToUrl(branch,"branch")}${optionQueryToUrl(isActive,"is_active")}${optionQueryToUrl(ordering,"ordering")}"""
  override val filter = QueryFilter(must= Seq(
    idQueryParam.matchPhraseQ,
    nameQueryParam.matchPhraseQ,
    branchQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ
  ))

  lazy val path = Router.Stack.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, nameQueryParam, branchQueryParam, isActiveQueryParam
  ))
}

case class GetBookStackByIdRequest(@Header Authorization: String,
                                   @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)
