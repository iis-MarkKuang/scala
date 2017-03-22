package com.shrfid.api.domains.vendor

import com.shrfid.api._
import com.shrfid.api.http.Elastic.vendor.order.{PostVendorOrderBulkRequest, PostVendorOrderRequest}
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 14/11/16.
  */


object VendorOrder {
  implicit val VendorOrderFmt = Json.format[VendorOrder]

  def toDomain(request: PostVendorOrderRequest, user: Username) = {
    Future.value(VendorOrder(request.title, request.author, request.publisher, request.isbn.replace("-", ""), request.price, request.quantity, request.total,
      0.00, 0, 0.00, request.orderDate.toString, Time.now.toString, "", request.description, request.vendorId, user))
  }

  def toDomain(request: PostVendorOrderBulkRequest, user: Username) = {
    Future.value(request.data.map(i => VendorOrder(i.title, i.author, i.publisher, i.isbn.replace("-", ""), i.price, i.quantity, i.total,
      0.00, 0, 0.00, i.orderDate.toString, Time.now.toString, "", i.description, i.vendorId, user)))
  }
}

//VendorOrderEntity(0, o.title, o.author, o.publisher,
//o.isbn.replace("-", ""), o.price, o.quantity, o.total, None, None, None, orderDate, None, Time.now, Time.now,
//o.description, vendorId, userInfo.id))
case class VendorOrder(title: String,
                       author: String,
                       publisher: String,
                       isbn: String,
                       price: BigDecimal,
                       quantity: Int,
                       total: BigDecimal,
                       actual_price: BigDecimal,
                       actual_quantity: Int,
                       actual_total: BigDecimal,
                       order_date: String,
                       create_at: String,
                       arrive_at: String,
                       description: String,
                       vendor: String,
                       user: String,
                       datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object VendorOrderNested {
  implicit val fmt = Json.format[VendorOrderNested]

  def toDomain(id: String, vendorOrder: VendorOrder, vendor: Seq[VendorMemberWithId]): VendorOrderNested = {

    VendorOrderNested(id, vendorOrder.title, vendorOrder.author, vendorOrder.publisher, vendorOrder.isbn, vendorOrder.price,
      vendorOrder.quantity, vendorOrder.total, vendorOrder.actual_price, vendorOrder.actual_quantity, vendorOrder.actual_total,
      vendorOrder.order_date, vendorOrder.create_at, vendorOrder.arrive_at, vendorOrder.description, vendor.filter(_.id == vendorOrder.vendor).head, vendorOrder.user, vendorOrder.datetime)


  }
}

case class VendorOrderNested(id: String,
                             title: String,
                             author: String,
                             publisher: String,
                             isbn: String,
                             price: BigDecimal,
                             quantity: Int,
                             total: BigDecimal,
                             actual_price: BigDecimal,
                             actual_quantity: Int,
                             actual_total: BigDecimal,
                             order_date: String,
                             create_at: String,
                             arrive_at: String,
                             //updateAt: String,
                             description: String,
                             vendor: VendorMemberWithId,
                             user: String,
                             datetime: String) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}
