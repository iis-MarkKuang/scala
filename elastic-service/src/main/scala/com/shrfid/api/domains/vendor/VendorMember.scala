package com.shrfid.api.domains.vendor

import com.shrfid.api._
import com.shrfid.api.http.Elastic.vendor.member.PostVendorMemberRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 6/12/16.
  */
object VendorMember {
  implicit val vendorMemberFmt = Json.format[VendorMember]

  def toDomain(request: PostVendorMemberRequest): Future[VendorMember] = {
    Future.value(VendorMember(request.name, request.isActive, request.description))
  }
}

case class VendorMember(name: String, is_active: Boolean, description: String, datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


object VendorMemberWithId {
  implicit val vendorMemberFmt = Json.format[VendorMemberWithId]

  def toDomain(id: String, vendorMember: VendorMember): VendorMemberWithId = {
    VendorMemberWithId(id, vendorMember.name, vendorMember.is_active, vendorMember.description, vendorMember.datetime)
  }
}

case class VendorMemberWithId(id: String, name: String, is_active: Boolean, description: String, datetime: String) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}