package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book.Book
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future
import org.elasticsearch.search.sort.SortOrder

import scala.collection.JavaConversions._


/**
  * Created by jiejin on 2/12/16.
  */
@Singleton
class BookItemRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "book"
  val _type = "info"

  val dal = new BaseDalImpl[Book](db)(_index, _type) {
    override def id(doc: Book): Future[String] = Future.value(doc.barcode.toString)

    override def upsertDoc(_id: String, _doc: Book): Future[UpsertResponse] = {
      db.execute(
        update(_doc.barcode.toString).in(_index / _type).docAsUpsert(_doc.jsonStringify)
      ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
    }

    def findNextBookIndex(clc: String) = {
      db.execute(
      search(_index / _type) termQuery("clc.keyword", clc) fetchSource (false) aggregations (
      maxAggregation("book_index").field("book_index")
      )
      ).toTwitterFuture.map(a => a.aggregations.maxResult("book_index").value() match {
        case a if a.isInfinity => 1
        case b => b.toInt
      })
    }

    def isAvailable(_id: String): Boolean = {
      db.execute(indexExists("book")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) termQuery("_id", _id) termQuery("is_available", "true") limit 1
        ).toTwitterFuture.map(a =>
            a.hits.headOption match {
              case None => false
              case Some(s) => println(s.sourceAsMap("_id")); println(s.sourceAsMap("is_available"))
                true
              }
        )
        }
      }
    }

    def findMaxBarcode(category: String): Future[String] = {
      db.execute(indexExists("book")).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
            search(_index / _type) termQuery("category.keyword", category) sortBy (Iterable(fieldSort("barcode") order SortOrder.DESC)) limit 1
        ).toTwitterFuture.map(a =>
            a.hits.headOption match {
                case None => barcodeFmt(0, bookCategoryId(category))
                case Some(s) => println(s.sourceAsMap("barcode")); s.sourceAsMap("barcode").toString
              }
        )
          case false => Future.value(barcodeFmt(0, bookCategoryId(category)))
        }
      }
    }

    def catalogue(_id: String, rfid: String, reference: String, title: String, stackId: String,
    clc: String, bookIndex: Int, user: Username): Future[UpsertResponse] = {
      db.execute(
      update(_id).in(_index / _type).docAsUpsert(
      "rfid" -> rfid,
      "reference" -> reference,
      "title" -> title,
      "stack" -> stackId,
      "clc" -> clc,
      "book_index" -> bookIndex,
      "datetime" -> Time.now.toString,
      "user" -> user)
      ).toTwitterFuture.map(a => (a.status.getStatus, updateJsonRepsonse(a)))
    }

    def updateAvailability(_id: String, isAvailable: Boolean = false, now: String = Time.now.toString) = {
      this.updateDoc(_id, Map("is_available" -> isAvailable, "datetime" -> now))
    }

    def insert( _id: String, item: String) = {
      db.execute(
      update(_id) in (_index / _type) script {
          script(s"""if (ctx._source.containsKey(params.field)) {params.value.removeAll(ctx._source.items);ctx._source.items.addAll(params.value)} else { ctx._source.items=params.value;}""")
            .lang("painless").params(Map("field" -> "items", "value" -> seqAsJavaList(Seq(item))))
        }
      )
    }

    def delete(_id: String, item: String) = {
      db.execute({
        update(_id) in (_index / _type) script {
          script(s""" ctx._source.items.removeIf(item -> item == \"$item\") """).lang("painless")
        }
      })
    }

  }
}
