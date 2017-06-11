package com.shrfid.api.persistence.elastic4s

import javax.inject.{Inject, Singleton}

import com.shrfid.api.{Security, Time}
import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api.domains.book.{ReservationHistory, TimeLocation}
import com.shrfid.api.modules.Elastic4SModule._
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by weijing on 6/4/17.
  */

@Singleton
class ReservationHistoryRepo @Inject()(db: Elastic4SDatabaseSource) {

  val _index = "reservation"
  val _type = "info"

  val dal = new BaseDalImpl[ReservationHistory](db)(_index, _type) {
    override def id(doc: ReservationHistory): Future[String] = Future.value(Security.digest(doc.toString))

    def reservationAt(_id: String, _reservation: TimeLocation, now: String) = {
      this.updateDoc(_id, Map("_reservation" -> Map("location" -> _reservation.location, "datetime" -> _reservation.datetime), "datetime" -> now, "status"-> "reservation"))
    }


    def updateStatus(_id: String, status: String = "cancel", now: String = Time.now.toString) = {
      this.updateDoc(_id, Map("status" -> status, "datetime" -> now))
    }


    def findReaderReservation(readerId: String,barcode: String) :Future[Seq[ReservationHistory]]= {
      db.execute(
        search(_index / _type)    query {
          boolQuery().must(termQuery("reader.id.keyword", readerId),termQuery("status.keyword", "reservation"),termQuery("book.barcode.keyword", barcode))
        }
      ).toTwitterFuture.map(a=>(a.totalHits, a.successfulShards == a.totalShards) match {
        case (0,_) => Seq()
        case (_, false) => Seq()
        case _ => a.hits.map( l => Json.parse(l.sourceAsString).as[ReservationHistory])
      } )
    }




  }
}