package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.reader.ReaderLevelNew
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future

/**
  * Created by jiejin on 7/12/16.
  */
@Singleton
class ReaderLevelNewRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "readerlevelnew"
  val _type = "info"

  val dal = new BaseDalImpl[ReaderLevelNew](db)(_index, _type) {
    override def id(doc: ReaderLevelNew): Future[String] = Future.value(Security.digest(doc.toString))

    def findDepositById(_id: String): Future[Option[Double]] = {
      db.execute(get(_id) from (_index / _type)).toTwitterFuture.map(a => a.exists match {
        case true => a.sourceAsMap.get("deposit") match {
          case Some(d) => Some(d.toString.toDouble)
          case None => None
        }
        case false => None
      })
    }

    def findIdByName(name: String): Future[(StatusCode, Docs)] = {
      db.execute(indexExists(_index)).toTwitterFuture.flatMap {
        case a => a.isExists match {
          case true =>
            db.execute(
              search(_index / _type) termQuery("name.keyword", name) limit 1
            ).toTwitterFuture.map(a =>
              a.hits.headOption match {
                case None => (404, "new level not found")
                case Some(s) =>
                  val r = s.id
                  (200, s.id)
              }
            )
          case false => Future((502, "readerlevelnew index does not exist"))
        }
      }
    }
  }
}
