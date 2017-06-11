package com.shrfid.api.http.Elastic

import java.net.{URLDecoder, URLEncoder}

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.searches.sort.SortDefinition
import org.elasticsearch.search.sort.SortOrder

/**
  * Created by jiejin on 25/11/16.
  */
abstract class BaseGetListRequest(Authorization: String, limit: Int, offset: Int, ordering: Option[String]) {

  val delimiter = ","

  def getLimit = limit

  def getOffset = offset

  val path: String
  private val codec = "UTF-8"

  private val paging = (path: String, limit: Int, offset: Int) => path + "&offset=" + offset + "&limit=" + limit

  val next = (path: String, count: Int) =>
    (if (offset + limit >= count || limit <= 0) "" else paging(path, if (limit > count) count else limit, offset + limit)).replace("+", "%2B")

  val prev = (path: String, count: Int) =>
    (if (offset <= 0 || offset > count || limit <= 0) "" else paging(path, if (limit > count) count else limit, if ((offset - limit) > 0) offset - limit else 0)).replace("+", "%2B")

  //case class QueryUrlDefinition[T](query: Option[T], field:String)

  case class UrlQueryParam(field: String, query: Option[Any], urlString: String = "") {
    def toUrl =
      query match {
        case None => ""
        case Some(s) =>
          urlString match {
            case "" => s"&$field=${URLEncoder.encode(s.toString, codec)}"
            case other => s"&$other=${URLEncoder.encode(s.toString, codec)}"
          }

      }

    def matchPhraseQ =
      query match {
        case None =>
          None
        case Some(exact) if URLDecoder.decode(exact.toString, codec).startsWith("^") =>
          field match {
            case "_id" => matchPhraseQuery(field, URLDecoder.decode(exact.toString, codec).drop(1))
            case other => matchPhraseQuery(other + ".keyword", URLDecoder.decode(exact.toString, codec).drop(1))
          }

        case Some(i) =>
          matchPhraseQuery(field, URLDecoder.decode(i.toString, codec))
      }

    def matchQ =
      query match {
        case None =>
          None
        case Some(exact) if URLDecoder.decode(exact.toString, codec).startsWith("^") =>
          field match {
            case "_id" => matchQuery(field, URLDecoder.decode(exact.toString, codec).drop(1))
            case other => matchQuery(other + ".keyword", URLDecoder.decode(exact.toString, codec).drop(1))
          }

        case Some(i) =>
          matchQuery(field, URLDecoder.decode(i.toString, codec))
      }

    def matchBool =
      query match {
        case None => None
        case Some(i) => matchQuery(field, i)
      }

    def exist =
      query match {
        case None => None
        case Some(i) => existsQuery(field)
      }

    def rangeQ = {
      query match {
        case None =>
          None
        case Some(l: String) if l.startsWith(delimiter) => rangeQuery(field).lte(l.replace(delimiter, ""))
        case Some(g: String) if g.endsWith(delimiter) || !g.contains(delimiter) => rangeQuery(field).gte(g.replace(delimiter, ""))
        case Some(o: String) => val range = o.split(delimiter)
          rangeQuery(field).gte(range(0)).lte(range(1))
        case Some(_) => None
      }
    }

  }

  def _requestPath(params: Seq[UrlQueryParam]): String = {
    s"""$path?${params.map(_.toUrl).mkString("")}${UrlQueryParam("ordering", ordering).toUrl}"""
  }

  val requestPath: String

  case class QueryFilter(must: Seq[Product with Serializable] = Seq(), not: Seq[Product with Serializable] = Seq(), sourceExclude: Seq[String] = Seq())

  val filter: QueryFilter = QueryFilter()

  val sortBy = sortD(ordering)

  def response(count: Int, content: String) = {
    s"""{"count": $count, "prev": "${prev(requestPath, count)}", "next": "${next(requestPath, count)}", "content": $content}""".stripMargin
  }


  def sortD(_order: Option[String]): Option[Iterable[SortDefinition[_]]] = _order match {
    case None => None
    case Some(_orderBy) =>
      val _s = _orderBy.split(delimiter).map(_ match {
        case "" => None
        case "-datetime" => fieldSort("datetime") order SortOrder.DESC
        case "datetime" => fieldSort("datetime") order SortOrder.ASC
        case "-id" => None
        case "id" => None
        case a if a.startsWith("-") => fieldSort(a.drop(1) + ".keyword") order SortOrder.DESC
        case b => fieldSort(b + ".keyword") order SortOrder.ASC
      }).filterNot(_ == None).toSeq.asInstanceOf[Iterable[SortDefinition[_]]]
      Some(_s)
  }


}

abstract class BaseGetByIdRequest (Authorization: String, id: String) {
  val sourceInclude: Seq[String] = Seq("*")
  val sourceExclude: Seq[String] = Seq()
}