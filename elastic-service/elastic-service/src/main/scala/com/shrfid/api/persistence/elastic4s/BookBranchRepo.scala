package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.readable.BookBranch
import com.shrfid.api.modules.Elastic4SModule._
import com.twitter.util.Future

/**
  * Created by jiejin on 24/11/16.
  */

@Singleton
class BookBranchRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "branch"
  val _type = "info"

  val dal = new BaseDalImpl[BookBranch](db)(_index, _type) {
    override def id(doc: BookBranch): Future[String] = Future.value(Security.digest(doc.toString))

  }
}
