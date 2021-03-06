package com.shrfid.api.http.Elastic.reader.member

import com.shrfid.api._
import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 9/10/16.
  */
case class PatchReaderMemberBulkRequest(@Header Authorization: String,
                                        @QueryParam action: String,
                                        ids: Option[Seq[String]],
                                        days: Option[Int],
                                        groupIds: Option[Seq[String]]) {
  @MethodValidation
  def validateAction = {
    ValidationResult.validate(
      Seq("inactivate", "suspend", "inactivate_by_group_id").contains(action) && (
        action == "inactivate" && ids.isDefined ||
          action == "suspend" && days.isDefined && ids.isDefined ||
          action == "inactivate_by_group_id" && groupIds.isDefined
        ),
      "action must be 'inactivate', 'inactivate_by_group_id' or 'suspend'")
  }

}

case class UpdateReaderMemberCreditByIdRequest(@Header Authorization: String,
                                               @RouteParam id: String,
                                               @QueryParam amount: String) {
  @MethodValidation
  def validateAction = {

    ValidationResult.validate(
      amount.toDouble > 0,
      "deduct amount must be positive, we wouldn't want to give reader money lol"
    )
  }
}

case class PatchReaderMemberByIdRequest(@Header Authorization: String,
                                        @RouteParam id: String,
                                        @QueryParam action: String,
                                        days: Option[Int],
                                        barcode: Option[String],
                                        rfid: Option[String],
                                        fullName: Option[String],
                                        gender: Option[String],
                                        email: Option[String],
                                        mobile: Option[String],

                                        // Added by kuangyuan 5/4/2017
                                        identity: Option[String],

                                        address: Option[String],
                                        postcode: Option[String],
                                        dob: Option[String],
                                        levelId: Option[String],
                                        levelNew: Option[String],
                                        groupIds: Option[Seq[String]],
                                        profileImage: Option[String],
                                        isActive: Option[Boolean],
                                        datetime: String = Time.now.toString)
  extends BasePatchByIdRequest(Authorization, id, datetime) {


  @MethodValidation
  def validateAction = {
    ValidationResult.validate(
      Seq("inactivate", "suspend", "card", "info").contains(action) && (action == "inactivate"
        || "suspend" == action && days.isDefined || "card" == action && barcode.isDefined && rfid.isDefined || action == "info"),
      s"""'action' must be 'inactivate', 'suspend', 'card', 'info'
          | 1. when 'action' == inactivate, 'is_active' is required
          | 2. when 'action' == suspend, 'days' is required
          | 3. when 'action' == card, 'barcode' and 'rfid' is required
       """.stripMargin)
  }

  override def patchRequest = {
    //val a = getCCParams(this, Seq("id", "Authorization", "action"))
    action match {
      case "inactivate" => Map("is_active" -> isActive.getOrElse(false), "datetime" -> Time.now.toString)
      case "suspend" => Map("restore_at" -> Time.nextNdays(days.get).toString, "datetime" -> Time.now.toString)
      case "card" => Map("barcode" -> barcode.getOrElse(""), "rfid" -> rfid.getOrElse(""), "datetime" -> Time.now.toString)
      case "info" =>
        val a = getCCParams(this, Seq("id", "Authorization", "barcode", "rfid", "action", "days", "levelId", "groupIds", "fullName"))
        (levelId, groupIds) match {
          case (Some(l), Some(g)) => a +("level" -> l, "groups" -> g.distinct.filterNot(_==""))
          case (Some(l), None) => a + ("level" -> l)
          case (None, Some(g)) => a + ("groups" -> g.distinct.filterNot(_==""))
          case (None, None) => a
        }
    }
  }
}