package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.Security
import com.shrfid.api.domains.readable.{BorrowHistory, TimeLocation}
import com.shrfid.api.modules.Elastic4SModule._
import com.twitter.util.Future

/**
  * Created by jiejin on 11/12/16.
  */

@Singleton
class BorrowHistoryRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "borrow"
  val _type = "info"

  val dal = new BaseDalImpl[BorrowHistory](db)(_index, _type) {
    override def id(doc: BorrowHistory): Future[String] = Future.value(Security.digest(doc.toString))

    def returnAt(_id: String, _return: TimeLocation, now: String) = {
      this.updateDoc(_id, Map("_return" -> Map("location" -> _return.location, "datetime" -> _return.datetime), "datetime" -> now, "status"-> "return"))
    }
    def renewAt(_id: String, _renew: TimeLocation, dueAt: String, now: String) = {
      this.updateDoc(_id, Map("_renew" -> Map("location" -> _renew.location, "datetime" -> _renew.datetime), "due_at" -> dueAt, "datetime" -> now, "status"-> "renew"))
    }
    def reserveAt(_id: String, _reserve: TimeLocation, now: String) = {
      this.updateDoc(_id, Map("_reserve" -> Map("location" -> _reserve.location, "datetime" -> _reserve.datetime), "datetime" -> now, "status"->"reserve"))
    }
    // called when reserved readables turn available
    def reserveNotify(_id: String, now: String, borrowWindow: String) = {
      this.updateDoc(_id, Map("datetime" -> now, "borrowWindow" -> borrowWindow))
    }
  }
}