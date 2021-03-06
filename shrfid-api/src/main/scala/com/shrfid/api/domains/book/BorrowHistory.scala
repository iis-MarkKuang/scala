package com.shrfid.api.domains.book

import com.shrfid.api.Time
import com.shrfid.api.persistence.elastic4s.BaseDoc
import play.api.libs.json.Json

/**
  * Created by jiejin on 11/12/16.
  */

object ReaderInfo {
  implicit val fmt = Json.format[ReaderInfo]
}

case class ReaderInfo(id: String,
                      barcode: String,
                      rfid: String,
                      level: String,
                      level_new: Option[String],
                      groups: Seq[String],
                      identity: String,
                      full_name: String,
                      gender: String,
                      dob: String,
                      create_at: String) {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object BookInfo {
  implicit val fmt = Json.format[BookInfo]
}

case class BookInfo(barcode: String,
                    rfid: String = "",
                    reference: String = "",
                    category: String = "",
                    title: String = "",
                    stack: String, // 馆藏库id
                    clc: String = "") {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object TimeLocation {
  implicit val fmt = Json.format[TimeLocation]
}

case class TimeLocation(datetime: String,
                        location: String)

object BorrowHistory {
  implicit val fmt = Json.format[BorrowHistory]

  def toDomain(status: String, reader: ReaderInfo, book: Book, _borrow: TimeLocation,
               _return: Option[TimeLocation],
               _renew: Option[TimeLocation], days: Int) = {
    BorrowHistory(status, reader, book.toBookInfo, _borrow, _return, _renew,Time.nextNdays(days).toString)
  }
}


case class BorrowHistory(status: String,
                         reader: ReaderInfo,
                         book: BookInfo,
                         _borrow: TimeLocation,
                         _return: Option[TimeLocation],
                         _renew: Option[TimeLocation],
                         due_at: String,
                         datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

// Added by kuangyuan 5/12/2017
//object BorrowHistoryWithId {
//  implicit val fmt = Json.format[BorrowHistoryWithId]
//
//  def toDomain(id: String, borrowHistory: BorrowHistory): BorrowHistoryWithId = {
//    println(borrowHistory)
//    BorrowHistoryWithId(
//      id,
//      borrowHistory.status,
//      borrowHistory.reader,
//      borrowHistory.book,
//      borrowHistory._borrow,
//      borrowHistory._return,
//      borrowHistory._renew,
//      borrowHistory.due_at,
//      borrowHistory.datetime)
//  }
//}
//
//case class BorrowHistoryWithId(
//                                _id: String,
//                                status: String,
//                                reader: ReaderInfo,
//                                book: BookInfo,
//                                _borrow: TimeLocation,
//                                _return: Option[TimeLocation],
//                                _renew: Option[TimeLocation],
//                                due_at: String,
//                                datetime: String = Time.now.toString
//                              ) extends BaseDoc {
//  lazy val json = Json.toJson(this)
//  lazy val jsonStringify = Json.stringify(json)
//}
