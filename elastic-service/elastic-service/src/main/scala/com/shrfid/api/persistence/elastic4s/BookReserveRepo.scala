package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.readable.BookReserve
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future
import org.elasticsearch.search.sort.SortOrder

import scala.collection.JavaConverters._

/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class BookReserveRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "reserve"
  val _type = "info"

  val dal = new BaseDalImpl[BookReserve](db)(_index, _type) {
    override def id(doc: BookReserve): Future[String] = Future.value(doc.barcode.toString)

    override def upsertDoc(_id: String, _doc: BookReserve): Future[UpsertResponse] = {
      db.execute(
        update(_doc.barcode.toString).in(_index / _type).docAsUpsert(_doc.jsonStringify)
      ).toTwitterFuture.map(a => (a.status.getStatus, upsertJsonRepsonse(a)))
    }
  }
}
