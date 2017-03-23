package com.shrfid.api.http.Elastic.book.item

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 11/10/16.
  */
case class PostPreGenBookItemsRequest(@Header Authorization: String,
                                      categoryId: Int,
                                      stackId: String,
                                      quantity: Int) {
  @MethodValidation
  def validateCategoryId = {
    ValidationResult.validate(
      Seq(1, 2, 3, 4).contains(categoryId),
      "categoryId must be 1, 2, 3 or 4. 1为普通图书, 2为期刊, 3为古籍, 4为非书资料")
  }

  @MethodValidation
  def validateQuantity = {
    ValidationResult.validate(
      quantity > 0,
      "quantity > 0")
  }

  def response(start: String, end: String) = {
    s"""{"start": "${start}", "end": "${end}"}"""
  }
}
