package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.readable.BookReference
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future

/**
  * Created by jiejin on 5/12/16.
  */
@Singleton
class BookReferenceRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "reference"
  val _type = "info"

  val dal = new BaseDalImpl[BookReference](db)(_index, _type) {
    override def id(doc: BookReference): Future[String] = {
      Future.value(Security.digest(doc.copy(datetime = "").jsonStringify))
    }

    def findTitleById(_id: String): Future[(Option[String], Option[String])] = {
      db.execute(
        get(_id) from _index / _type
      ).toTwitterFuture.flatMap { case a =>

        val title = a.sourceAsMap.get("题名与责任者") match {
          case Some(t) => Some(t.asInstanceOf[java.util.HashMap[String, String]].get("正题名").toString)
          case None => None
        }
        val clc = a.sourceAsMap.get("中国图书馆图书分类法分类号") match {
          case Some(t) => Some(t.toString)
          case None => None
        }
        Future.value((title, clc))
      }


    }
  }
}
