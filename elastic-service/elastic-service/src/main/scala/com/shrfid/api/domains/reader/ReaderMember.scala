package com.shrfid.api.domains.reader

import com.shrfid.api.domains.readable.ReaderInfo
import com.shrfid.api.http.Elastic.reader.member.{PostReaderMemberRequest, ReaderMemberInsertion}
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.shrfid.api.{Config, Time}
import com.twitter.util.Future
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * Created by jiejin on 12/10/16.
  */
object ReaderMember {
  implicit val readerMemberFmt = Json.format[ReaderMember]

  def toDomain(request: PostReaderMemberRequest, deposit: Double): Future[ReaderMember] = {
    Future.value(ReaderMember(request.barcode, request.rfid, Config.defaultPassword, credit = deposit, request.levelId, request.groupIds.filterNot(_==""),
      request.identity, request.fullName, request.gender, request.dob, request.email, request.mobile,
      request.address, request.postcode, request.profileImage, request.createAt.toString, true, "0000-01-01")
    )
  }

  def toDomain(request: ReaderMemberInsertion, deposit: Double): Future[ReaderMember] = {
    Future.value(ReaderMember(request.barcode, request.rfid, Config.defaultPassword, credit = deposit, request.levelId, request.groupIds.filterNot(_==""),
      request.identity, request.fullName, request.gender, request.dob, request.email, request.mobile,
      request.address, request.postcode, request.profileImage, request.createAt.toString, true, "0000-01-01")
    )
  }
}

object ReaderMemberIsSuspend {
  implicit val readerMemberIsSuspendFmt = Json.format[ReaderMemberIsSuspend]
  def toDomain(id: String, r: ReaderMember, includeImage: Boolean) = {
    ReaderMemberIsSuspend(id, r.barcode, r.rfid, r.credit, r.level, r.groups, r.identity, r.full_name, r.gender, r.dob, r.email,
      r.mobile, r.address, r.postcode, if (includeImage == true) r.profile_image else "", r.create_at, r.is_active, if (Seq("0000-01-01", "").contains(r.restore_at)) false else true,
      if (Seq("0000-01-01", "").contains(r.restore_at) && DateTime.parse(r.restore_at).isBeforeNow) "" else r.restore_at, r.datetime)
  }
}

case class ReaderMemberIsSuspend(id: String,
                                 barcode: String,
                                 rfid: String,
                                 credit: Double,
                                 level: String,
                                 groups: Seq[String],
                                 identity: String,
                                 full_name: String,
                                 gender: String,
                                 dob: String,
                                 email: String,
                                 mobile: String,
                                 address: String,
                                 postcode: String,
                                 profile_image: String,
                                 create_at: String,
                                 is_active: Boolean,
                                 is_suspend: Boolean,
                                 restore_at: String,
                                 datetime: String )

case class ReaderMember(barcode: String,
                        rfid: String,
                        password: String,
                        credit: Double,
                        level: String,
                        groups: Seq[String],
                        identity: String,
                        full_name: String,
                        gender: String,
                        dob: String,
                        email: String,
                        mobile: String,
                        address: String,
                        postcode: String,
                        profile_image: String,
                        create_at: String,
                        is_active: Boolean,
                        restore_at: String,
                        datetime: String = Time.now.toString) extends BaseDoc {

  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

  def toReaderInfo(id: String): ReaderInfo = {
    ReaderInfo(id, barcode, rfid, level, groups, identity, full_name, gender, dob, create_at)
  }

}

