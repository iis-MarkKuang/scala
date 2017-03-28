package com.shrfid.api.http.Elastic.book.item

import com.shrfid.api.domains.readable.BookReference
import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 21/11/16.
  */

case class PostBookItemRequest(@Header Authorization: String,
                               barcode: String,
                               rfid: Option[String],
                               reference: Option[String],
                               stackId: String,
                               info: Option[BookReference],
                               categoryId: Int = 1) {
  @MethodValidation
  def validateCategoryId = {
    ValidationResult.validate(
      Seq(1, 2, 3, 4).contains(categoryId),
      "categoryId must be 1, 2, 3 or 4. 1为普通图书, 2为期刊, 3为古籍, 4为非书资料")
  }



  def validateReference = {
    ValidationResult.validate(
      reference.isDefined || info.isDefined,
      "Either reference or info need to be provided")
  }

}
