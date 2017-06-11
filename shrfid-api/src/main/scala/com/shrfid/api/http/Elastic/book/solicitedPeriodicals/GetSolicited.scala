package com.shrfid.api.http.Elastic.book.solicitedPeriodicals

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

// Created by kuangyuan 5/9/2017

case class GetSolicitedListRequest(@Header Authorization: String,
                                   @QueryParam limit: Int = 100,
                                   @QueryParam offset: Int = 0,
                                   @QueryParam id: Option[String],
                                   @QueryParam issn: Option[String],
                                   @QueryParam title: Option[String],
                                   @QueryParam batch: Option[String],
                                   @QueryParam publisher: Option[String],
                                   @QueryParam isActive: Option[Boolean],
                                   @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering){


  val idQueryParam = UrlQueryParam("_id", id)
  val issnQueryParam = UrlQueryParam("issn", issn)
  val titleQueryParam = UrlQueryParam("title", title)
  val batchQueryParam = UrlQueryParam("batch", batch)
  val pubQueryParam = UrlQueryParam("publisher", publisher)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    issnQueryParam.matchPhraseQ,
    titleQueryParam.matchPhraseQ,
    batchQueryParam.matchPhraseQ,
    pubQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ
  ))

  val path = Router.Branch.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, issnQueryParam, titleQueryParam, batchQueryParam, pubQueryParam, isActiveQueryParam
  ))
}

case class GetSolicitedByIdRequest(@Header Authorization: String,
                                   @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)

// Added by kuangyuan 5/23/2017
case class GetSolicitedLateRequest(@Header Authorization: String)

