package com.shrfid.api.persistence.slick.reader

import javax.inject.Inject

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.google.inject.Singleton
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.domains.reader.ReaderMemberBorrowRecord
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.readable.BookItemTable
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
import com.shrfid.api.persistence._

/**
  * Created by Administrator on 2016-9-12.
  */
case class ReaderMemberBorrowHistoryEntity(id: Int,
                                           bookItemId: Int,
                                           readerMemberId: Int,
                                           borrowAt: DateTime,
                                           dueAt: DateTime,
                                           returnAt: Option[DateTime]) extends BaseEntity


class ReaderMemberBorrowHistoryTable(tag: Tag) extends BaseTable[ReaderMemberBorrowHistoryEntity](tag, "reader_member_borrow_history") {
  def bookItemId = column[Int]("book_item_id")

  def readerMemberId = column[Int]("member_id")

  def borrowAt = column[DateTime]("borrow_at", O.SqlType("DATETIME"))

  def dueAt = column[DateTime]("due_at", O.SqlType("DATETIME"))

  def returnAt = column[Option[DateTime]]("return_at", O.SqlType("DATETIME"), Nullable)

  def bookItem = foreignKey("borrow_history_book_item_id_fk_book_item_id", bookItemId, TableQuery[BookItemTable])(_.id)

  def readerMember = foreignKey("borrow_history_reader_member_id_fk_member_id", readerMemberId, TableQuery[ReaderMemberTable])(_.id)

  override def * = (id, bookItemId, readerMemberId, borrowAt, dueAt, returnAt) <>((ReaderMemberBorrowHistoryEntity.apply _).tupled, ReaderMemberBorrowHistoryEntity.unapply)
}

@Singleton
class ReaderMemberBorrowHistoryRepository @Inject()(db: SlickDatabaseSource) {
  lazy val readerMemberBorrowHistory = TableQuery[ReaderMemberBorrowHistoryTable]
  lazy val readerMembers = TableQuery[ReaderMemberTable]
  lazy val bookItems = TableQuery[BookItemTable]
  val dal = new BaseDalImpl[ReaderMemberBorrowHistoryTable, ReaderMemberBorrowHistoryEntity](db)(readerMemberBorrowHistory) {

    def countByFilter(param: String, by: String, recordOption: String) = {
      val query = (recordOption, by) match {
        case ("current", "id") =>
          for {
            rs <- readerMemberBorrowHistory if rs.readerMemberId === param.toInt && rs.returnAt.isEmpty
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("all", "id") =>
          for {
            rs <- readerMemberBorrowHistory if rs.readerMemberId === param.toInt
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("current", "barcode") =>
          for {
            r <- readerMembers if r.barcode === param
            rs <- readerMemberBorrowHistory if rs.readerMemberId === r.id && rs.returnAt.isEmpty
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("all", "barcode") =>
          for {
            r <- readerMembers if r.barcode === param
            rs <- readerMemberBorrowHistory if rs.readerMemberId === r.id
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
      }
      db.run(query.length.result).toTwitterFuture
    }

    def findByFilter(param: String, by: String, recordOption: String, limit: Int = 100,
                     offset: Int = 0): Future[Seq[ReaderMemberBorrowRecord]] = {
      val query = (recordOption, by) match {
        case ("current", "id") =>
          for {
            rs <- readerMemberBorrowHistory if rs.readerMemberId === param.toInt && rs.returnAt.isEmpty
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("all", "id") =>
          for {
            rs <- readerMemberBorrowHistory if rs.readerMemberId === param.toInt
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("current", "barcode") =>
          for {
            r <- readerMembers if r.barcode === param
            rs <- readerMemberBorrowHistory if rs.readerMemberId === r.id && rs.returnAt.isEmpty
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
        case ("all", "barcode") =>
          for {
            r <- readerMembers if r.barcode === param
            rs <- readerMemberBorrowHistory if rs.readerMemberId === r.id
            b <- bookItems if rs.bookItemId === b.id
          } yield (rs, b)
      }
      val result = (limit, offset) match {
        case (All, o) => db.run(query.drop(offset).sortBy(_._1.id.asc.nullsLast).result).toTwitterFuture
        case (l, o) => db.run(query.sortBy(_._1.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
      }
      result.map {
        r => r.map(a => ReaderMemberBorrowRecord(a._1.id, a._2, a._1.readerMemberId, a._1.borrowAt, a._1.dueAt, a._1.returnAt))
      }
    }

    def updateReturnAt(bookItemIds: Seq[Int]): Future[Int] = {
      val query = readerMemberBorrowHistory.filter(r => r.bookItemId.inSetBind(bookItemIds) && r.returnAt.isEmpty).map(x => x.returnAt)
        .update(Some(DateTime.now(DateTimeZone.forOffsetHours(8))))
      db.run(query).toTwitterFuture
    }
  }
}
