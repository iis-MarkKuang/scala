package com.shrfid.api.http.Elastic.book.reservation

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by xwj on 14/4/17.
  */
case class GetBookReservationListRequest(@Header Authorization: String,
                                       @QueryParam limit: Int = 100,
                                       @QueryParam offset: Int = 0,
                                       @QueryParam id: Option[String],
                                         @QueryParam barcode: Option[String],
                                       @QueryParam title: Option[String],
                                       @QueryParam rSDate: Option[String],
                                         @QueryParam rEDate: Option[String],
                                       @QueryParam isbn: Option[String],
                                         @QueryParam status: Option[String],
                                       @QueryParam ordering: Option[String]
                                      ) extends BaseGetListRequest(Authorization, limit, offset, ordering) {

  val rIDQueryParam = UrlQueryParam("reader.id", id, "id")
  val bCodeQueryParam = UrlQueryParam("book.barcode", barcode, "barcode")
  val titleQueryParam = UrlQueryParam("book.reference.题名与责任者.正题名", title, "title")
  val isbnQueryParam = UrlQueryParam("book.reference.ISBN.ISBN", isbn, "isbn")
  val rSDateQueryParam = UrlQueryParam("datetime", rSDate, "datetime")
  val rEDateQueryParam = UrlQueryParam("datetime", rEDate, "datetime")
  val statusQueryParam = UrlQueryParam("status", status, "status")

  override val filter = QueryFilter(must = Seq(
    rIDQueryParam.matchPhraseQ,
    bCodeQueryParam.matchPhraseQ,
    titleQueryParam.matchPhraseQ,
    isbnQueryParam.matchPhraseQ,
    rSDateQueryParam.rangeQ,
    rEDateQueryParam.rangeQ,
    statusQueryParam.matchPhraseQ
  ))

  val path = Router.Reservation.list

  override val requestPath: String = _requestPath(Seq(rIDQueryParam,bCodeQueryParam,
    titleQueryParam, isbnQueryParam,rSDateQueryParam,rEDateQueryParam,statusQueryParam))
}

case class GetBookReservationById (@Header Authorization: String,
                                 @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)

