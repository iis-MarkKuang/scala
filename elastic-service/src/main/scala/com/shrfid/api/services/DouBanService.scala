package com.shrfid.api.services

import com.shrfid.api._
import com.twitter.util.Future

import com.shrfid.api.http.Elastic.book.reference.GetBookCatalogingByISBNRequest
/**
  * Created by weijing.xu
  */

trait DouBanService {
 def findBookCatalogingByISBN(request: GetBookCatalogingByISBNRequest): Future[(Int, String)]

}
