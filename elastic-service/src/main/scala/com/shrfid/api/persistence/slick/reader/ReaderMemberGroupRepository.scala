package com.shrfid.api.persistence.slick.reader

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule._
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}

/**
  * Created by jiejin on 8/09/2016.
  */

case class ReaderMemberGroupEntity(id: Int, memberId: Int, groupId: Int) extends BaseEntity

class ReaderMemberGroupTable(tag: Tag) extends BaseTable[ReaderMemberGroupEntity](tag, "reader_member_groups") {

  def memberId = column[Int]("member_id")

  def groupId = column[Int]("group_id")

  override def * = (id, memberId, groupId) <>((ReaderMemberGroupEntity.apply _).tupled, ReaderMemberGroupEntity.unapply)

  def member = foreignKey("reader_member_groups_member_id_fk_reader_member_id", memberId, TableQuery[ReaderMemberTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def group = foreignKey("reader_member_groups_user_id_fk_reader_group_id", groupId, TableQuery[ReaderGroupTable])(_.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade)

  def idx = index("reader_member_groups_uniq", (memberId, groupId), unique = true)
}

@Singleton
class ReaderMemberGroupRepository @Inject()(db: SlickDatabaseSource) {
  lazy val readerMemberGroups = TableQuery[ReaderMemberGroupTable]
  val dal = new BaseDalImpl[ReaderMemberGroupTable, ReaderMemberGroupEntity](db)(readerMemberGroups) {}
}