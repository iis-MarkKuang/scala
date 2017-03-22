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

case class AuthUserPermissionEntity(id: Int, userId: Int, permissionId: Int) extends BaseEntity

class AuthUserPermissionTable(tag: Tag) extends BaseTable[AuthUserPermissionEntity](tag, "auth_user_permissions") {
  def userId = column[Int]("user_id")

  def permissionId = column[Int]("permission_id")

  override def * = (id, userId, permissionId) <>((AuthUserPermissionEntity.apply _).tupled, AuthUserPermissionEntity.unapply)

  def user = foreignKey("auth_user_permission_user_id_fk_auth_user_id", userId, TableQuery[AuthUserTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def permission = foreignKey("auth_user_permission_user_id_fk_auth_permission_id", permissionId, TableQuery[AuthPermissionTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def idx = index("auth_user_permissions_uniq", (userId, permissionId), unique = true)
}


@Singleton
class AuthUserPermissionRepository @Inject()(db: SlickDatabaseSource) {
  lazy val authUserPermissions = TableQuery[AuthUserPermissionTable]
  lazy val authPermissions = TableQuery[AuthPermissionTable]
  val dal = new BaseDalImpl[AuthUserPermissionTable, AuthUserPermissionEntity](db)(authUserPermissions) {
    def findPermissionsByUserId(userId: Int): Future[Seq[AuthPermissionEntity]] = {
      val q = for {
        p <- authUserPermissions if p.userId === userId
        authPermissions <- authPermissions if authPermissions.id === p.permissionId
      } yield authPermissions
      db.run(q.result).toTwitterFuture
    }

    def findPermissionIdsByUserId(userId: Int): Future[Seq[Int]] = {
      val q = for {
        p <- authUserPermissions if p.userId === userId
        authPermissions <- authPermissions if authPermissions.id === p.permissionId
      } yield authPermissions.id
      db.run(q.result).toTwitterFuture
    }
  }
}
