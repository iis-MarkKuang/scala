package com.shrfid.api.persistence.slick.book

import javax.inject.Inject

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.google.inject.Singleton
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book.BookItem
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.auth.AuthUserTable
import com.shrfid.api.persistence.slick.reader.ReaderMemberBorrowHistoryTable
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import com.twitter.util.Future
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
import com.shrfid.api.persistence._

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by Administrator on 2016-9-12.
  */
case class BookItemEntity(id: Int,
                          reference: Option[String],
                          title: Option[String],
                          barcode: String,
                          rfid: Option[String],
                          categoryId: Int, // 文献类型 1为普通图书 2为期刊 3为古籍 4为非书资料
                          stackId: Int, // 馆藏库id
                          clc: Option[String],
                          bookIndex: Option[Int], // 书次号
                          isAvailable: Boolean, // 图书是否在借
                          isActive: Boolean,
                          userId: Option[Int], // 编目人
                          createAt: DateTime, // 编目记录创建日期
                          updateAt: DateTime,
                          description: Option[String]
                         ) extends BaseEntity {

}


class BookItemTable(tag: Tag) extends BaseTable[BookItemEntity](tag, "book_item") {
  def reference = column[Option[String]]("reference", O.Length(254), Nullable)

  def title = column[Option[String]]("title", O.Length(254), Nullable)

  def barcode = column[String]("barcode", O.Length(254))

  def rfid = column[Option[String]]("rfid", O.Length(254), Nullable)

  def categoryId = column[Int]("category_id")

  def stackId = column[Int]("stack_id")

  def clc = column[Option[String]]("clc", O.Length(254), Nullable)

  def bookIndex = column[Option[Int]]("book_index", Nullable)

  def isAvailable = column[Boolean]("is_available")

  def isActive = column[Boolean]("is_active")

  def userId = column[Option[Int]]("user_id", Nullable)

  def createAt = column[DateTime]("create_at", O.SqlType("DATETIME"), O.Length(6))

  def updateAt = column[DateTime]("update_at", O.SqlType("DATETIME"), O.Length(6))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  def user = foreignKey("book_item_user_id_fk_auth_user_id", userId, TableQuery[AuthUserTable])(_.id.?)

  def stack = foreignKey("book_item_stack_id_fk_book_stack_id", stackId, TableQuery[BookStackTable])(_.id)

  override def * = (id, reference, title, barcode, rfid, categoryId, stackId, clc,
    bookIndex, isAvailable, isActive, userId, createAt, updateAt, description) <>((BookItemEntity.apply _).tupled, BookItemEntity.unapply)

  def barcodeIdx = index("book_item_barcode_uniq", barcode, unique = true)
}

