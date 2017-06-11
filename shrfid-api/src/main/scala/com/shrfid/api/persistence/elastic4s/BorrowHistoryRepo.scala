package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book.{BorrowHistory, TimeLocation}
import com.sksamuel.elastic4s.ElasticDsl._
import com.shrfid.api.modules.Elastic4SModule._
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval._
import com.twitter.util.Future
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.Json
/**
  * Created by jiejin on 11/12/16.
  */

@Singleton
class BorrowHistoryRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "borrow"
  val _type = "info"

  val dal = new BaseDalImpl[BorrowHistory](db)(_index, _type) {
    override def id(doc: BorrowHistory): Future[String] = Future.value(Security.digest(doc.toString))

    def returnAt(_id: String, _return: TimeLocation, now: String) = {
      this.updateDoc(_id, Map("_return" -> Map("location" -> _return.location, "datetime" -> _return.datetime), "datetime" -> now, "status"-> "return"))
    }
    def renewAt(_id: String, _renew: TimeLocation, dueAt: String, now: String) = {
      this.updateDoc(_id, Map("_renew" -> Map("location" -> _renew.location, "datetime" -> _renew.datetime), "due_at" -> dueAt, "datetime" -> now, "status"-> "renew"))
    }

    def getReturnedDays(_barcode: String): Future[Long] = {
      db.execute(
        search(_index / _type) query {
          boolQuery().must(termQuery("book.barcode.keyword", _barcode),existsQuery("_return"))
        } sortBy (Iterable(fieldSort("_return.datetime") order SortOrder.DESC)) limit 1
      ).toTwitterFuture.map( a=> a.hits.headOption match {
        case None =>  100
        case Some(s) =>  Json.parse(s.sourceAsString).as[BorrowHistory]._return match {
          case None=>  100
          case Some(tt)=>  Time.daysFromNow(tt.datetime)
        }
      })
    }

    def getDateHistogramInterval(interval: String): DateHistogramInterval = {
      interval.toLowerCase match {
        case "day" => DAY
        case "week" => WEEK
        case "month" => MONTH
        case "quarter" => QUARTER
        case "year" => YEAR
      }
    }

    def getLimit(limitStr: String): Int = {
      val limit = limitStr.toInt
      if (limit > 10) 10 else limit
    }

    def getFlowDataByCategory(start_time: String, end_time: String, interval: String, limit: String, minCount: String):Future[SearchResponse] = {
      val aggInterval = getDateHistogramInterval(interval)
      val recordLimit = getLimit(limit)
      val minDocumentCount = minCount.toInt
      db.execute(
        search(_index / _type) fetchSource(false) query {
          rangeQuery("_borrow.datetime").from(start_time).to(end_time)
        } aggregations (
          Seq(
            dateHistogramAggregation("flow_data_category_borrow").field("_borrow.datetime").interval(aggInterval).subAggregation(
              termsAggregation("byCategory").field("book.category.keyword").size(recordLimit).minDocCount(minDocumentCount)
            ),
            dateHistogramAggregation("flow_data_category_return").field("_return.datetime").interval(aggInterval).subAggregation(
              termsAggregation("byCategory").field("book.category.keyword").size(recordLimit).minDocCount(minDocumentCount)
            ),
            dateHistogramAggregation("flow_data_category_renew").field("_renew.datetime").interval(aggInterval).subAggregation(
              termsAggregation("byCategory").field("book.category.keyword").size(recordLimit).minDocCount(minDocumentCount)
            )
          )
        )
      ).toTwitterFuture.map(a =>
        searchNestedAggResponse(
          a,
          Seq("flow_data_category_borrow", "flow_data_category_return", "flow_data_category_renew"),
          "daily_flow_data", "date", Seq("borrows_per_day", "returns_per_day", "renews_per_day"), "category", "count")
      )
    }

    def getFlowData(start_time: String, end_time: String, interval: String, limit: String, minCount: String):Future[SearchResponse] = {
      val aggInterval = getDateHistogramInterval(interval)
      val recordLimit = getLimit(limit)
      val minDocumentCount = minCount.toInt

      db.execute(
        search(_index / _type) fetchSource(false) query {
          rangeQuery("_borrow.datetime").from(start_time).to(end_time)
        } aggregations (
          Seq(
            dateHistogramAggregation("flow_data_borrow").field("_borrow.datetime").interval(aggInterval).subAggregation(
//              nestedAggregation("by_book_borrow", "book").subAggregation(
              termsAggregation("by_book_title_borrow").field("book.title.keyword").size(recordLimit).minDocCount(minDocumentCount)
//              termsAggregation("by_book_title_borrow").script("doc['book.barcode.keyword'].value + ',' + doc['book.title.keyword'].value").size(recordLimit).minDocCount(minDocumentCount)
//              )
            ),
            dateHistogramAggregation("flow_data_return").field("_return.datetime").interval(aggInterval).subAggregation(
              termsAggregation("by_book_title_return").field("book.title.keyword").size(recordLimit).minDocCount(minDocumentCount)
            ),
            dateHistogramAggregation("flow_data_renew").field("_renew.datetime").interval(aggInterval).subAggregation(
              termsAggregation("by_book_title_renew").field("book.title.keyword").size(recordLimit).minDocCount(minDocumentCount)
            )
          )
        )
      ).toTwitterFuture.map(a =>
        searchNestedAggResponse(
          a,
          Seq("flow_data_borrow", "flow_data_return", "flow_data_renew"),
          "daily_flow_data", "date", Seq("borrows_per_day", "returns_per_day", "renews_per_day"), "book_title", "count")
      )
    }

    def getReaderBorrowRanking(start_time: String, end_time: String, asc: Boolean = false, limit_param: Int = 10):Future[SearchResponse] = {
      db.execute(
        search(_index / _type) query {
          rangeQuery("_borrow.datetime").from(start_time).to(end_time)
        } aggregations (
          Seq(
            termsAggregation("readerRanking").field("reader.full_name.keyword").order(Terms.Order.count(asc))
          )
        ) fetchSource false start 0 limit limit_param
      ).toTwitterFuture.map(a =>
        searchTermsAggResponse(a, "readerRanking", "borrow_ranking", "full_name", "borrow_times")
      )
    }

    def getBookBorrowRanking(start_time: String, end_time: String, asc: Boolean = false, limit_param: Int = 10):Future[SearchResponse] = {
      db.execute(
        search(_index / _type) query {
          rangeQuery("_borrow.datetime").from(start_time).to(end_time)
        } aggregations (
          Seq(
            termsAggregation("bookRanking").field("book.title.keyword").order(Terms.Order.count(asc))
          )
        ) fetchSource false start 0 limit limit_param
      ).toTwitterFuture.map(a =>
        searchTermsAggResponse(a, "bookRanking", "borrowed_ranking", "booktitle", "borrowed_times")
      )
    }

    // added by kuangyuan 5/12/2017
    def updateLost(_id: String, lost: Boolean = true, now: String = Time.now.toString): Future[UpdateResponse]  = {
      this.updateDoc(_id, Map("lost" -> lost, "datetime" -> now))
    }

    def updateFined(_id: String, fined: Boolean = true, now: String = Time.now.toString): Future[UpdateResponse] = {
      this.updateDoc(_id, Map("fined" -> fined, "datetime" -> now))
    }

    def getLeastBorrowRankingFromBarcodes(start_time: String, end_time: String): Future[SearchResponse] = {
      db.execute(
        search(_index / _type) aggregations (
          Seq(
            termsAggregation("leastBorrowRankingBook").field("book.barcode.keyword").order(Terms.Order.count(false))
          )
        ) fetchSource false start 0 limit elasticSearchLimit
      ).toTwitterFuture.map(a =>
        searchTermsAggResponse(a, "leastBorrowRanking", "least_borrowed_ranking", "book_barcode", "borrowed_times")
      )
    }

  }
}