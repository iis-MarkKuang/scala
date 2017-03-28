package com.shrfid.api.persistence.slick.readable

import javax.inject.Inject

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.google.inject.Singleton
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.readable.PeriodicalItem
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
  * Created by Kuang 2017-03-28
  */

case class PeriodicalItemEntity(id: Int,
                                reference: Option[String],
                                title: Option[String],
                                barcode: String,
                                rfid: Option[String],
                                categoryId: Int, // 文献类型 待补充
                                stackId: Int, // 馆藏库id
                                clc: Option[String],
                                PeriodicalIndex: Option[Int], //书次号
                                isAvailable: Boolean, //图书是否(在借或者在馆但被预约)
                                isActive: Boolean,
                                userId: Option[Int], // 编目人
                                createAt: DateTime, //编目记录创建日期
                                updateAt: DateTime,
                                description: Option[String]
                               ) extends BaseEntity {

}

class PeriodicalItemTable(tag: Tag) extends BaseTable[PeriodicalItemEntity](tag, "periodical_item") {
  def reference = column[Option[String]]("reference", O.Length(254), Nullable)

  def title = column[Option[String]]("title", O.Length(254), Nullable)

  def barcode = column[String]("barcode", O.Length(254))

  def rfid = column[Option[String]]("rfid", O.Length(254), Nullable)

  def categoryId = column[Int]("category_id")

  def stackId = column[Int]("stack_id")

  def clc = column[Option[String]]("clc", O.Length(254), Nullable)

  def periodicalIndex = column[Option[Int]]("periodical_index", Nullable)

  def isAvailable = column[Boolean]("is_available")

  def isActive = column[Boolean]("is_active")

  def userId = column[Option[Int]]("user_id", Nullable)

  def createAt = column[DateTime]("create_at", O.SqlType("DATETIME"), O.Length(6))

  def updateAt = column[DateTime]("update_at", O.SqlType("DATETIME"), O.Length(6))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  def user = foreignKey("periodical_item_user_id_fk_auth_user_id", userId, TableQuery[AuthUserTable])(_.id.?)

  def stack = foreignKey("periodical_item_stack_id_fk_periodical_stack_id", stackId, TableQuery[PeriodicalStackTable])(_.id)

  override def * = (id, reference, title, barcode, rfid, categoryId, stackId, clc,
    periodicalIndex, isAvailable, isActive, userId, createAt, updateAt, description) <>((PeriodicalItemEntity.apply _).tupled, PeriodicalItemEntity.unapply)

  def barcodeIdx = index("periodical_item_barcode_uniq", barcode, unique = true)
}

@Singleton
class PeriodicalItemRepository @Inject()(db: SlickDatabaseSource) {
  lazy val periodicalItems = TableQuery[PeriodicalItemTable]
  lazy val periodicalStacks = TableQuery[PeriodicalStackTable]
  lazy val readerMemberBorrowHistory = TableQuery[ReaderMemberBorrowHistoryTable]
  lazy val authUsers = TableQuery[AuthUserTable]
  val dal = new BaseDalImpl[PeriodicalItemTable, PeriodicalItemEntity](db)(periodicalItems) {

    def findIdByBarcode(pedlBarcodes: Seq[String]): Future[Seq[Int]] = {
      val query = for {
        bs <- periodicalItems.filter(_.barcode inSetBind pedlBarcodes)
      } yield bs.id
      db.run(query.result).toTwitterFuture
    }

    def findPeriodicalIndex(clc: Option[String], periodicalIndex: Option[Int]): Future[Int] = {
      periodicalIndex match {
        case Some(pIndex) => Future.value(pIndex)
        case None =>
          val query = for {
            ps <- periodicalItems.filter(_.clc === clc)
          } yield (ps.periodicalIndex, ps.reference)
          db.run(query.sortBy(_._1.desc.nullsLast).take(1).result.headOption).toTwitterFuture.flatMap {
            case Some((Some(i), Some(reference))) => Future.value(i + 1)
            case _ => Future.value(1)
          }
      }
    }

    def updateAvailability(periodicalIds: Seq[Int], boolean: Boolean)  ={
      val query = periodicalItems.filter(_.id.inSetBind(periodicalIds)).map(x => x.isAvailable).update(boolean)
      db.run(query).toTwitterFuture
    }


    private val filter = (ids: Option[Seq[Int]], references: Option[Seq[String]], titles: Option[Seq[String]],
                          barcodes: Option[Seq[String]], rfids: Option[Seq[String]], categoryIds: Option[Seq[Int]],
                          stackIds: Option[Seq[Int]], clcs: Option[Seq[String]],
                          bookIndexes: Option[Seq[Int]], isAvailable: Option[Boolean], isActive: Option[Boolean]) => {
      periodicalItems.filter { it =>
        List(
          ids.map(it.id.inSetBind(_)),
          barcodes.map(it.barcode.inSetBind(_)),
          categoryIds.map(it.categoryId.inSetBind(_)),
          stackIds.map(it.stackId.inSetBind(_)),
          isAvailable.map(it.isAvailable === _),
          isActive.map(it.isActive === _)
        ).collect({ case Some(it) => it }).reduceLeftOption(Logic)
      }
    }
  }
}
