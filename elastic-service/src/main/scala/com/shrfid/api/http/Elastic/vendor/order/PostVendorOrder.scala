package com.shrfid.api.http.Elastic.vendor.order

import java.sql.Date

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 11/11/16.
  */

case class PostVendorOrderRequest(@Header Authorization: String,
                                  title: String,
                                  author: String,
                                  publisher: String,
                                  isbn: String,
                                  price: Double,
                                  quantity: Int,
                                  total: Double,
                                  description: String = "",
                                  vendorId: String,
                                  orderDate: Date)


case class PutVendorOrderRow(title: String,
                             author: String,
                             publisher: String,
                             isbn: String,
                             price: Double,
                             quantity: Int,
                             total: Double,
                             description: String = "",
                             vendorId: String,
                             orderDate: Date)

case class PostVendorOrderBulkRequest(@Header Authorization: String,
                                      data: Seq[PutVendorOrderRow]) {


  @MethodValidation
  def validateNonEmpty = {
    ValidationResult.validate(
      data.nonEmpty,
      "data cannot be empty")
  }
}
