package com.shrfid.api.http.Elastic.book

import com.shrfid.api.http.Elastic.BasePatchByIdRequest
import com.shrfid.api._
import com.twitter.finatra.request.{Header, RouteParam}

// Created by kuangyuan 5/13/2017

case class PatchBookItemByIdRequest(@Header Authorization: String,
                                    @RouteParam id: String,
                                    rfid: Option[String],
                                    stackId: Option[String],
                                    datetime: String = Time.now.toString
                                   ) extends BasePatchByIdRequest(Authorization, id, datetime)

case class PatchBookItemsActiveStatusRequest(@Header Authorization: String,
                                             ids: Option[Seq[String]],
                                             description: Option[String],
                                             is_active: Boolean = false)

case class PatchBookItemsStackIdRequest(@Header Authorization: String,
                                        ids: Option[Seq[String]],
                                        stackId: String)