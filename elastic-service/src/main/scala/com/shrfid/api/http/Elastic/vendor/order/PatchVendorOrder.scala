package com.shrfid.api.http.Elastic.vendor.order

import java.sql.Date

import com.shrfid.api._
import com.twitter.finatra.request.Header
import org.joda.time.DateTime

/**
  * Created by jiejin on 13/11/16.
  */
case class PatchRequest(title: Option[String],
                        author: Option[String],
                        publisher: Option[String],
                        isbn: Option[String],
                        price: Option[BigDecimal],
                        quantity: Option[Int],
                        total: Option[BigDecimal],
                        actual_price: Option[BigDecimal],
                        actual_quantity: Option[Int],
                        actual_total: Option[BigDecimal],
                        order_date: Option[String],
                        arrive_at: Option[String],
                        description: Option[String],
                        vendor_id: Option[String],
                        datetime: String = Time.now.toString())

case class PatchVendorOrderRow(id: String,
                               title: Option[String],
                               author: Option[String],
                               publisher: Option[String],
                               isbn: Option[String],
                               price: Option[BigDecimal],
                               quantity: Option[Int],
                               total: Option[BigDecimal],
                               actualPrice: Option[BigDecimal],
                               actualQuantity: Option[Int],
                               actualTotal: Option[BigDecimal],
                               orderDate: Option[Date],
                               arriveAt: Option[DateTime],
                               description: Option[String],
                               vendorId: Option[String]) {


  def patchRequest = {
    getCCParams(PatchRequest(title, author, publisher, isbn, price, quantity, total, actualPrice, actualQuantity,
      actualTotal, orderDate match {
        case Some(d) => Some(d.toString)
        case None => None
      }, arriveAt match {
        case Some(d) => Some(d.toString)
        case None => None
      }, description, vendorId), Seq("id"))

  }
}


case class PatchVendorOrderBulkRequest(@Header Authorization: String,
                                       data: Seq[PatchVendorOrderRow])
