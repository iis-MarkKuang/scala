package com.shrfid.api.http.Elastic.reader.member

import com.twitter.finatra.request.{Header, RouteParam}
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 7/11/16.
  */
case class PostReaderMemberBorrowItemsRequest(@Header Authorization: String,
                                              @RouteParam id: String,
                                              bookBarcodes: Seq[String],
                                              location: String) {
//  @MethodValidation
//  def validateAction = {
//    ValidationResult.validate(
//      Seq("1", "2").contains(category),
//      "Category have to be 1 for ordinary books or 2 for periodicals")
//  }
}


