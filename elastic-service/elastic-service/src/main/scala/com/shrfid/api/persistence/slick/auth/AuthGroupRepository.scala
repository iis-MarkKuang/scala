package com.shrfid.api.persistence.slick.auth

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import com.twitter.util.Future
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import com.shrfid.api.persistence._

/**
  * Created by jiejin on 7/09/2016.
  */
case class AuthGroupEntity(id: Int, name: String) extends BaseEntity

case class AuthGroupNestedEntity(id: Int, name: String, permissions: Seq[AuthPermissionEntity])

class AuthGroupTable(tag: Tag) extends BaseTable[AuthGroupEntity](tag, "auth_group") {
  def name = column[String]("name", O.Length(80))

  override def * = (id, name) <>((AuthGroupEntity.apply _).tupled, AuthGroupEntity.unapply)

  def idx = index("auth_group_name_uniq", (name), unique = true)
}

@Singleton
class AuthGroupRepository @Inject()(db: SlickDatabaseSource) {
  lazy val authGroups = TableQuery[AuthGroupTable]
  lazy val authGroupPermissions = TableQuery[AuthGroupPermissionTable]
  lazy val authPermissions = TableQuery[AuthPermissionTable]
  val dal = new BaseDalImpl[AuthGroupTable, AuthGroupEntity](db)(authGroups) {
    def findAllNested(limit: Int = 100, offset: Int = 0): Future[Seq[AuthGroupNestedEntity]] = {
      val raw = (limit, offset) match {
        case (All, o) => db.run((for {
          g <- authGroups.drop(o).sortBy(_.id.asc.nullsLast)
          gp <- authGroupPermissions if g.id === gp.groupId
          p <- authPermissions if gp.permissionId === p.id
        } yield (g, p)).result).toTwitterFuture
        case (l, o) => db.run((for {
          g <- authGroups.drop(o).take(l).sortBy(_.id.asc.nullsLast)
          gp <- authGroupPermissions if g.id === gp.groupId
          p <- authPermissions if gp.permissionId === p.id
        } yield (g, p)).result).toTwitterFuture
      }
      raw.map(a => a.groupBy(_._1).map(g => AuthGroupNestedEntity(g._1.id, g._1.name, g._2.map(_._2))).toSeq)

    }
  }
}