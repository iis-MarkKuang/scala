package com.shrfid.api.persistence.elastic4s

import javax.inject.Inject

import com.shrfid.api._
import com.shrfid.api.modules.Elastic4SModule._
import com.twitter.util.Future
import com.shrfid.api.domains.book.SolicitedPeriodical
import play.api.libs.json.Json

/**
  * Created by kuang on 2017/5/9.
  */
class SolicitedRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "solicitedperiods"
  val _type = "info"

  val dal = new BaseDalImpl[SolicitedPeriodical](db)(_index, _type) {
    override def id(doc: SolicitedPeriodical): Future[String] = Future.value(Security.digest(doc.toString))

    def resolicit(_id: String, solicited_date: String) = {
      this.updateDoc(_id, Map("solicited_date" -> solicited_date, "datetime" -> Time.now.toString))
    }

    def findLastSolicitedSerial(solicited_periodical: String): Future[SearchResponse] = {
      this.findById(solicited_periodical) flatMap {
        case n if n._1 == 404 => Future.value(404, valueJson("error", "periodical not found"))
        case y => Future.value(200, Json.parse(y._2).as[SolicitedPeriodical].last_serial.toString)
      }
    }
  }
}
