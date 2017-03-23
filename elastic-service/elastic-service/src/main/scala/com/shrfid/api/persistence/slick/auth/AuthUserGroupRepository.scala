package com.shrfid.api.persistence.slick.auth

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}

/**
  * Created by jiejin on 7/09/2016.
  */
case class AuthUserGroupEntity(id: Int, userId: Int, groupId: Int) extends BaseEntity

class AuthUserGroupTable(tag: Tag) extends BaseTable[AuthUserGroupEntity](tag, "auth_user_groups") {
  def userId = column[Int]("user_id")

  def groupId = column[Int]("group_id")

  override def * = (id, userId, groupId) <>((AuthUserGroupEntity.apply _).tupled, AuthUserGroupEntity.unapply)

  def user = foreignKey("auth_user_groups_user_id_fk_auth_user_id", userId, TableQuery[AuthUserTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def group = foreignKey("auth_user_groups_user_id_fk_auth_group_id", groupId, TableQuery[AuthGroupTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def idx = index("auth_user_groups_uniq", (userId, groupId), unique = true)
}

@Singleton
class AuthUserGroupRepository @Inject()(db: SlickDatabaseSource) {
  lazy val authUserGroups = TableQuery[AuthUserGroupTable]
  val dal = new BaseDalImpl[AuthUserGroupTable, AuthUserGroupEntity](db)(authUserGroups) {
  }
}
