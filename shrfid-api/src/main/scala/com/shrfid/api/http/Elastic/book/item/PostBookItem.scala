package com.shrfid.api.http.Elastic.book.item

import com.shrfid.api.domains.book.BookReference
import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}
import com.twitter.util.Future

/**
  * Created by jiejin on 21/11/16.
  */

// Modified by kuangyuan 5/18/2017
// Modified by kuangyuan 5/23/2017
case class PostBookItemsRequest(@Header Authorization: String,
                                barcodeString: String,
                                rfid: Option[String],
                                reference: Option[String],
                                stackId: String,
                                info: Option[BookReference],
                                categoryId: Int = 1,
                                solicitedPeriodical: String = "",
                                serialNumber: Int = -1) {
  @MethodValidation
  def validateCategoryId = {
    ValidationResult.validate(
      Seq(1, 2, 3, 4).contains(categoryId) && barcodeString != "",
      "条码号字符串不能为空，且categoryId must be 1, 2, 3 or 4. 1为普通图书, 2为期刊, 3为古籍, 4为非书资料")
  }

  def validateReference = {
    ValidationResult.validate(
      reference.isDefined || info.isDefined,
      "Either reference or info need to be provided")
  }


}

object PostBookItemsRequest {
  def toDomain(request: PostBookItemBindRequest): PostBookItemsRequest = {
    PostBookItemsRequest(request.Authorization, request.barcode, request.rfid, request.reference, request.stackId, request.info, request.categoryId, request.solicitedPeriodical)
  }
}

// Added by kuangyuan 5/6/2017
case class PostBookItemBindRequest(@Header Authorization: String,
                                   periodicalBarcodesStr: String,
                                   barcode: String,
                                   rfid: Option[String],
                                   reference: Option[String],
                                   stackId: String,
                                   info: Option[BookReference],
                                   categoryId: Int = 5,
                                   solicitedPeriodical: String = ""
                                  ) {

  @MethodValidation
  def validateReference = {
    ValidationResult.validate(
      (reference.isDefined || info.isDefined) && categoryId == 5,
      "Either reference or info need to be provided and category need to be periodical bound book"
    )
  }
}
