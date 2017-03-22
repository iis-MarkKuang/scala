package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.book.BookStack
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.twitter.util.Future

/**
  * Created by jiejin on 25/11/16.
  */


@Singleton
class BookStackRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "stack"
  val _type = "info"

  val dal = new BaseDalImpl[BookStack](db)(_index, _type) {
    override def id(doc: BookStack): Future[String] = Future.value(Security.digest(doc.toString))

    //override def findAllDocs(_filter: Seq[Product with Serializable], _order: Option[String], _limit: Int, _offset: Int): Future[(StatusCode, (Count, Docs))] = ???
  }
}