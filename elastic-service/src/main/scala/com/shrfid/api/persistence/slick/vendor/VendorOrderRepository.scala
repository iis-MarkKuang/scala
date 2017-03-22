package com.shrfid.api.persistence.slick.vendor

import java.sql.Date
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import org.joda.time.DateTime
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
import com.shrfid.api.persistence._
import com.shrfid.api.persistence.slick.auth.AuthUserTable

/**
  * Created by jiejin on 11/11/16.
  */
case class VendorOrderEntity(id: Int,
                             title: String,
                             author: String,
                             publisher: String,
                             isbn: String,
                             price: BigDecimal,
                             quantity: Int,
                             total: BigDecimal,
                             actualPrice: Option[BigDecimal],
                             actualQuantity: Option[Int],
                             actualTotal: Option[BigDecimal],
                             orderDate: Date,
                             arriveAt: Option[DateTime],
                             createAt: DateTime,
                             updateAt: DateTime,
                             description: Option[String],
                             vendorId: Int,
                             user_id: Int) extends BaseEntity

class VendorOrderTable(tag: Tag) extends BaseTable[VendorOrderEntity](tag, "vendor_order") {
  override def * = (id, title, author, publisher, isbn, price, quantity, total, actualPrice, actualQuantity, actualTotal,
    orderDate, arriveAt, createAt, updateAt, description, vendorId, userId) <>((VendorOrderEntity.apply _).tupled, VendorOrderEntity.unapply)

  def author = column[String]("author", O.Length(80))

  def publisher = column[String]("publisher", O.Length(80))

  def isbn = column[String]("isbn", O.Length(13))

  def price = column[BigDecimal]("price", O.Default[BigDecimal](0.00), O.SqlType("DECIMAL(10,2)"))

  def quantity = column[Int]("quantity", O.Default(0))

  def total = column[BigDecimal]("total", O.Default[BigDecimal](0.00), O.SqlType("DECIMAL(10,2)"))

  def actualPrice = column[Option[BigDecimal]]("actual_price", O.SqlType("DECIMAL(10,2)"), Nullable)

  def actualQuantity = column[Option[Int]]("actual_quantity", Nullable)

  def actualTotal = column[Option[BigDecimal]]("actual_total", O.SqlType("DECIMAL(10,2)"), Nullable)

  def orderDate = column[Date]("order_date", O.SqlType("DATE"))

  def arriveAt = column[Option[DateTime]]("arrive_at", O.SqlType("DATETIME"), O.Length(6))

  def createAt = column[DateTime]("create_at", O.SqlType("DATETIME"), O.Length(6))

  def updateAt = column[DateTime]("update_at", O.SqlType("DATETIME"), O.Length(6))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  def vendor = foreignKey("vendor_order_vender_id_fk_vender_member_id", vendorId, TableQuery[VendorMemberTable])(_.id)

  def vendorId = column[Int]("vender_id")

  def user = foreignKey("vendor_order_user_id_fk_auth_user_id", userId, TableQuery[AuthUserTable])(_.id)

  def userId = column[Int]("user_id")

  def idx = index("vender_order_book_title", title, unique = false)

  def title = column[String]("title", O.Length(254))
}

@Singleton
class VendorOrderRepository @Inject()(db: SlickDatabaseSource) {
  lazy val vendorOrders = TableQuery[VendorOrderTable]
  lazy val vendorMembers = TableQuery[VendorMemberTable]
  lazy val authUsers = TableQuery[AuthUserTable]
  val dal = new BaseDalImpl[VendorOrderTable, VendorOrderEntity](db)(vendorOrders) {
    /*def updateOrder(order: PatchVendorOrderRow) = {
      val q = for {o <- vendorOrders if o.id === order.id} yield (o.title, o.author, o.publisher, o.isbn, o.price,
        o.quantity, o.total, o.actualPrice, o.actualQuantity, o.actualTotal, o.orderDate, o.arriveAt, o.updateAt,
        o.description, o.vendorId)

      db.run(q.update((order.title, order.author, order.publisher, order.isbn, order.price,
        order.quantity, order.total, order.actualPrice, order.actualQuantity, order.actualTotal, order.orderDate, order.arriveAt,
        Time.now, order.description, order.vendorId))).toTwitterFuture

    }*/

    private def filter(vendorIds: Option[Seq[Int]],
                       isbns: Option[Seq[String]],
                       orderDate: Option[(Date, Date)],
                       createAt: Option[(DateTime, DateTime)],
                       isArrived: Option[Boolean],
                       logicOp: String) = {

      vendorOrders.filter { it =>
        List(
          vendorIds.map(it.vendorId.inSetBind(_)),
          isbns.map(it.isbn.inSetBind(_)),
          isArrived.map(whether => whether match {
            case true => !it.arriveAt.isEmpty
            case false => it.arriveAt.isEmpty
          }),
          orderDate.map(d => it.orderDate.between(d._1, d._2)),
          createAt.map(d => it.createAt.between(d._1, d._2))
        ).collect({ case Some(it) => it }).reduceLeftOption(logic(_, _, logicOp)).getOrElse(LiteralColumn(1) === LiteralColumn(1))
      }

    }

    def countByFilter(queryFilter: Map[String, _]) = {
      val q = filter(queryFilter("vendor").asInstanceOf[Option[Seq[Int]]],
        queryFilter("isbn").asInstanceOf[Option[Seq[String]]],
        queryFilter("orderDate").asInstanceOf[Option[(Date, Date)]],
        queryFilter("createAt").asInstanceOf[Option[(DateTime, DateTime)]],
        queryFilter("isArrived").asInstanceOf[Option[Boolean]],
        queryFilter("logicOp").asInstanceOf[String])
      db.run(q.length.result).toTwitterFuture
    }

    /*def findAllByFilter(queryFilter: Map[String, _]): Future[Seq[VendorOrder]] = {
      val q = for {
        o <- filter(queryFilter("vendor").asInstanceOf[Option[Seq[Int]]],
          queryFilter("isbn").asInstanceOf[Option[Seq[String]]],
          queryFilter("orderDate").asInstanceOf[Option[(Date, Date)]],
          queryFilter("createAt").asInstanceOf[Option[(DateTime, DateTime)]],
          queryFilter("isArrived").asInstanceOf[Option[Boolean]],
          queryFilter("logicOp").asInstanceOf[String])
        v <- vendorMembers if o.vendorId === v.id
        u <- authUsers if o.userId === u.id
      } yield (o.id, o.title, o.author, o.publisher, o.isbn, o.price, o.quantity, o.total, o.actualPrice,
        o.actualQuantity, o.actualTotal, o.orderDate, o.createAt, o.arriveAt, o.updateAt, o.description, v.name, u.fullName)
      val result = (queryFilter("limit").asInstanceOf[Int], queryFilter("offset").asInstanceOf[Int]) match {
        case (All, o) =>
          db.run(q.drop(o).sortBy(_._1.asc.nullsLast).result).toTwitterFuture
        case (l, o) =>
          db.run(q.sortBy(_._1.asc.nullsLast).drop(o).take(l).result).toTwitterFuture
      }
      result.flatMap {
        r => Future.value(r.map(a =>
          VendorOrder(a._1, a._2, a._3, a._4, a._5, a._6, a._7, a._8, a._9.getOrElse(0.00), a._10.getOrElse(0),
            a._11.getOrElse(0.00), a._12.toString,a._13.toString, a._14 match {
              case None => Empty
              case Some(dt) => dt.toString
            }, a._15.toString, a._16.getOrElse(""), a._17, a._18)))
      }
    }*/
  }
}

