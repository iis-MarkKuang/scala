package com.shrfid.api.persistence.slick.auth

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.modules.SlickDatabaseModule._
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import com.twitter.util.Future
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}

/**
  * Created by jiejin on 7/09/2016.
  */
case class AuthPermissionEntity(id: Int, appLabel: String, name: String, path: String) extends BaseEntity

class AuthPermissionTable(tag: Tag) extends BaseTable[AuthPermissionEntity](tag, "auth_permission") {
  def appLabel = column[String]("app_label", O.Length(100))

  def name = column[String]("name", O.Length(100))

  def path = column[String]("path", O.Length(100))

  override def * = (id, appLabel, name, path) <>((AuthPermissionEntity.apply _).tupled, AuthPermissionEntity.unapply)

  def idx = index("auth_permission_app_label_name_path_uniq", (appLabel, name, path), unique = true)
}

@Singleton
class AuthPermissionRepository @Inject()(db: SlickDatabaseSource) {
  lazy val adminContentTypes = TableQuery[AuthPermissionTable]
  val dal = new BaseDalImpl[AuthPermissionTable, AuthPermissionEntity](db)(adminContentTypes) {
    def findAllIds: Future[Seq[Int]] = {
      val q = for {
        p <- adminContentTypes
      } yield p.id
      db.run(q.result).toTwitterFuture
    }
  }
}
