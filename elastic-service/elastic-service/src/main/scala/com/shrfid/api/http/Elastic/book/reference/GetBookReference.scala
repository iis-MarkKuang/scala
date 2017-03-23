package com.shrfid.api.http.Elastic.book.reference

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}

/**
  * Created by jiejin on 11/11/16.
  */
case class GetBookReferenceListRequest(@Header Authorization: String,
                                       @QueryParam limit: Int = 100,
                                       @QueryParam offset: Int = 0,
                                       @QueryParam id: Option[String],
                                       @QueryParam keyword: Option[String],
                                       @QueryParam author: Option[String],
                                       @QueryParam title: Option[String],
                                       @QueryParam isbn: Option[String],
                                       @QueryParam publisher: Option[String],
                                       @QueryParam clc: Option[String],
                                       @QueryParam publishYear: Option[String],
                                       @QueryParam topic: Option[String],
                                       @QueryParam ordering: Option[String]
                                      ) extends BaseGetListRequest(Authorization, limit, offset, ordering) {

  val idQueryParam = UrlQueryParam("_id", id)
  val keywordQueryParam = UrlQueryParam("keywords.pinyin", keyword, "title")
  val authorQueryParam = UrlQueryParam("责任者.主标目", author, "author")
  val titleQueryParam = UrlQueryParam("题名与责任者.正题名", title, "title")
  val isbnQueryParam = UrlQueryParam("ISBN.ISBN", isbn, "isbn")
  val publisherQueryParam = UrlQueryParam("出版发行.出版发行者名称", publisher, "publisher")
  val clcQueryParam = UrlQueryParam("中国图书馆图书分类法分类号", clc, "clc")
  val topicQueryParam = UrlQueryParam("普通主题.主标目", topic, "topic")
  val publishYearQueryParam = UrlQueryParam("出版发行.出版发行日期", publishYear, "publish_year")

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    keywordQueryParam.matchQ,
    authorQueryParam.matchPhraseQ,
    titleQueryParam.matchPhraseQ,
    isbnQueryParam.matchPhraseQ,
    publisherQueryParam.matchPhraseQ,
    clcQueryParam.matchPhraseQ,
    topicQueryParam.matchPhraseQ,
    publishYearQueryParam.rangeQ
  ))

  val path = Router.Reference.list

  override val requestPath: String = _requestPath(Seq(idQueryParam, keywordQueryParam, authorQueryParam,
    titleQueryParam, isbnQueryParam, publisherQueryParam, clcQueryParam, topicQueryParam, publishYearQueryParam))
}

case class GetBookReferenceById (@Header Authorization: String,
                                 @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id)



case class GetBookCatalogingByISBNRequest (@RouteParam isbn: String)