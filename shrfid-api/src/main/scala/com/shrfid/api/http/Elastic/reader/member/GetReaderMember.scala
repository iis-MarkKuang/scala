package com.shrfid.api.http.Elastic.reader.member

import com.shrfid.api.Time
import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.{BaseGetByIdRequest, BaseGetListRequest}
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 8/12/16.
  */
case class GetReaderMemberByIdRequest(@Header Authorization: String,
                                      @RouteParam id: String) extends BaseGetByIdRequest(Authorization, id) {
  override val sourceExclude = Seq("password")
}

// added by kuangyuan 5/4/2017
case class GetReaderMemberByBarcodeRequest(@Header Authorization: String,
                                           @QueryParam barcode: String)

case class GetReaderMemberDelayedFineRequest(@Header Authorization: String,
                                             @RouteParam id: String)

case class GetReaderMemberLostFineRequest(@Header Authorization: String,
                                          @RouteParam id: String,
                                          @QueryParam bookBarcode: String)

// Added by kuangyuan 5/9/2017
case class GetReaderMemberDelayedDetailRequest(@Header Authorization: String,
                                               @RouteParam id: String)

case class GetReaderMemberListRequest(@Header Authorization: String,
                                      @QueryParam limit: Int = 100,
                                      @QueryParam offset: Int = 0,
                                      @QueryParam id: Option[String],
                                      @QueryParam barcode: Option[String],
                                      @QueryParam rfid: Option[String],
                                      @QueryParam identity: Option[String],
                                      @QueryParam fullName: Option[String],
                                      @QueryParam gender: Option[String],
                                      @QueryParam email: Option[String],
                                      @QueryParam mobile: Option[String],
                                      @QueryParam address: Option[String],
                                      @QueryParam postcode: Option[String],
                                      @QueryParam dob: Option[String],
                                      @QueryParam levelId: Option[String],
                                      @QueryParam groupId: Option[String],
                                      @QueryParam createAt: Option[String],
                                      @QueryParam isActive: Option[Boolean],
                                      @QueryParam isSuspend: Option[Boolean],
                                      @QueryParam ordering: Option[String],
                                      @QueryParam isOwing: Option[Boolean])
extends BaseGetListRequest(Authorization, limit, offset, ordering) {

  @MethodValidation
  def validateGender = {
    ValidationResult.validate(
    gender.getOrElse("").equals("男") || gender.getOrElse("").equals("女") || gender.isEmpty,
    "gender must be '男' or '女'")
  }

  val idQueryParam = UrlQueryParam("_id", id)
  val barcodeQueryParam = UrlQueryParam("barcode", barcode)
  val rfidQueryParam = UrlQueryParam("rfid", rfid)
  val identityQueryParam = UrlQueryParam("identity", identity)
  val fullNameQueryParam = UrlQueryParam("full_name", fullName)
  val genderQueryParam = UrlQueryParam("gender", gender)
  val emailQueryParam = UrlQueryParam("email", email)
  val mobileQueryParam = UrlQueryParam("mobile", mobile)
  val addressQueryParam = UrlQueryParam("address", address)
  val postcodeQueryParam = UrlQueryParam("postcode", postcode)
  val dobQueryParam = UrlQueryParam("dob", dob)
  val levelQueryParam = UrlQueryParam("level", levelId, "level_id")
  val groupQueryParam = UrlQueryParam("groups", groupId, "group_id")
  val createAtQueryParam = UrlQueryParam("create_at", createAt)
  val isActiveQueryParam = UrlQueryParam("is_active", isActive)
  //todo
  val isSuspendQueryParam = UrlQueryParam("is_suspend", isSuspend)
  val isOwingQueryParam = UrlQueryParam("is_owing", isSuspend)

  override val filter = QueryFilter(must = Seq(
    idQueryParam.matchPhraseQ,
    barcodeQueryParam.matchPhraseQ,
    rfidQueryParam.matchPhraseQ,
    identityQueryParam.matchPhraseQ,
    fullNameQueryParam.matchPhraseQ,
    genderQueryParam.matchPhraseQ,
    emailQueryParam.matchPhraseQ,
    mobileQueryParam.matchPhraseQ,
    addressQueryParam.matchPhraseQ,
    postcodeQueryParam.matchPhraseQ,
    dobQueryParam.rangeQ,
    levelQueryParam.matchPhraseQ,
    groupQueryParam.matchQ,
    createAtQueryParam.rangeQ,
    isActiveQueryParam.matchBool,
    isSuspend match {
      case Some(bool) => bool match {
        case false => UrlQueryParam("restore_at", Some(s",${Time.endOfDay}")).rangeQ
        case _ => None
      }
      case None => None
    },
    isOwing match {
      case Some(bool) => bool match {
        case false => UrlQueryParam("credit", Some(s"0,")).rangeQ
        case _ => None
      }
      case None => None
    }

  ),
    not = Seq(
      isSuspend match {
        case Some(bool) => bool match {
          case true => UrlQueryParam("restore_at", Some(s",${Time.endOfDay}")).rangeQ
          case _ => None
        }
        case None => None
      },
      isOwing match {
        case Some(bool) => bool match {
          case true => UrlQueryParam("credit", Some(s"0,")).rangeQ
          case _ => None
        }
        case None => None
      }

    )
  )

  override val path: String = Router.ReaderMember.list
  override val requestPath: String = _requestPath(Seq(idQueryParam, barcodeQueryParam, rfidQueryParam,
    identityQueryParam, fullNameQueryParam, genderQueryParam, emailQueryParam, mobileQueryParam,
    addressQueryParam, postcodeQueryParam, dobQueryParam, levelQueryParam, groupQueryParam, createAtQueryParam,
    isActiveQueryParam, isSuspendQueryParam, isOwingQueryParam))

}