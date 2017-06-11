package com.shrfid.api.http.Elastic.reader.level

import com.shrfid.api._
import com.twitter.finatra.request.{Header, RouteParam}
import play.api.libs.json.Json

/**
  * Created by jiejin on 28/9/16.
  */

case class PenaltyRule(method: Option[String],
                       expire_factor: Option[Double],
                       lost_factor: Option[Double]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object PenaltyRule {
  implicit val penaltyRuleFmt = Json.format[PenaltyRule]
}


case class BorrowRule(quantity: Option[Int],
                      day: Option[Int],
                      can_renew: Option[Boolean],
                      can_book: Option[Boolean]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object BorrowRule {
  implicit val borrowRuleFmt = Json.format[BorrowRule]

}

case class PatchReaderLevelByIdRequest(@Header Authorization: String,
                                       @RouteParam id: String,
                                       name: Option[String],
                                       deposit: Option[Double],
                                       borrowRule: Option[BorrowRule],
                                       penaltyRule: Option[PenaltyRule],
                                       description: Option[String],
                                       datetime: String = Time.now.toString) {
  def patchRequest = {
    val a = getCCParams(this, Seq("id", "Authorization", "borrowRule", "penaltyRule"))
    (borrowRule, penaltyRule) match {
      case (Some(br), Some(pr)) => a +("borrow_rule" -> br.patchRequest, "penalty_rule" -> pr.patchRequest)
      case (Some(br), None) => a + ("borrow_rule" -> br.patchRequest)
      case (None, Some(pr)) => a + ("penalty_rule" -> pr.patchRequest)
      case (None, None) => a
    }

  }
}

case class PatchReaderLevelNewByIdRequest(@Header Authorization: String,
                                          @RouteParam id: String,
                                          name: Option[String],
                                          deposit: Option[Double],
                                          borrowRuleNew: Option[BorrowRuleNew],
                                          penaltyRuleNew: Option[PenaltyRuleNew],
                                          description: Option[String],
                                          datetime: String = Time.now.toString) {
  def patchRequest = {
    val a = getCCParams(this, Seq("id", "Authorization", "borrowRuleNew", "penaltyRuleNew"))
    (borrowRuleNew, penaltyRuleNew) match {
      case (Some(br), Some(pr)) => a +("borrow_rule_new" -> br.patchRequest, "penalty_rule_new" -> pr.patchRequest)
      case (Some(br), None) => a + ("borrow_rule_new" -> br.patchRequest)
      case (None, Some(pr)) => a + ("penalty_rule_new" -> pr.patchRequest)
      case (None, None) => a
    }
  }
}

case class BorrowRuleNew(borrowRuleBook: BorrowRuleBook,
                         borrowRulePeriodical: BorrowRulePeriodical) {
  def patchRequest = {
    getCCParams(this)
  }
}

//object BorrowRuleNew {
//  implicit val borrowRuleNewFmt = Json.format[BorrowRuleNew]
//}

case class BorrowRuleBook(quantity: Option[Int],
                      day: Option[Int],
                      can_renew: Option[Boolean],
                      can_book: Option[Boolean]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object BorrowRuleBook {
  implicit val borrowRuleBookFmt = Json.format[BorrowRuleBook]

}

case class BorrowRulePeriodical(quantity: Option[Int],
                          day: Option[Int],
                          can_renew: Option[Boolean],
                          can_book: Option[Boolean]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object BorrowRulePeriodical {
  implicit val borrowRulePeriodicalFmt = Json.format[BorrowRulePeriodical]

}

case class PenaltyRuleNew(penaltyRuleBook: PenaltyRuleBook,
                         penaltyRulePeriodical: PenaltyRulePeriodical) {
  def patchRequest = {
    getCCParams(this)
  }
}

//object PenaltyRuleNew {
//  implicit val penaltyRuleNewFmt = Json.format[PenaltyRuleNew]
//}

case class PenaltyRuleBook(method: Option[String],
                          expire_factor: Option[Double],
                          lost_factor: Option[Double]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object PenaltyRuleBook {
  implicit val penaltyRuleBookFmt = Json.format[PenaltyRuleBook]
}

case class PenaltyRulePeriodical(method: Option[String],
                                 expire_factor: Option[Double],
                                 lost_factor: Option[Double]) {
  def patchRequest = {
    getCCParams(this)
  }
}

object PenaltyRulePeriodical {
  implicit val penaltyRulePeriodicalFmt = Json.format[PenaltyRulePeriodical]
}