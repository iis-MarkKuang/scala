package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.domains.book.BookStack
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.twitter.util.Future
import org.elasticsearch.action.search.SearchResponse

import com.sksamuel.elastic4s.ElasticDsl._
import scala.language.postfixOps

/**
  * Created by jiejin on 25/11/16.
  */


@Singleton
class BookStackRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "stack"
  val _type = "info"

  val dal = new BaseDalImpl[BookStack](db)(_index, _type) {
    override def id(doc: BookStack): Future[String] = Future.value(Security.digest(doc.toString))

    // Added by kuangyuan 5/1/2017
    def findActiveStacksUnderBranchId(branchId: String): Future[(StatusCode, Docs)] = {
      db.execute(
        search(_index / _type) query (
          // stack 的 is_active有二义性，被用作"流通"用，所以这里删除term条件
          boolQuery().must(Seq(termQuery("branch", branchId)
//            , termQuery("is_active", true)
          ))
        ) fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case None => (404, valueJson("result", "Not found"))
        case Some(s) => (200, valueJsonArrayObject("result", "[" + a.ids.map(id => '"' + id + '"').mkString(",") + "]"))
      })
    }
    //override def findAllDocs(_filter: Seq[Product with Serializable], _order: Option[String], _limit: Int, _offset: Int): Future[(StatusCode, (Count, Docs))] = ???
  }
}