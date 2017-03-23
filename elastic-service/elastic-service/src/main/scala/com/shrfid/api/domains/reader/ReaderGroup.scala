package com.shrfid.api.domains.reader

import com.shrfid.api.Time
import com.shrfid.api.http.Elastic.reader.group.PostReaderGroupRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 19/09/2016.
  */


object ReaderGroup {
  implicit val readerReaderFmt = Json.format[ReaderGroup]

  def toDomain(request: PostReaderGroupRequest): Future[ReaderGroup] = {
    Future.value(ReaderGroup(request.name, request.isActive, request.description))
  }
}

case class ReaderGroup(name: String, is_active: Boolean, description: String,
                       datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}