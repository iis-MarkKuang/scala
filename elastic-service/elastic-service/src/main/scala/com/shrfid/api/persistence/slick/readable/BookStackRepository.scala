package com.shrfid.api.persistence.slick.readable

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule._
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.{TableQuery, Tag}
import slick.profile.SqlProfile.ColumnOption.Nullable

/**
  * Created by jiejin on 19/09/2016.
  */
case class BookStackEntity(id: Int, name: String, isActive: Boolean, description: Option[String]) extends BaseEntity

class BookStackTable(tag: Tag) extends BaseTable[BookStackEntity](tag, "book_stack") {
  def name = column[String]("name", O.Length(80))

  def isActive = column[Boolean]("is_active", O.Default(true))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  override def * = (id, name, isActive, description) <>((BookStackEntity.apply _).tupled, BookStackEntity.unapply)

  def idx = index("book_stack_name_alias_uniq", name, unique = true)
}


@Singleton
class BookStackRepository @Inject()(db: SlickDatabaseSource) {
  lazy val bookStacks = TableQuery[BookStackTable]
  val dal = new BaseDalImpl[BookStackTable, BookStackEntity](db)(bookStacks) {}
}