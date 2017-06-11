package com.shrfid.api.http.Elastic.book.solicitedPeriodicals

import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.twitter.finatra.request.{Header, RouteParam}
import com.shrfid.api._

// Created by kuangyuan 5/9/2017

case class PatchSolicitedRequest(
                                 @Header Authorization: String,
                                 @RouteParam id: String,
                                 title: Option[String],
                                 period: Option[String],
                                 publisher: Option[String],
                                 solicitedDate: Option[String],
                                 stack: Option[String],
                                 batch: Option[String],
                                 price: Option[Double],
                                 isActive: Option[Boolean],
                                 isSubscribed: Option[Boolean],
                                 description: Option[String],
                                 datetime: String = Time.now.toString
                                 ) extends BasePatchByIdRequest(Authorization, id, datetime)

case class PatchSolicitedResolicitRequest(@Header Authorization: String, @RouteParam id: String)