@Singleton
class BookItemRepository @Inject()(db: SlickDatabaseSource) {
  lazy val bookItems = TableQuery[BookItemTable]
  lazy val bookStacks = TableQuery[BookStackTable]
  lazy val readerMemberBorrowHistory = TableQuery[ReaderMemberBorrowHistoryTable]
  lazy val authUsers = TableQuery[AuthUserTable]
  val dal = new BaseDalImpl[BookItemTable, BookItemEntity](db)(bookItems) {

    def findIdByBarcode(bookBarcodes: Seq[String]): Future[Seq[Int]] = {
      val query = for {
        bs <- bookItems.filter(_.barcode inSetBind bookBarcodes)
      } yield bs.id
      db.run(query.result).toTwitterFuture
    }

    def findBookIndex(clc: Option[String], bookIndex: Option[Int]): Future[Int] = {
      bookIndex match {
        case Some(bIndex) => Future.value(bIndex)
        case None =>
          val query = for {
            bs <- bookItems.filter(_.clc === clc)
          } yield (bs.bookIndex, bs.reference)
          db.run(query.sortBy(_._1.desc.nullsLast).take(1).result.headOption).toTwitterFuture.flatMap {
            case Some((Some(i), Some(reference))) => Future.value(i + 1)
            case _ => Future.value(1)
          }
      }

    }

    def updateAvailability(bookIds: Seq[Int], boolean: Boolean) = {
      val query = bookItems.filter(_.id.inSetBind(bookIds)).map(x => x.isAvailable).update(boolean)
      db.run(query).toTwitterFuture
    }


    private val filter = (ids: Option[Seq[Int]], references: Option[Seq[String]], titles: Option[Seq[String]],
                          barcodes: Option[Seq[String]], rfids: Option[Seq[String]], categoryIds: Option[Seq[Int]],
                          stackIds: Option[Seq[Int]], clcs: Option[Seq[String]],
                          bookIndexs: Option[Seq[Int]], isAvailable: Option[Boolean], isActive: Option[Boolean]) => {
      bookItems.filter { it =>
        List(
          ids.map(it.id.inSetBind(_)),
          barcodes.map(it.barcode.inSetBind(_)),
          categoryIds.map(it.categoryId.inSetBind(_)),
          stackIds.map(it.stackId.inSetBind(_)),
          isAvailable.map(it.isAvailable === _),
          isActive.map(it.isActive === _)
        ).collect({ case Some(it) => it }).reduceLeftOption(logic(_, _, "and")).getOrElse(LiteralColumn(1) === LiteralColumn(1))
      }.filter { it =>
        List(
          references.map(a => it.reference.nonEmpty && it.reference.inSetBind(a)),
          titles.map(it.title.nonEmpty && it.title.inSetBind(_)),
          rfids.map(it.rfid.nonEmpty && it.rfid.inSetBind(_)),
          clcs.map(it.clc.nonEmpty && it.clc.inSetBind(_)),
          bookIndexs.map(it.bookIndex.nonEmpty && it.bookIndex.inSetBind(_))
        ).collect({ case Some(it) => it }) match {
          case a if a.isEmpty => LiteralColumn(1) === LiteralColumn(1)
          case other => other.reduce(_ && _).getOrElse(LiteralColumn(1) === LiteralColumn(1))
        }
      }
    }


    def findByFilter(queryFilter: Map[String, _]): Future[(Int, Seq[BookItem])] = {
      val q = filter(queryFilter("id").asInstanceOf[Option[Seq[Int]]],
        queryFilter("reference").asInstanceOf[Option[Seq[String]]],
        queryFilter("title").asInstanceOf[Option[Seq[String]]],
        queryFilter("barcode").asInstanceOf[Option[Seq[String]]],
        queryFilter("rfid").asInstanceOf[Option[Seq[String]]],
        queryFilter("categoryId").asInstanceOf[Option[Seq[Int]]],
        queryFilter("stackId").asInstanceOf[Option[Seq[Int]]],
        queryFilter("clc").asInstanceOf[Option[Seq[String]]],
        queryFilter("bookIndex").asInstanceOf[Option[Seq[Int]]],
        queryFilter("isAvailable").asInstanceOf[Option[Boolean]],
        queryFilter("isActive").asInstanceOf[Option[Boolean]]
      )

      val contentQ = (queryFilter("limit").asInstanceOf[Int], queryFilter("offset").asInstanceOf[Int]) match {
        case (All, o) =>
          q.drop(o).sortBy(_.id.asc.nullsLast).result
        case (l, o) =>
          q.sortBy(_.id.asc.nullsLast).drop(o).take(l).result
      }

      for {
        count <- db.run(q.length.result).toTwitterFuture
        content <- db.run(contentQ).toTwitterFuture
      } yield (count, content) match {
        case (c: Int, con: Seq[BookItemEntity]) => (c, con.map(BookItem.toDomain))
      }

    }

    def upsert(bookItemEntity: BookItemEntity): Future[Int] = {
      val q = for {
        existing <- bookItems.filter(_.barcode === bookItemEntity.barcode).result.headOption
        row = existing.fold(bookItemEntity)(_.copy(reference = bookItemEntity.reference,
          title = bookItemEntity.title,
          rfid = bookItemEntity.rfid,
          categoryId = bookItemEntity.categoryId,
          stackId = bookItemEntity.stackId,
          clc = bookItemEntity.clc,
          bookIndex = bookItemEntity.bookIndex,
          userId = bookItemEntity.userId,
          updateAt = Time.now))
        result <- bookItems.insertOrUpdate(row)
      } yield result
      db.run(q).toTwitterFuture
    }

    // TODO: updating isActive, isAvailable, stackId
    def inactivate(bookBarcode: String, stackId: Int, description: String): Future[Int] = {
      val q = for {
        book <- bookItems.filter(b => b.barcode === bookBarcode && b.isAvailable)
      } yield (book.stackId, book.isActive, book.updateAt, book.description)
      db.run(q.update((stackId, false, Time.now, Some(description)))).toTwitterFuture
    }

    def findMaxBarcode(categortId: Int): Future[Option[String]] = {
      val q = for {
        barcode <- bookItems.map(_.barcode).max
      } yield barcode
      db.run(q.result).toTwitterFuture
    }

    def transfer(bookBarcode: String, bookStack: BookStackEntity, description: String): Future[Int] = {
      bookStack.isActive match {
        case true =>
          val q = for {
            book <- bookItems.filter(b => b.barcode === bookBarcode && b.isAvailable)
          } yield (book.stackId, book.isActive, book.updateAt, book.description)
          db.run(q.update((bookStack.id, true, Time.now, Some(description)))).toTwitterFuture
        case false =>
          val q = for {
            book <- bookItems.filter(b => b.barcode === bookBarcode && b.isAvailable)
          } yield (book.stackId, book.isActive, book.updateAt, book.description)
          db.run(q.update((bookStack.id, false, Time.now, Some(description)))).toTwitterFuture
      }

    }
  }
}
