package com.shrfid.api.controllers

import javax.inject.Inject

import com.shrfid.api.http.DouBanRequest
import com.shrfid.api.services.DouBanService
import com.twitter.finagle.http.{Request, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder

/**
  * Created by Administrator on 2017/3/20.
  */
class DouBanController @Inject()(douBanService: DouBanService, response: ResponseBuilder) extends Controller {

  get("/api/douban/:isbn") { request: DouBanRequest =>
    for {
      r <- douBanService.findBookByISBNFromDouBan(request)
    } yield r match {
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound("sss").contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

}
