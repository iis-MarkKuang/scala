package com.shrfid.api.domains.reader

import com.shrfid.api._
import com.shrfid.api.http.Elastic.reader.level.PostReaderLevelNewRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by kuangyuan on 4/17/2017.
  */

object BorrowRuleBook {
  implicit val borrowRuleBookFmt = Json.format[BorrowRuleBook]
}

case class BorrowRuleBook(quantity: Int,
                          day: Int,
                          can_renew: Boolean = true,
                          can_book: Boolean = true) {

  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object BorrowRulePeriodical {
  implicit val borrowRulePeriodicalFmt = Json.format[BorrowRulePeriodical]
}

case class BorrowRulePeriodical(quantity: Int,
                                day: Int,
                                can_renew: Boolean = true,
                                can_book: Boolean = true) {

  def patchRequest = {
    getCCParams(this)
  }
}

object BorrowRuleNew {
  implicit val borrowRuleNewFmt = Json.format[BorrowRuleNew]
}

case class BorrowRuleNew(borrow_rule_book: BorrowRuleBook,
                         borrow_rule_periodical: BorrowRulePeriodical) {

  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


object PenaltyRuleBook {
  implicit val penaltyRuleBookFmt = Json.format[PenaltyRuleBook]
}

case class PenaltyRuleBook(method: String,
                       expire_factor: Double,
                       lost_factor: Double) {
  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object PenaltyRulePeriodical {
  implicit val penaltyRulePeriodicalFmt = Json.format[PenaltyRulePeriodical]
}

case class PenaltyRulePeriodical(method: String,
                                 expire_factor: Double,
                                 lost_factor: Double) {
  def patchRequest = {
    getCCParams(this)
  }
}

object PenaltyRuleNew {
  implicit val penaltyRuleNewFmt = Json.format[PenaltyRuleNew]
}

case class PenaltyRuleNew(penalty_rule_book: PenaltyRuleBook,
                          penalty_rule_periodical: PenaltyRulePeriodical
                         ) {
  def patchRequest = {
    getCCParams(this)
  }

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


object ReaderLevelNew {
  implicit val readerLevelNewFmt = Json.format[ReaderLevelNew]

  def toDomain(r: PostReaderLevelNewRequest): Future[ReaderLevelNew] = {
    Future.value(ReaderLevelNew(r.name,
      r.deposit,
      r.borrowRuleNew,
      r.penaltyRuleNew,
      r.description
    ))
  }
}

case class ReaderLevelNew(name: String,
                       deposit: Double,
                       borrow_rule_new: BorrowRuleNew,
                       penalty_rule_new: PenaltyRuleNew,
                       description: String,
                       datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


