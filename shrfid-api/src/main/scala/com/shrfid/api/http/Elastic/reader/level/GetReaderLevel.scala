package com.shrfid.api.http.Elastic.reader.level

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 13/09/2016.
  */
case class GetReaderLevelListRequest(@Header Authorization: String,
                                     @QueryParam limit: Int = 100,
                                     @QueryParam offset: Int = 0,
                                     @QueryParam id: Option[String],
                                     @QueryParam name: Option[String],
                                     @QueryParam isActive: Option[Boolean],
                                     @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering) {

  val idQueryParam = UrlQueryParam("_id", id)
  val nameQueryParam = UrlQueryParam("name", name)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    nameQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ
  ))

  override val path: String = Router.ReaderLevel.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, nameQueryParam, isActiveQueryParam
  ))
}

case class GetReaderLevelByIdRequest(@Header Authorization: String,
                                     @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)
