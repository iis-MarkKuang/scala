package com.shrfid.api.http.Elastic.vendor.order

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 13/11/16.
  */
case class GetVendorOrderListRequest(@Header Authorization: String,
                                     @QueryParam limit: Int = 100,
                                     @QueryParam offset: Int = 0,
                                     @QueryParam id: Option[String],
                                     @QueryParam vendorId: Option[String],
                                     @QueryParam isbn: Option[String],
                                     @QueryParam orderDate: Option[String],
                                     @QueryParam createAt: Option[String],
                                     @QueryParam isArrived: Option[Boolean],
                                     @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering) {


  //override protected def requestPath= s"""$path?${optionQueryToUrl(id, "id")}${optionQueryToUrl(vendorId, "vendor_id")}${optionQueryToUrl(isbn, "isbn")}${optionQueryToUrl(orderDate, "order_date")}${optionQueryToUrl(createAt, "create_at")}${optionQueryToUrl(isArrived, "is_arrived")}${optionQueryToUrl(ordering,"ordering")}"""

  val idQueryParam = UrlQueryParam("_id", id)
  val vendorQueryParam = UrlQueryParam("vendor", vendorId)
  val isbnQueryParam = UrlQueryParam("isbn", isbn)
  val orderDateQueryParam = UrlQueryParam("order_date", orderDate)
  val createAtQueryParam = UrlQueryParam("create_at", createAt)
  val isArrivedQueryParam = UrlQueryParam("is_arrived", isArrived)


  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    vendorQueryParam.matchPhraseQ,
    isbnQueryParam.matchPhraseQ,
    orderDateQueryParam.rangeQ,
    createAtQueryParam.rangeQ,
    isArrived match {
      case Some(bool) => bool match {
        case false => UrlQueryParam("arrive_at", Some("^")).matchQ
        case _ => None
      }
      case None => None
    }
  ), not = Seq(isArrived match {
    case Some(bool) => bool match {
      case true => UrlQueryParam("arrive_at", Some("^")).matchQ
      case _ => None
    }
    case None => None
  }))

  lazy val path = Router.VendorOrder.list
  override val requestPath: String = _requestPath(Seq(
    idQueryParam, vendorQueryParam, isbnQueryParam,
    orderDateQueryParam, createAtQueryParam, isArrivedQueryParam
  ))
}

case class GetVendorOrderByIdRequest(@Header Authorization: String,
                                     @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)

