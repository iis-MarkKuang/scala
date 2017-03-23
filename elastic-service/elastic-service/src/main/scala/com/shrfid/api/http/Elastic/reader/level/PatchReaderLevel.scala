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
