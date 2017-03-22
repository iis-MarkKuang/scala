package com.shrfid.api.domains.reader

import com.shrfid.api._
import com.shrfid.api.http.Elastic.reader.level.PostReaderLevelRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 13/09/2016.
  */
object BorrowRule {
  implicit val borrowRuleFmt = Json.format[BorrowRule]

}

case class BorrowRule(quantity: Int,
                      day: Int,
                      can_renew: Boolean = true,
                      can_book: Boolean = true) {

  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object PenaltyRule {
  implicit val penaltyRuleFmt = Json.format[PenaltyRule]
}

case class PenaltyRule(method: String, expire_factor: Double, lost_factor: Double) {
  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


object ReaderLevel {
  implicit val readerLevelFmt = Json.format[ReaderLevel]

  def toDomain(r: PostReaderLevelRequest): Future[ReaderLevel] = {
    Future.value(ReaderLevel(r.name,
      r.deposit,
      r.borrowRule,
      r.penaltyRule,
      r.description
    ))
  }
}

case class ReaderLevel(name: String,
                       deposit: Double,
                       borrow_rule: BorrowRule,
                       penalty_rule: PenaltyRule,
                       description: String,
                       datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


