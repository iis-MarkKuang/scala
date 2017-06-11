package com.shrfid.api.http.auth

import play.api.libs.functional.syntax._
import play.api.libs.json._

/**
  * Created by jiejin on 20/10/16.
  */

/*case class ListResponse[R](count: Int=0, prev: String="", next: String="", content: Seq[R]= Seq())

trait BaseListResponse[R] {
  implicit def fmt[R] = JsonNaming.snakecase(Json.format[ListResponse[R]])
  def toHttpResponse(content: Seq[R], path: String, limit: Int, offset: Int, count: Int): Future[String] = {
    val obj = ListResponse[R](count,prev(path, limit, offset, count), next(path, limit, offset, count), content)
    Future.value(Json.stringify(Json.toJson(obj)(fmt)))
  }
}*/



case class Paging[T](var count: Int,
                     var prev: String,
                     var next: String,
                     var content: Seq[T])(implicit val tFormat: Format[T]) {

}

object Paging {
  private val paging = (path: String, limit: Int, offset: Int) => path + "&offset=" + offset + "&limit=" + limit
  val next = (path: String, limit: Int, offset: Int, count: Int) =>
    if (offset + limit >= count || limit <= 0) "" else paging(path, if (limit > count) count else limit, offset + limit)
  val prev = (path: String, limit: Int, offset: Int, count: Int) =>
    if (offset <= 0 || offset > count || limit <= 0) "" else paging(path, if (limit > count) count else limit, if ((offset - limit) > 0) offset - limit else 0)

  implicit def pageFormat[T: Format]: Format[Paging[T]] = (
    (__ \ 'count).format[Int] and
      (__ \ 'prev).format[String] and
      (__ \ 'next).format[String] and
      (__ \ 'content).format[Seq[T]]
    ) (Paging.apply[T], unlift(Paging.unapply[T]))

}