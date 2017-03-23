package com.shrfid.api.persistence.slick.reader

import javax.inject.Inject

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.google.inject.Singleton
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.book.BookItemTable
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
/**
  * Created by Administrator on 2016-9-12.
  */
case class ReaderMemberOrderEntity(id: Int,
                                   bookItemId: Int,
                                   readerMemberId: Int,
                                   orderAt: DateTime,
                                   availableAt: Option[DateTime],
                                   borrowHistoryId: Option[Int]) extends BaseEntity

//到库时间

class ReaderMemberOrderTable(tag: Tag) extends BaseTable[ReaderMemberOrderEntity](tag, "reader_member_order") {
  def bookItemId = column[Int]("book_item_id")

  def readerMemberId = column[Int]("member_id")

  def orderAt = column[DateTime]("order_at", O.SqlType("DATETIME"))

  def availableAt = column[Option[DateTime]]("available_at", O.SqlType("DATETIME"), Nullable)

  def borrowHistoryId = column[Option[Int]]("history_Id", Nullable)

  def bookItem = foreignKey("member_order_item_id_fk_book_item_id", bookItemId, TableQuery[BookItemTable])(_.id)

  def readerMember = foreignKey("member_order_member_id_fk_member_id", readerMemberId, TableQuery[ReaderMemberTable])(_.id)

  def borrowHistory = foreignKey("member_order_borrow_history_id_fk_history_Id", borrowHistoryId, TableQuery[ReaderMemberBorrowHistoryTable])(_.id.?)

  override def * = (id, bookItemId, readerMemberId, orderAt, availableAt, borrowHistoryId) <>((ReaderMemberOrderEntity.apply _).tupled, ReaderMemberOrderEntity.unapply)
}

@Singleton
class ReaderMemberOrderRepository @Inject()(db: SlickDatabaseSource) {
  lazy val bookAppointRecords = TableQuery[ReaderMemberOrderTable]
  val dal = new BaseDalImpl[ReaderMemberOrderTable, ReaderMemberOrderEntity](db)(bookAppointRecords) {

  }
}

