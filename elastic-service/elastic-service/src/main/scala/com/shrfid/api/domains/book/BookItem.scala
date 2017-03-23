package com.shrfid.api.domains.book

import com.shrfid.api._
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.shrfid.api.persistence.slick.BaseEntity
import com.shrfid.api.persistence.slick.book.BookItemEntity
import play.api.libs.json.Json

/**
  * Created by jiejin on 17/11/16.
  */
object Book {
  implicit val bookFmt = Json.format[Book]

}

case class Book(barcode: String,
                rfid: String = "",
                reference: String = "",
                category: String = "",
                title: String = "",
                stack: String, // 馆藏库id
                clc: String = "",
                book_index: Int = -1, // 书次号
                is_available: Boolean = true, // 图书是否在借
                is_active: Boolean = true,
                user: String = "", // 编目人
                datetime: String = Time.now.toString, // 编目记录创建日期
                description: String = "") extends BaseDoc {

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

  def toBookInfo: BookInfo = {
    BookInfo(barcode, rfid, reference, category, title, stack, clc)
  }
}

object BookWithId {
  implicit val bookFmt = Json.format[BookWithId]

  def toDomain(id: String, book:Book ): BookWithId = {
    BookWithId(id, book.barcode, book.rfid, book.reference, book.category, book.title, book.stack, book.clc,
      if (book.book_index == -1) "" else book.book_index.toString, if (book.book_index == -1) "" else  s"""{book.clc}.${book.book_index}""", book.is_available,
      book.is_active, book.user, book.description, book.datetime)
  }
}

case class BookWithId(id: String,
                      barcode: String,
                      rfid: String,
                      reference: String,
                      category: String ,
                      title: String,
                      stack: String, // 馆藏库id
                      clc: String,
                      book_index: String, // 书次号
                      call_no: String,
                      is_available: Boolean, // 图书是否在借
                      is_active: Boolean,
                      user: String, // 编目人
                      description: String,
                      datetime: String) extends BaseDoc {

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)


}


object BookItem {
  implicit val bookItemFmt = Json.format[BookItem]

  def toDomain(b: BookItemEntity): BookItem = {
    BookItem(b.id,
      optionStrToStr(b.reference),
      optionStrToStr(b.title),
      b.barcode,
      optionStrToStr(b.rfid),
      itemCategory(b.categoryId),
      b.stackId,
      optionStrToStr(b.clc),
      b.bookIndex,
      b.isAvailable,
      b.userId,
      b.createAt.toString,
      b.updateAt.toString)
  }
}

case class BookItem(id: Int,
                    reference: String,
                    title: String,
                    barcode: String,
                    rfid: String,
                    category: String, // 文献类型 1为普通图书 2为期刊 3为古籍 4为非书资料
                    stackId: Int,
                    clc: String,
                    bookIndex: Option[Int], // 书次号
                    isAvailable: Boolean, // 图书是否在借
                    userId: Option[Int], // 编目人
                    createAt: String, // 编目记录创建日期
                    updateAt: String) extends BaseEntity
