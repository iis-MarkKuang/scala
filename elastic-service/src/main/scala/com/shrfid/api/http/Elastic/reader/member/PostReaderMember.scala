package com.shrfid.api.http.Elastic.reader.member

import com.shrfid.api.Time
import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.{MethodValidation, NotEmpty, ValidationResult}
import org.joda.time.DateTime


/**
  * Created by jiejin on 9/10/16.
  */
case class PostReaderMemberRequest(@Header Authorization: String,
                                   @NotEmpty barcode: String,
                                   rfid: String = "",
                                   levelId: String,
                                   groupIds: Seq[String],
                                   @NotEmpty identity: String,
                                   fullName: String,
                                   gender: String,
                                   dob: String,
                                   email: String = "",
                                   mobile: String = "",
                                   address: String = "",
                                   postcode: String = "",
                                   profileImage: String = "",
                                   createAt: DateTime= Time.now) {
  @MethodValidation
  def validateGender = {
    ValidationResult.validate(
      gender.equals("男") || gender.equals("女"),
      "gender must be '男' or '女'")
  }
}

case class ReaderMemberInsertion(barcode: String,
                                 rfid: String = "",
                                 levelId: String,
                                 groupIds: Seq[String],
                                 identity: String,
                                 fullName: String,
                                 gender: String,
                                 dob: String,
                                 email: String = "",
                                 mobile: String = "",
                                 address: String = "",
                                 postcode: String = "",
                                 profileImage: String = "",
                                 createAt: DateTime=Time.now) {
  @MethodValidation
  def validateGender = {
    ValidationResult.validate(
      gender.equals("男") || gender.equals("女"),
      "gender must be '男' or '女'")
  }
}

case class PostReaderMemberBulkRequest(@Header Authorization: String,
                                       data: Seq[ReaderMemberInsertion]) {
  @MethodValidation
  def validateNonEmpty = {
    ValidationResult.validate(
      data.nonEmpty,
      "data cannot be empty")
  }
}