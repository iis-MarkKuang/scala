package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.domains.reader.ReaderMember
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future
import com.shrfid.api._
import com.twitter.finagle.http.Status
import play.api.libs.json.Json

/**
  * Created by jiejin on 8/12/16.
  */
@Singleton
class ReaderMemberRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "reader"
  val _type = "info"

  val dal = new BaseDalImpl[ReaderMember](db)(_index, _type) {
    override def id(doc: ReaderMember): Future[String] = Future.value(doc.identity)

    def findIdsByGroupId(groupIds: Seq[String]): Future[Seq[String]] = {
      val query = search(_index / _type) query {
        boolQuery().should(groupIds.distinct.map(g=> matchPhraseQuery("groups", g))).not(matchPhraseQuery("is_active", false))
      } fetchSource false start 0 limit elasticSearchLimit
      //println(query.show)
      db.execute(query).toTwitterFuture.map(a => a.ids.distinct)
    }

    def deductCredit(id: String, deduction: String, now: String = Time.now.toString): Future[UpdateResponse] = {
      val getDepositQuery = search(_index / _type) query {
        matchQuery("_id", id)
      }
      val credit_future = db.execute(getDepositQuery).toTwitterFuture.map(a =>
      a.hits.headOption match {
          case None => 0.00
          case Some(s) => Json.parse(s.sourceAsString).as[ReaderMember].credit
        })
      credit_future flatMap {
        case r if r == 0.00 => Future((404, valueJson("error", "reader not found")))
        case value => BigDecimal(value - deduction.toDouble).setScale(2, BigDecimal.RoundingMode.HALF_EVEN) match {
          case minus if minus < 0 =>
            Future((402, valueJson("error", s"Not enough credit, please charge, you need ${minus.abs} yuan to pay your fine.")))
          case others =>
            updateDocWithResult(id, Map("credit" -> others.toDouble, "datetime" -> now), "credit_left", others.toString)
        }
      }
    }

    // added by kuangyuan 5/2/2017
    def findReaderMembersByLevelId(level_id: String): Future[SearchResponse] = {
      db.execute(
        search(_index / _type) query(
          boolQuery().must(Seq(termQuery("level", level_id), termQuery("is_active", true)))
        ) fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case Some(s) => (Status.Ok.code, valueJsonArrayObject("reader_ids", s"[${a.ids.map(id => '"' + id + '"').mkString(",")}]"))
        case None => (Status.NotFound.code, valueJson("error", "Not Found"))
      })
    }

    def findIdsByLevelIds(levelIds: Seq[String]): Future[Seq[String]] = {
      val query = search(_index / _type) query {
        boolQuery().should(levelIds.distinct.map(g => matchPhraseQuery("level", g))).not(matchPhraseQuery("is_active", false))
      } fetchSource false start 0 limit elasticSearchLimit

      db.execute(query).toTwitterFuture.map(a => a.ids.distinct)
    }

    def findReaderMemberByNewLevelId(new_level_id: String): Future[SearchResponse] = {
      db.execute(
        search(_index / _type) query(
          boolQuery().must(Seq(termQuery("level_new", new_level_id), termQuery("is_active", true)))
          ) fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case Some(s) => (Status.Ok.code, valueJsonArrayObject("reader_ids", "[" + a.ids.map(id => '"' + id + '"').mkString(",") + "]"))
        case None => (Status.NotFound.code, valueJson("error", "Not Found"))
      })
    }

    def findIdsByNewLevelIds(newLevelIds: Seq[String]): Future[Seq[String]] = {
      val query = search(_index / _type) query {
        boolQuery().should(newLevelIds.distinct.map(g => matchPhraseQuery("level_new", g))).not(matchPhraseQuery("is_active", false))
      } fetchSource false start 0 limit elasticSearchLimit

      db.execute(query).toTwitterFuture.map(a => a.ids.distinct)
    }

    // added by kuangyuan 5/4/2017
    def findReaderMemberByBarcode(barcode: String): Future[SearchResponse] = {
      db.execute(
      search(_index / _type) termQuery("barcode", barcode)
      ).toTwitterFuture.map(a => a.hits.headOption match {
        case Some(s) => (Status.Ok.code, s.sourceAsString)
        case None => (Status.NotFound.code, "Not Found")
      })
    }


  }


}
