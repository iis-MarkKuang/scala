package com.shrfid.api.persistence.slick

/**
  * Created by jiejin on 6/09/2016.
  */

import com.google.inject.Inject
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.twitter.util.Future
import slick.driver.MySQLDriver.api._
import slick.lifted.{CanBeQueryCondition, TableQuery, Tag}
import com.shrfid.api.persistence._

import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success}

trait BaseEntity {
  val id: Int

  def isValid: Boolean = true

}

abstract class BaseTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
}


trait BaseDal[T, A] {

  def insert(row: A): Future[Int]

  def insert(rows: Seq[A]): Future[Seq[Int]]

  def update(row: A): Future[Int]

  def update(rows: Seq[A]): Future[Unit]

  def findAll(offset: Int = 0, limit: Int = 100): Future[Seq[A]]

  def findAllByFilter[C: CanBeQueryCondition](f: (T) => C, limit: Int = 100, offset: Int = 0): Future[Seq[A]]

  def findAllByFilterA[C: CanBeQueryCondition](f: (T) => C, n: (T) => C, limit: Int = 100, offset: Int = 0): Future[Seq[A]]

  def findById(id: Int): Future[Option[A]]

  def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]]

  def deleteById(id: Int): Future[Int]

  def deleteById(ids: Seq[Int]): Future[Int]

  def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int]

  def count: Future[Int]

  def countByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int]

  def createTable(): Future[Unit]

}

class BaseDalImpl[T <: BaseTable[A], A <: BaseEntity] @Inject()(db: SlickDatabaseSource)(tableQ: TableQuery[T]) extends BaseDal[T, A] {


  override def insert(row: A): Future[Int] = {
    insert(Seq(row)).map(_.head)
  }

  override def insert(rows: Seq[A]): Future[Seq[Int]] = {
    db.run((tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid)).asTry).map {
      case Success(res) => res
      case Failure(e) => print(e); Seq(0)
    }.toTwitterFuture
  }

  override def update(row: A): Future[Int] = {
    if (row.isValid)
      db.run(tableQ.filter(_.id === row.id).update(row)).toTwitterFuture
    else
      Future {
        0
      }
  }

  override def update(rows: Seq[A]): Future[Unit] = {
    db.run(DBIO.seq(rows.filter(_.isValid).map(r => tableQ.filter(_.id === r.id).update(r)): _*)).toTwitterFuture
  }

  override def findAll(limit: Int = 100, offset: Int = 0): Future[Seq[A]] = {
    (limit, offset) match {
      case (All, o) => db.run(tableQ.drop(offset).sortBy(_.id.asc.nullsLast).result).toTwitterFuture
      case (l, o) => db.run(tableQ.sortBy(_.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
    }
  }

  override def findAllByFilter[C: CanBeQueryCondition](f: (T) => C, limit: Int = 100, offset: Int = 0): Future[Seq[A]] = {
    (limit, offset) match {
      case (All, o) => db.run(tableQ.withFilter(f).drop(offset).sortBy(_.id.asc.nullsLast).result).toTwitterFuture
      case (l, o) => db.run(tableQ.withFilter(f).sortBy(_.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
    }
  }

  override def findAllByFilterA[C: CanBeQueryCondition](f: (T) => C, n: (T) => C, limit: Int = 100, offset: Int = 0) = {
    (limit, offset) match {
      case (All, o) => db.run(
        tableQ.withFilter(f).++(tableQ.withFilter(n)).drop(offset).sortBy(_.id.asc.nullsLast).result).toTwitterFuture
      case (l, o) => db.run(tableQ.withFilter(f).withFilter(n).sortBy(_.id.asc.nullsLast).drop(offset).take(limit).result).toTwitterFuture
    }
  }

  override def findById(id: Int): Future[Option[A]] = {
    db.run(tableQ.filter(_.id === id).result.headOption).toTwitterFuture
  }

  override def findByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = {
    db.run(tableQ.withFilter(f).result).toTwitterFuture
  }

  override def deleteById(id: Int): Future[Int] = {
    deleteById(Seq(id))
  }

  override def deleteById(ids: Seq[Int]): Future[Int] = {
    db.run(tableQ.filter(_.id.inSet(ids)).delete).toTwitterFuture
  }

  override def deleteByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int] = {
    db.run(tableQ.withFilter(f).delete).toTwitterFuture
  }

  override def count: Future[Int] = {
    db.run(tableQ.length.result).toTwitterFuture
  }

  override def countByFilter[C: CanBeQueryCondition](f: (T) => C): Future[Int] = {
    db.run(tableQ.withFilter(f).length.result).toTwitterFuture
  }

  override def createTable(): Future[Unit] = {
    db.run(DBIO.seq(tableQ.schema.create)).toTwitterFuture
  }

}
