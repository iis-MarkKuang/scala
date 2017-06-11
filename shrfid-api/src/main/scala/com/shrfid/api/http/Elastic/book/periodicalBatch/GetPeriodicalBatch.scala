package com.shrfid.api.http.Elastic.book.periodicalBatch

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}


// Created by kuangyuan 5/11/2017

case class GetPeriodicalBatchListRequest(@Header Authorization: String,
                                         @QueryParam limit: Int = 100,
                                         @QueryParam offset: Int = 0,
                                         @QueryParam id: Option[String],
                                         @QueryParam creator: Option[String],
                                         @QueryParam isActive: Option[String],
                                         @QueryParam ordering: Option[String],
                                         @QueryParam datetime: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering) {

  val idQueryParam = UrlQueryParam("_id", id)
  val creatorQueryParam = UrlQueryParam("creator", creator)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    creatorQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ
  ))

  val path = Router.PeriodicalBatch.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, creatorQueryParam, isActiveQueryParam
  ))
}

case class GetPeriodicalBatchByIdRequest(@Header Authorization: String,
                                         @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)