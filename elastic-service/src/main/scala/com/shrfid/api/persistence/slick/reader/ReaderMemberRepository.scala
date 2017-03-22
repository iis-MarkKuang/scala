package com.shrfid.api.persistence.slick.reader

import java.sql.Date
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.modules.SlickDatabaseModule._
import com.shrfid.api.persistence.slick.auth.AuthUserTable
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
import com.shrfid.api.persistence._
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
/**
  * Created by jiejin on 7/09/2016.
  */
case class ReaderMemberEntity(id: Int,
                              barcode: String,
                              rfid: Option[String],
                              identity: String,
                              password: String,
                              fullName: String,
                              gender: String,
                              dob: Date,
                              email: Option[String],
                              mobile: Option[String],
                              address: Option[String],
                              postcode: Option[String],
                              profileImage: Option[String],
                              isActive: Boolean,
                              restoreAt: Option[Date],
                              createAt: DateTime,
                              updateAt: DateTime,
                              lastLogin: DateTime,
                              levelId: Int) extends BaseEntity


class ReaderMemberTable(tag: Tag) extends BaseTable[ReaderMemberEntity](tag, "reader_member") {

  def barcode = column[String]("barcode", O.Length(254))

  def rfid = column[Option[String]]("rfid", O.Length(254), Nullable)

  def identity = column[String]("identity", O.Length(30))

  def password = column[String]("password", O.Length(128))

  def fullName = column[String]("full_name", O.Length(30))

  def gender = column[String]("gender", O.Length(4))

  def dob = column[Date]("dob", O.SqlType("DATE"))

  def email = column[Option[String]]("email", O.Length(254), Nullable)

  def mobile = column[Option[String]]("mobile", O.Length(20), Nullable)

  def address = column[Option[String]]("address", O.Length(254), Nullable)

  def postcode = column[Option[String]]("postcode", O.Length(20), Nullable)

  def profileImage = column[Option[String]]("profile_image", O.SqlType("TEXT"), Nullable)

  def isActive = column[Boolean]("is_active", O.Default(true))

  def restoreAt = column[Option[Date]]("restore_at", O.SqlType("DATE"), Nullable)

  def createAt = column[DateTime]("create_at", O.SqlType("DATETIME"), O.Length(6))

  def updateAt = column[DateTime]("update_at", O.SqlType("DATETIME"), O.Length(6))

  def lastLogin = column[DateTime]("last_login", O.SqlType("DATETIME"), O.Length(6))

  def levelId = column[Int]("level_id")

  override def * = (id, barcode, rfid, identity, password, fullName, gender, dob, email, mobile, address, postcode,
    profileImage, isActive, restoreAt, createAt, updateAt, lastLogin, levelId) <>((ReaderMemberEntity.apply _).tupled, ReaderMemberEntity.unapply)


  def level = foreignKey("reader_member_level_id_fk_reader_level_id", levelId, TableQuery[ReaderLevelTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Restrict)

  def barcodeIdx = index("member_member_user_id_barcode_uniq", barcode, unique = true)

  def identityIdx = index("member_member_user_id_identity_uniq", identity, unique = true)
}

@Singleton
class ReaderMemberRepository @Inject()(db: SlickDatabaseSource) {
  lazy val readerMembers = TableQuery[ReaderMemberTable]
  lazy val readerMemberGroups = TableQuery[ReaderMemberGroupTable]
  lazy val readerGroups = TableQuery[ReaderGroupTable]
  lazy val readerLevels = TableQuery[ReaderLevelTable]
  lazy val authUsers = TableQuery[AuthUserTable]

  implicit class QueryExtensions3[E, T <: Table[E], C[_]]
  (val query: Query[T, E, C]) {
    def sortDynamic(sortString: String): Query[T, E, C] = {
      //split string into useful pieces
      val sortKeys = sortString.split(Delimiter).toList.map(
        _.split('.').map(_.toUpperCase).toList)
      sortDynamicImpl(sortKeys)
    }

    private def sortDynamicImpl(sortKeys: List[Seq[String]]): Query[T, E, C] = {
      sortKeys match {
        case key :: tail =>
          sortDynamicImpl(tail).sortBy(table =>
            key match {
              case name :: Nil => table.column[String](name).asc
              case name :: "ASC" :: Nil => table.column[String](name).asc
              case name :: "DESC" :: Nil => table.column[String](name).desc
              case o => throw new Exception("invalid	sorting	key:	" + o)
            }
          )
        case Nil => query
      }
    }
  }


