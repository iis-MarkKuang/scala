package com.shrfid.api.persistence.slick.reader

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
case class ReaderGroupEntity(id: Int,
                             name: String,
                             isActive: Boolean) extends BaseEntity


class ReaderGroupTable(tag: Tag) extends BaseTable[ReaderGroupEntity](tag, "reader_group") {
  def name = column[String]("name", O.Length(80))
  def isActive = column[Boolean]("is_active", O.Default(true))
  override def * = (id, name, isActive) <>((ReaderGroupEntity.apply _).tupled, ReaderGroupEntity.unapply)
  def idx = index("reader_group_name_uniq", name, unique = false)
}

@Singleton
class ReaderGroupRepository @Inject()(db: SlickDatabaseSource) {
  lazy val readerGroups = TableQuery[ReaderGroupTable]
  val dal = new BaseDalImpl[ReaderGroupTable, ReaderGroupEntity](db)(readerGroups) {

    def isActiveFilter(isActive: Option[Boolean]) = {
      for {
        r <- readerGroups.filter { it =>
          List(
            isActive.map(it.isActive === _)
          ).collect({ case Some(it) => it }).reduceLeftOption(_ || _).getOrElse(LiteralColumn(1) === LiteralColumn(1))
        }
      } yield r
    }

    def count(isActive: Option[Boolean]): Future[Int] = {
      db.run(isActiveFilter(isActive).length.result).toTwitterFuture
    }

    def findAllByActive(isActive: Option[Boolean], limit: Int = 100, offset: Int = 0): Future[Seq[ReaderGroupEntity]] = {
      (limit, offset) match {
        case (All, o) => db.run(isActiveFilter(isActive).drop(offset).sortBy(_.id.asc.nullsLast).result).toTwitterFuture
        case (l, o) => db.run(isActiveFilter(isActive).sortBy(_.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
      }
    }
  }
}

