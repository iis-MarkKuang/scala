package com.shrfid.api.domains.book

import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json


// Created by kuangyuan 5/11/2017

object PeriodicalBatch {
  implicit val periodicalBatchFmt = Json.format[PeriodicalBatch]

  def toDomain(request: PeriodicalBatch): Future[PeriodicalBatch] = {
    Future.value(
      PeriodicalBatch(
        request.creator,
        request.period_number,
        request.items_number,
        request.arrived_items_number,
        request.datetime,
        request.description,
        request.is_active
      )
    )
  }
}

case class PeriodicalBatch(
                          creator: String,
                          period_number: Int,
                          items_number: Int,
                          arrived_items_number: Int,
                          datetime: String,
                          description: String,
                          is_active: Boolean
                          ) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}