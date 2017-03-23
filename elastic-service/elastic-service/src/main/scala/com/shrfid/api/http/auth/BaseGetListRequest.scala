package com.shrfid.api.http.auth

import java.sql.Date

import com.shrfid.api._
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._
import shapeless.HList._
import shapeless._
import shapeless.syntax.std.traversable._

/**
  * Created by jiejin on 16/11/16.
  */

abstract class BaseGetListRequest(limit: Int, offset: Int) {
  type Delimiter = String

  type ResultTranslator[T, R] = (Seq[T]) => R

  def stringSep(str: String, delimiter: String): Seq[String] = str.split(delimiter)

  private def to[T, R](str: String, typeTrans: (String) => T, resultTrans: ResultTranslator[T, R], delimiter: Delimiter) = {
    resultTrans(stringSep(str, delimiter.distinct).map(typeTrans))
  }

  def to[T, R](str: Option[String], t: (String) => T, r: ResultTranslator[T, R], d: Delimiter = ","): Option[R] = str match {
    case Some("") => None
    case Some(s) => Some(to(s, t, r, d))
    case None => None
  }

  def toDateRangeFilter(str: Option[String], d: Delimiter = ","): Option[(Date, Date)] = {
    str match {
      case Some("") => None
      case None => None
      case Some(end) if end.startsWith(d) => Some((Date.valueOf("0000-01-01"), Date.valueOf(end.replace(d, ""))))
      case Some(start) if start.endsWith(",") => Some((Date.valueOf(start.replace(d, "")), today))
      case Some(single) if !single.contains(d) => Some((Date.valueOf(single.replace(d, "")), Date.valueOf(single.replace(d, ""))))
      case Some(s) => to[Date, (Date, Date)](str, (s: String) => Date.valueOf(s), (r: Seq[Date]) => r.toHList[Date :: Date :: HNil].get.tupled, d)

    }
  }

  def toDateTimeRangeFilter(str: Option[String], d: Delimiter = ","): Option[(DateTime, DateTime)] = {
    str match {
      case Some("") => None
      case None => None
      case Some(end) if end.startsWith(d) => Some((DateTime.parse("0000-01-01"), DateTime.parse(end.replace(d, ""))))
      case Some(start) if start.endsWith(",") || !start.contains(d) => Some((DateTime.parse(start.replace(d, "")), Time.now))
      case Some(timeNotSpecified) if timeNotSpecified.split(d).distinct.length == 1 => Some((Time.startOfDay, Time.endOfDay))
      case Some(s) => to[DateTime, (DateTime, DateTime)](str, (s: String) => DateTime.parse(s), (r: Seq[DateTime]) => r.toHList[DateTime :: DateTime :: HNil].get.tupled, d)

    }
  }

  def toInSetStrFilter(str: String, d: Delimiter): Option[Seq[String]] = {
    str match {
      case "" => None
      case s => Some(stringSep(s, d).distinct)
    }

  }

  def toInSetStrFilter(str: Option[String], d: Delimiter = ","): Option[Seq[String]] = {
    str match {
      case Some("") => None
      case None => None
      case Some(s) => toInSetStrFilter(s, d)
    }
  }

  def toInSetIntFilter(str: String, d: Delimiter): Option[Seq[Int]] = {

    stringSep(str, d).filter(isInt _) match {
      case empty if empty.isEmpty => None
      case other => Some(other.distinct.map(_.toInt))
    }
  }

  def toInSetIntFilter(str: Option[String], d: Delimiter = ","): Option[Seq[Int]] = {
    str match {
      case Some("") => None
      case None => None
      case Some(s) => toInSetIntFilter(s, d)
    }
  }

  val queryFilter: Map[String, _] = Map(
    "limit" -> 100,
    "offset" -> 0
  )


  implicit def pageFormat[T: Format]: Format[Paging[T]] = (
    (__ \ 'count).format[Int] and
      (__ \ 'prev).format[String] and
      (__ \ 'next).format[String] and
      (__ \ 'content).format[Seq[T]]
    ) (Paging.apply[T], unlift(Paging.unapply[T]))

  protected case class Paging[T](var count: Int,
                                 var prev: String,
                                 var next: String,
                                 var content: Seq[T])(implicit val tFormat: Format[T]) {
    def stringify = Json.stringify(Json.toJson(this))
  }

  protected def requestPath(path: String): String

  private val paging = (path: String, limit: Int, offset: Int) => path + "&offset=" + offset + "&limit=" + limit
  val next = (path: String, count: Int) =>
    (if (offset + limit >= count || limit <= 0) "" else paging(path, if (limit > count) count else limit, offset + limit)).replace("+", "%2B")
  val prev = (path: String, count: Int) =>
    (if (offset <= 0 || offset > count || limit <= 0) "" else paging(path, if (limit > count) count else limit, if ((offset - limit) > 0) offset - limit else 0)).replace("+", "%2B")


}
