package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.vendor.VendorOrder
import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource
import com.twitter.util.Future

/**
  * Created by jiejin on 6/12/16.
  */
@Singleton
class VendorOrderRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "vendororder"
  val _type = "info"

  val dal = new BaseDalImpl[VendorOrder](db)(_index, _type) {
    override def id(_doc: VendorOrder): Future[String] = Future.value(Security.digest(_doc.jsonStringify))

    /*override def upsertDoc(_id: String, _doc: String): Future[UpsertResponse] = {

      db.execute(
        update(_id).in(_index / _type).docAsUpsert(_doc)
      ).toTwitterFuture.map(a => (a.status.getStatus, _doc.replaceFirst("\\{", s"""{ "id":"${a.id}", """)))
    }*/
  }
}
