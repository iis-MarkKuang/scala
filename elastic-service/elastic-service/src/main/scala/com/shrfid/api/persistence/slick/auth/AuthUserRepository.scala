package com.shrfid.api.persistence.slick.auth

/**
  * Created by jiejin on 6/09/2016.
  */

import java.sql.Date
import javax.inject.{Inject, Singleton}

import com.github.tototoshi.slick.MySQLJodaSupport._
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable
import com.shrfid.api.persistence._
import com.shrfid.api.persistence.slick.reader.ReaderMemberTable

object AuthUserEntity {
  implicit val authUserEntityFmt = Json.format[AuthUserEntity]
}

case class AuthUserEntity(id: Int,
                          username: String,
                          identity: String,
                          password: String,
                          fullName: String,
                          gender: String,
                          dob: Option[Date],
                          email: Option[String],
                          mobile: Option[String],
                          address: Option[String],
                          postcode: Option[String],
                          profileUrl: Option[String],
                          isSuperuser: Boolean,
                          isStaff: Boolean,
                          isActive: Boolean,
                          createAt: DateTime,
                          updateAt: DateTime,
                          lastLogin: DateTime) extends BaseEntity


class AuthUserTable(tag: Tag) extends BaseTable[AuthUserEntity](tag, "auth_user") {
  def username = column[String]("username", O.Length(30))

  def identity = column[String]("identity", O.Length(30))

  def password = column[String]("password", O.Length(128))

  def fullName = column[String]("full_name", O.Length(30))

  def gender = column[String]("gender", O.Length(4))

  def dob = column[Option[Date]]("dob", O.SqlType("DATE"))

  def email = column[Option[String]]("email", O.Length(254), Nullable)

  def mobile = column[Option[String]]("mobile", O.Length(20), Nullable)

  def address = column[Option[String]]("address", O.Length(254), Nullable)

  def postcode = column[Option[String]]("postcode", O.Length(20), Nullable)

  def profileUrl = column[Option[String]]("profile_url", O.Length(254), Nullable)

  def isSuperuser = column[Boolean]("is_superuser", O.Default(false))

  def isStaff = column[Boolean]("is_staff", O.Default(false))

  def isActive = column[Boolean]("is_active", O.Default(true))

  def createAt = column[DateTime]("create_at", O.SqlType("DATETIME"), O.Length(6))

  def updateAt = column[DateTime]("update_at", O.SqlType("DATETIME"), O.Length(6))

  def lastLogin = column[DateTime]("last_login", O.SqlType("DATETIME"), O.Length(6))

  //def organizationId = column[Option[Int]]("organization_id", Nullable)
  override def * = (id, username, identity, password, fullName, gender, dob, email, mobile, address, postcode, profileUrl, isSuperuser,
    isStaff, isActive, createAt, updateAt, lastLogin) <>((AuthUserEntity.apply _).tupled, AuthUserEntity.unapply)

  //def organization = foreignKey("auth_user_organization_id_fk_organization_id", organizationId, TableQuery[BaseOrganizationTable])(_.id.?)
  def idx = index("auth_user_username_uniq", username, unique = true)
}

@Singleton
class AuthUserRepository @Inject()(db: SlickDatabaseSource) {

  lazy val authUsers = TableQuery[AuthUserTable]

  lazy val readerMembers = TableQuery[ReaderMemberTable]

  val dal = new BaseDalImpl[AuthUserTable, AuthUserEntity](db)(authUsers) {

    def updateLastLogin(id: Int): Future[Int] = {
      val q = for {u <- authUsers if u.id === id} yield u.lastLogin
      db.run(q.update(DateTime.now(DateTimeZone.forOffsetHours(8)))).toTwitterFuture
    }

    private def filter(username: Option[String], identity: Option[String], fullName: Option[String], gender: Option[String],
                       isSuperuser: Option[Boolean], isStaff: Option[Boolean], isActive: Option[Boolean]) = {
      for {
        u <- authUsers.filter { it =>
          List(
            username.map(it.username === _),
            identity.map(it.identity === _),
            fullName.map(it.fullName === _),
            gender.map(it.gender === _),
            isSuperuser.map(it.isSuperuser === _),
            isStaff.map(it.isStaff === _),
            isActive.map(it.isActive === _)
          ).collect({ case Some(it) => it }).reduceLeftOption(_ && _).getOrElse(LiteralColumn(1) === LiteralColumn(1))
        }
      } yield u
    }

    def countByFilter(username: Option[String], identity: Option[String], fullName: Option[String], gender: Option[String],
                      isSuperuser: Option[Boolean], isStaff: Option[Boolean], isActive: Option[Boolean]): Future[Int] = {
      val q = filter(username, identity, fullName, gender, isSuperuser, isStaff, isActive)
      db.run(q.length.result).toTwitterFuture
    }

    def findAllByFilter(limit: Int, offset: Int, username: Option[String], identity: Option[String],
                        fullName: Option[String], gender: Option[String], isSuperuser: Option[Boolean], isStaff: Option[Boolean],
                        isActive: Option[Boolean], ordering: Option[String]): Future[Seq[AuthUserEntity]] = {
      val q = filter(username, identity, fullName, gender, isSuperuser, isStaff, isActive)
      (limit, offset) match {
        case (All, o) =>
          db.run(q.drop(offset).sortBy(_.id.asc.nullsLast).result).toTwitterFuture
        case (l, o) =>
          db.run(q.sortBy(_.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
      }
    }

    def findPassword(userId: Int): Future[Option[String]] = {
      val q = for {u <- authUsers if u.id === userId} yield u.password
      db.run(q.result.headOption).toTwitterFuture
    }

    def updateUserPassword(userId: Int, password: String): Future[Int] = {
      val q = for {u <- authUsers if u.id === userId} yield (u.password, u.updateAt)
      db.run(q.update((password, DateTime.now(DateTimeZone.forOffsetHours(8))))).toTwitterFuture
    }

    def updateUser(userId: Int, identity: String = "", fullName: String = "", gender: String, dob: Option[Date],
                   email: Option[String], mobile: Option[String], address: Option[String], postcode: Option[String],
                   profileUrl: Option[String], isStaff: Boolean, isActive: Boolean): Future[Int] = {
      val q = for {u <- authUsers if u.id === userId} yield (u.identity, u.fullName, u.gender, u.dob, u.email,
        u.mobile, u.address, u.postcode, u.profileUrl, u.isStaff, u.isActive, u.updateAt)
      db.run(q.update((identity, fullName, gender, dob, email, mobile, address, postcode, profileUrl, isStaff, isActive,
        DateTime.now(DateTimeZone.forOffsetHours(8))))).toTwitterFuture
    }
  }
}
