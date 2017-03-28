package com.shrfid.api.domains.readable

import com.shrfid.api._
import com.shrfid.api.persistence.elastic4s.BaseDoc
import play.api.libs.json.Json
/**
  * Created by kuang on 2017/3/27.
  */
object BookReserve {
  implicit val bookReserveFmt = Json.format[BookReserve]
}

case class BookReserve(barcode: String,
                       reader_id: String,
                       user: String = "", //管理员
                       datetime: String = Time.now.toString)
  extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}