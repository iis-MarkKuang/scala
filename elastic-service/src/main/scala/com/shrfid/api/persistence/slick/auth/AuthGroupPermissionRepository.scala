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
case class AuthGroupPermissionEntity(id: Int, groupId: Int, permissionId: Int) extends BaseEntity

class AuthGroupPermissionTable(tag: Tag) extends BaseTable[AuthGroupPermissionEntity](tag, "auth_group_permissions") {
  def groupId = column[Int]("group_id")

  def permissionId = column[Int]("permission_id")

  override def * = (id, groupId, permissionId) <>((AuthGroupPermissionEntity.apply _).tupled, AuthGroupPermissionEntity.unapply)

  def group = foreignKey("auth_group_permission_user_id_fk_auth_group_id", groupId, TableQuery[AuthGroupTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def permission = foreignKey("auth_group_permission_user_id_fk_auth_permission_id", permissionId, TableQuery[AuthPermissionTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def idx = index("auth_group_permissions_uniq", (groupId, permissionId), unique = true)
}


@Singleton
class AuthGroupPermissionRepository @Inject()(db: SlickDatabaseSource) {
  lazy val authGroupPermissions = TableQuery[AuthGroupPermissionTable]
  lazy val authUserGroups = TableQuery[AuthUserGroupTable]
  lazy val authPermissions = TableQuery[AuthPermissionTable]
  val dal = new BaseDalImpl[AuthGroupPermissionTable, AuthGroupPermissionEntity](db)(authGroupPermissions) {
    def findPermissionsByUserId(userId: Int): Future[Seq[AuthPermissionEntity]] = {
      val q = for {
        g <- authUserGroups if g.userId === userId
        p <- authGroupPermissions if g.groupId === p.groupId
        authPermissions <- p.permission
      } yield authPermissions
      db.run(q.result).toTwitterFuture
    }

    def findGroupPermissionsByUserId(userId: Int): Future[Seq[AuthGroupPermissionEntity]] = {
      val q = for {
        g <- authUserGroups if g.userId === userId
        p <- authGroupPermissions if g.groupId === p.groupId
      } yield p
      db.run(q.result).toTwitterFuture
    }
  }
}
