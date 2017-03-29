package com.shrfid.api.persistence.slick.readable

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable

case class PeriodicalStackEntity(id: Int, name: String, isActive: Boolean, description: Option[String]) extends BaseEntity

class PeriodicalStackTable(tag: Tag) extends BaseTable[PeriodicalStackEntity](tag, "periodical_stack") {
  def name = column[String]("name", O.Length(80))

  def isActive = column[Boolean]("is_active", O.Default(true))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  override def * = (id, name, isActive, description) <> ((PeriodicalStackEntity.apply _).tupled, PeriodicalStackEntity.unapply)

  def idx = index("periodical_stack_name_alias_uniq", name, unique = true )
}

@Singleton
class PeriodicalStackRepository @Inject()(db: SlickDatabaseSource) {
  lazy val periodicalStacks = TableQuery[PeriodicalStackTable]
  val dal = new BaseDalImpl[PeriodicalStackTable, PeriodicalStackEntity](db)(periodicalStacks) {}
}