  val dal = new BaseDalImpl[ReaderMemberTable, ReaderMemberEntity](db)(readerMembers) {

    private def filter(barcode: Option[String], identity: Option[String],
                       fullName: Option[String], gender: Option[String], dob: Option[String], readerLevel: Option[String],
                       readerGroup: Option[String], createAt: Option[String],
                       isActive: Option[Boolean], isSuspend: Option[Boolean], logicOp: String) = {

      for {
        r <- readerMembers.filter { it =>
          List(
            barcode.map(it.barcode inSetBind _.split(Delimiter)),
            readerLevel.map(it.levelId inSetBind _.split(Delimiter).map(_.toInt).toList),
            identity.map(it.identity === _),
            fullName.map(it.fullName === _),
            gender.map(it.gender === _),
            dob.map { dob => val dt = dob.split(Delimiter).map(java.sql.Date.valueOf _); it.dob >= dt.head && it.dob <= dt(1) },
            createAt.map { c => val dt = c.split(Delimiter).map(DateTime.parse _); it.createAt >= dt.head && it.createAt <= dt(1) },
            isActive.map(it.isActive === _),
            isSuspend.map(whether => whether match {
              case true => !it.restoreAt.isEmpty
              case false => it.restoreAt.isEmpty
            })
          ).collect({ case Some(it) => it }).reduceLeftOption(logic(_, _, logicOp)).getOrElse(LiteralColumn(1) === LiteralColumn(1))
        }
        rmg <- readerMemberGroups.filter { it =>
          List(
            readerGroup.map(it.groupId inSetBind _.split(Delimiter).map(_.toInt).toList)
          ).collect({ case Some(it) => it }).reduceLeftOption(logic(_, _, logicOp)).getOrElse(LiteralColumn(1) === LiteralColumn(1))
        } if rmg.memberId === r.id
      } yield (r.id, r.barcode, r.levelId, r.identity, r.fullName, r.gender, r.dob, r.createAt, r.isActive, r.restoreAt)
    }

    def countByFilter(barcode: Option[String], identity: Option[String],
                      fullName: Option[String], gender: Option[String], dob: Option[String], readerLevel: Option[String],
                      readerGroup: Option[String], createAt: Option[String], isActive: Option[Boolean], isSuspend: Option[Boolean], logicOp: String): Future[Int] = {
      val q = filter(barcode, identity, fullName, gender, dob,
        readerLevel, readerGroup, createAt, isActive, isSuspend, logicOp)
      db.run(q.distinctOn(_._1).length.result).toTwitterFuture
    }

    /*def findAllByFilter(limit: Int, offset: Int, barcode: Option[String], identity: Option[String],
                        fullName: Option[String], gender: Option[String], dob: Option[String], readerLevel: Option[String],
                        readerGroup: Option[String], createAt: Option[String], isActive: Option[Boolean], isSuspend: Option[Boolean], logicOp: String, ordering: Option[String]) = {
      val q = filter(barcode, identity, fullName, gender, dob,
        readerLevel, readerGroup, createAt, isActive, isSuspend, logicOp)
      val raw = (limit, offset) match {
        case (All, o) =>
          db.run(q.distinctOn(_._1).drop(offset).sortBy(_._1.asc.nullsLast).result).toTwitterFuture
        case (l, o) => db.run(q.distinctOn(_._1).sortBy(_._1.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
      }
      raw.map(r => r.map(c => ReaderMember(c._1, c._2, c._3, c._4, c._5, c._6, c._7, c._8, c._9, c._10)))
    }*/

    /*def findDetail[C: CanBeQueryCondition](f: (ReaderMemberTable) => C): Future[Option[ReaderMemberDetail]] = {
      val q = for {
        r <- readerMembers.withFilter(f)
        rmg <- readerMemberGroups if rmg.memberId === r.id
        rg <- readerGroups if rmg.groupId === rg.id
        rl <- readerLevels if r.levelId === rl.id
      } yield (r, rg, rl)
      val raw = db.run(q.result).toTwitterFuture
      raw.map(r => r.groupBy(a => (a._1, a._3)).map(c => ReaderMemberDetail(c._1._1.id, c._1._1.barcode, c._1._1.rfid,
        ReaderLevel.toDomain(c._1._2), c._2.map(a => ReaderGroup.toDomain(a._2)),
        c._1._1.identity, c._1._1.password, c._1._1.fullName, c._1._1.gender, c._1._1.dob, c._1._1.email, c._1._1.mobile,
        c._1._1.address, c._1._1.postcode, c._1._1.profileImage, c._1._1.restoreAt, c._1._1.createAt, c._1._1.updateAt, c._1._1.lastLogin, c._1._1.isActive)).headOption)
    }*/

    def activeMember(ids: Seq[Int], boolean: Boolean): Future[Int] = {
      val q = for {r <- readerMembers if r.id inSetBind ids} yield r.isActive
      db.run(q.update(boolean)).toTwitterFuture
    }


    def updateMemberCard(id: Int, barcode: String, rfid: Option[String]): Future[Int] = {
      val q = for {
        r <- readerMembers if r.id === id
      } yield (r.barcode, r.rfid, r.updateAt)
      db.run(q.update((barcode, rfid, Time.now))).toTwitterFuture
    }

    def suspendMember(id: Int, days: Int): Future[Int] = {
      val q = for {
        r <- readerMembers if r.id === id
      } yield r.restoreAt
      db.run(q.update(Some(new java.sql.Date(new java.util.Date().getTime + (days * 1000 * 60 * 60 * 24))))).toTwitterFuture
    }

    def updateMember(id: Int, barcode: String, rfid: Option[String], identity: String,
                     fullName: String, gender: String, dob: Date, email: Option[String],
                     mobile: Option[String], address: Option[String], postcode: Option[String],
                     profileUrl: Option[String], isActive: Boolean, restoreAt: Option[Date], levelId: Int): Future[Int] = {
      val q = for {
        r <- readerMembers if r.id === id
      } yield (r.barcode, r.rfid, r.identity, r.fullName, r.gender, r.dob, r.email, r.mobile, r.address, r.postcode,
        r.profileImage, r.isActive, r.restoreAt, r.updateAt, r.levelId)
      db.run(q.update((barcode, rfid, identity, fullName, gender, dob, email, mobile, address, postcode, profileUrl,
        isActive, restoreAt, DateTime.now(DateTimeZone.forOffsetHours(8)), levelId))).toTwitterFuture
    }


    def findIdByBarcode(barcode: String): Future[Option[Int]] = {
      val query = for {
        rm <- readerMembers if rm.barcode === barcode
      } yield rm.id
      db.run(query.result.headOption).toTwitterFuture
    }
  }
}
