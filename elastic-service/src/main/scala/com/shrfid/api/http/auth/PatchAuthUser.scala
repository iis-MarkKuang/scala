package com.shrfid.api.http.auth

import java.sql.Date

import com.twitter.finatra.request.{Header, RouteParam}
import com.twitter.finatra.validation._

/**
  * Created by jiejin on 13/10/16.
  */
case class PatchAuthUserRequest(@Header Authorization: String,
                                @RouteParam id: Int,
                                @Size(min = 0, max = 30) identity: String,
                                @Size(min = 0, max = 30) fullName: String,
                                gender: String,
                                dob: Option[Date],
                                @Size(min = 0, max = 254) email: String,
                                @Size(min = 0, max = 20) mobile: String,
                                @Size(min = 0, max = 254) address: String,
                                @Size(min = 0, max = 20) postcode: String,
                                @Size(min = 0, max = 254) profileUrl: String,
                                isStaff: Boolean = true,
                                isActive: Boolean = true) {
  @MethodValidation
  def validateGender = {
    ValidationResult.validate(
      gender.equals("男") || gender.equals("女"),
      "gender must be '男' or '女'")
  }
}
