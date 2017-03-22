package com.shrfid.api.http.Elastic.reader.member

import com.shrfid.api.controllers.Router
import com.shrfid.api.http.Elastic.BaseGetListRequest
import com.twitter.finatra.request.{Header, QueryParam, RouteParam}
import com.twitter.finatra.validation.{MethodValidation, ValidationResult}

/**
  * Created by jiejin on 7/11/16.
  */
case class GetReaderMemberBorrowRecordListRequest(@Header Authorization: String,
                                                  @RouteParam id: String,
                                                  @QueryParam record: Option[String] = Some("current"),
                                                  @QueryParam limit: Int = 100,
                                                  @QueryParam offset: Int = 0,
                                                  @QueryParam ordering: Option[String])
  extends BaseGetListRequest(Authorization, limit, offset, ordering) {
  @MethodValidation
  def validateRecord = {
    ValidationResult.validate(
      record.getOrElse("").equals("current") || record.getOrElse("").equals("all"),
      "record must be 'current' or 'all'")
  }

  val recordQueryParam = UrlQueryParam("record", record)

  override val filter = record match {
    case Some("current") => QueryFilter(must = Seq(UrlQueryParam("reader.id", Some(id)).matchPhraseQ), not = Seq(UrlQueryParam("_return.datetime", Some("*")).exist), sourceExclude = Seq("reader"))
    case Some("all") => QueryFilter(must = Seq(UrlQueryParam("reader.id", Some(id)).matchPhraseQ), sourceExclude = Seq("reader"))
    case _ => QueryFilter(must = Seq(UrlQueryParam("reader.id", Some(id)).matchPhraseQ), sourceExclude = Seq("reader"))
  }

  override val path: String = Router.ReaderMember.currentHoldings.replace(":id", id)
  override val requestPath: String = _requestPath(Seq(recordQueryParam))
}


