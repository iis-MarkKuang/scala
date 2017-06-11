package com.shrfid.api.domains.book

import com.shrfid.api.http.Elastic.book.solicitedPeriodicals.PostSolicitedRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by kuangyuan 5/8/2017
  */

object SolicitedPeriodical {
  implicit val solicitedPeriodicalFmt = Json.format[SolicitedPeriodical]

  def toDomain(request: PostSolicitedRequest): Future[SolicitedPeriodical] = {
    Future.value(
      SolicitedPeriodical(
        request.title,
        request.period,
        request.ISSN,
        request.publisher,
        request.solicitedDate,
        request.stack,
        request.batch,
        request.price,
        request.lastSerial,
        request.description,
        request.datetime,
        request.isActive,
        request.isSubscribed
      )
    )
  }
}

case class SolicitedPeriodical(
                              title: String,
                              period: String,
                              ISSN: String,
                              publisher: String,
                              solicited_date: String,
                              stack: String,
                              batch: String,
                              price: Double,
                              last_serial: Int,
                              description: String,
                              datetime: String,
                              is_active: Boolean,
                              is_subscribed: Boolean
                              ) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

}

object SolicitedPeriodicalWithId {
  implicit val solicitedPeriodicalFmt = Json.format[SolicitedPeriodicalWithId]

  def toDomain(id: String, sp: SolicitedPeriodical): SolicitedPeriodicalWithId = {
    SolicitedPeriodicalWithId(id, sp.title, sp.period, sp.ISSN, sp.publisher, sp.solicited_date, sp.stack, sp.batch, sp.price, sp.last_serial, sp.description,
      sp.datetime, sp.is_active, sp.is_subscribed)
  }
}

case class SolicitedPeriodicalWithId(
                                     id: String,
                                     title: String,
                                     period: String,
                                     ISSN: String,
                                     publisher: String,
                                     solicited_date: String,
                                     stack: String,
                                     batch: String,
                                     price: Double,
                                     last_serial: Int,
                                     description: String,
                                     datetime: String,
                                     is_active: Boolean,
                                     is_subscribed: Boolean
                                    ) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

}