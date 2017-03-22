package com.shrfid.api.http.auth

import java.sql.Date

import com.twitter.finatra.request.Header
import com.twitter.finatra.validation.Size

/**
  * Created by jiejin on 13/10/16.
  */
case class PutAuthUserRequest(@Header Authorization: String,
                              @Size(min = 1, max = 30) username: String,
                              @Size(min = 0, max = 30) identity: String = "",
                              @Size(min = 0, max = 30) fullName: String = "",
                              gender: String = "男",
                              dob: Option[Date],
                              @Size(min = 0, max = 254) email: Option[String],
                              @Size(min = 0, max = 20) mobile: Option[String],
                              @Size(min = 0, max = 254) address: Option[String],
                              @Size(min = 0, max = 20) postcode: Option[String],
                              @Size(min = 0, max = 254) profileUrl: Option[String]
                             )
