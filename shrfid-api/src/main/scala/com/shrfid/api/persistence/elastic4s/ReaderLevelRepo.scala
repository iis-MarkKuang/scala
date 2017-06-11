package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.reader.ReaderLevel
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future

/**
  * Created by jiejin on 7/12/16.
  */
@Singleton
class ReaderLevelRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "readerlevel"
  val _type = "info"

  val dal = new BaseDalImpl[ReaderLevel](db)(_index, _type) {
    override def id(doc: ReaderLevel): Future[String] = Future.value(Security.digest(doc.toString))

    def findDepositById(_id: String): Future[Option[Double]] = {
      db.execute(get(_id) from (_index / _type)).toTwitterFuture.map(a => a.exists match {
        case true => a.sourceAsMap.get("deposit") match {
          case Some(d) => Some(d.toString.toDouble)
          case None => None
        }
        case false => None
      })
    }

    def findNameById(_id: String): Future[Option[String]] = {
      db.execute(get(_id) from (_index / _type)).toTwitterFuture.map(a => a.exists match {
        case true => a.sourceAsMap.get("name") match {
          case Some(n) => Some(n.toString)
          case None => None
        }
        case false => None
      })
    }
  }
}
