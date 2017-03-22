package com.shrfid.api.persistence.slick.reader

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.Tag

/**
  * Created by jiejin on 7/09/2016.
  */
case class ReaderLevelEntity(id: Int,
                             name: String,
                             deposit: BigDecimal,
                             generalBookRule: String, //quantity, days
                             journalRule: String, //quantity, days
                             ancientBookRule: String, //quantity, days
                             otherMediaRule: String, //quantity, days
                             penaltyRule: String, //method, yuan/day, times
                             canRenew: Boolean,
                             canBook: Boolean) extends BaseEntity


class ReaderLevelTable(tag: Tag) extends BaseTable[ReaderLevelEntity](tag, "reader_level") {
  def name = column[String]("name", O.Length(80))

  def deposit = column[BigDecimal]("deposit", O.Default[BigDecimal](0.00), O.SqlType("DECIMAL(10,2)"))

  def generalBookRule = column[String]("general_book_rule", O.Length(80))

  def journalRule = column[String]("journal_rule", O.Length(80))

  def ancientBookRule = column[String]("ancient_book_rule", O.Length(80))

  def otherMediaRule = column[String]("other_media_rule", O.Length(80))

  def penaltyRule = column[String]("penalty_rule", O.Length(80))

  def canRenew = column[Boolean]("can_renew", O.Default(false))

  def canBook = column[Boolean]("can_book", O.Default(false))

  //def organizationId = column[Int]("organization_id")
  override def * = (id, name, deposit, generalBookRule, journalRule, ancientBookRule, otherMediaRule, penaltyRule,
    canRenew, canBook) <>((ReaderLevelEntity.apply _).tupled, ReaderLevelEntity.unapply)

  def idx = index("reader_level_name_uniq", name, unique = false)
}

@Singleton
class ReaderLevelRepository @Inject()(db: SlickDatabaseSource) {
  lazy val readerLevels = TableQuery[ReaderLevelTable]
  val dal = new BaseDalImpl[ReaderLevelTable, ReaderLevelEntity](db)(readerLevels) {
  }
}
