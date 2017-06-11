package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.vendor.VendorMember
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.twitter.util.Future

/**
  * Created by jiejin on 6/12/16.
  */
@Singleton
class VendorMemberRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "vendor"
  val _type = "info"

  val dal = new BaseDalImpl[VendorMember](db)(_index, _type) {
    override def id(doc: VendorMember): Future[String] = Future.value(Security.digest(doc.toString))

  }
}
