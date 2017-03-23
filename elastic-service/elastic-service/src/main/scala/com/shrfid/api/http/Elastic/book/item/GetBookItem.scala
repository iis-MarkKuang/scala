package com.shrfid.api.http.Elastic.book.item

import com.shrfid.api._
import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 11/11/16.
  */
case class GetBookItemListRequest(@Header Authorization: String,
                                  @QueryParam limit: Int = 100,
                                  @QueryParam offset: Int = 0,
                                  @QueryParam id: Option[String],
                                  @QueryParam reference: Option[String],
                                  @QueryParam title: Option[String],
                                  @QueryParam barcode: Option[String],
                                  @QueryParam rfid: Option[String],
                                  @QueryParam categoryId: Option[Int],
                                  @QueryParam stackId: Option[String],
                                  @QueryParam clc: Option[String],
                                  @QueryParam isAvailable: Option[Boolean],
                                  @QueryParam isActive: Option[Boolean],
                                  @QueryParam ordering: Option[String]) extends BaseGetListRequest(Authorization, limit, offset, ordering) {


  val idQueryParam = UrlQueryParam("_id", id)
  val referenceQueryParam = UrlQueryParam("reference", reference)
  val titleQueryParam = UrlQueryParam("title", title)
  val barcodeQueryParam = UrlQueryParam("barcode", barcode)
  val rfidQueryParam = UrlQueryParam("rfid", rfid)
  val categoryQueryParam = UrlQueryParam("category", categoryId match {
    case None => None
    case Some(c) => Some(bookCategory(c))
  })
  val stackQueryParam = UrlQueryParam("stack", stackId)
  val clcQueryParam = UrlQueryParam("clc", clc)
  val isAvailableQueryParam = UrlQueryParam("is_available", isAvailable)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)


  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    referenceQueryParam.matchPhraseQ,
    titleQueryParam.matchPhraseQ,
    barcodeQueryParam.matchPhraseQ,
    rfidQueryParam.matchPhraseQ,
    categoryQueryParam.matchPhraseQ,
    stackQueryParam.matchPhraseQ,
    clcQueryParam.matchPhraseQ,
    isAvailableQueryParam.matchPhraseQ,
    isActiveQueryParam.matchPhraseQ
  ))

  override val path = Router.Book.items
  override val requestPath: String = _requestPath(Seq(idQueryParam, referenceQueryParam, titleQueryParam,
    barcodeQueryParam, rfidQueryParam, categoryQueryParam, stackQueryParam, clcQueryParam, isAvailableQueryParam,
    isActiveQueryParam
  ))
}

case class GetBookItemByIdRequest(@Header Authorization: String,
                                  @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)
