package com.shrfid.api.persistence.elastic4s


import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.readable.Periodical
import com.sksamuel.elastic4s.ElasticDsl._
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.twitter.util.Future
import org.elasticsearch.search.sort.SortOrder
/**
  * Created by kuang on 2017/3/28.
  */
@Singleton
class PeriodicalItemRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "periodical"
  val _type = "info"

  val dal = new BaseDalImpl[Periodical](db)(_index, _type) {
    override def id(doc: Periodical): Future[String] = Future.value(doc.barcode.toString)

    override def upsertDoc(_id: String, _doc: Periodical): Future[UpsertResponse] = {
      db.execute(
        update(_doc.barcode.toString).in(_index / _type).docAsUpsert(_doc.jsonStringify)
      ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
    }

    def findNextPeriodicalIndex(clc: String) = {
      db.execute(
        search(_index / _type) termQuery("clc.keyword", clc) fetchSource (false) aggregations (
          maxAggregation("periodical_index").field("periodical_index")
        )
      ).toTwitterFuture.map(a => a.aggregations.maxResult("periodical_index").value() match {
        case a if a.isInfinity => 1
        case b => b.toInt
      })
    }

    def isAvailable(_id: String): Future[Boolean] = {
      db.execute(indexExists("periodical")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) termQuery("_id", _id) termQuery("is_available", true) limit 1
            ).toTwitterFuture.map(a =>
              a.hits.headOption match {
                case None => false
                case Some(s) => println(s.sourceAsMap("_id")); println(s.sourceAsMap("is_available"))
                  true
              }
            )
          case false => Future(false)
        }
      }
    }

    def findMaxBarcode(category: String): Future[String] = {
      db.execute(indexExists("periodical")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
            search(_index / _type) termQuery("category.keyword", category) sortBy (Iterable(fieldSort("barcode") order SortOrder.DESC)) limit 1
          ).toTwitterFuture.map(a =>
            a.hits.headOption match {
              case None => barcodeFmt(0, periodicalCategoryId(category))
              case Some(s) => println(s.sourceAsMap("barcode")); s.sourceAsMap("barcode").toString
            })
        }
      }
    }

    // To be continued...

  }
}
