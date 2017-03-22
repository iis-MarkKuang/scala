package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.Security
import com.shrfid.api.domains.reader.ReaderGroup
import com.shrfid.api.modules.Elastic4SModule._
import com.twitter.util.Future

/**
  * Created by jiejin on 7/12/16.
  */
@Singleton
class ReaderGroupRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "readergroup"
  val _type = "info"

  val dal = new BaseDalImpl[ReaderGroup](db)(_index, _type) {
    override def id(doc: ReaderGroup): Future[String] = Future.value(Security.digest(doc.toString))

  }
}