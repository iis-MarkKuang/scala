package com.shrfid.api.domains.book

import com.shrfid.api.Time
import com.shrfid.api.persistence.elastic4s.BaseDoc
import play.api.libs.json.Json

/**
  * Created by Administrator on 2017/4/6.
  */

object ReservationHistory {
  implicit val fmt = Json.format[ReservationHistory]

  def toDomain(status: String, reader: ReaderInfo, book: Book, _reservation: TimeLocation) = {
    ReservationHistory(status, reader, book.toBookInfo, _reservation)
  }
}

case class ReservationHistory(status: String,
                         reader: ReaderInfo,
                         book: BookInfo,
                         _reservation: TimeLocation,
                         datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}