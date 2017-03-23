package com.shrfid.api.domains.book

import com.shrfid.api._
import com.shrfid.api.http.Elastic.stack.PostBookStackRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json
/**
  * Created by jiejin on 9/10/16.
  */


object BookStack {
  implicit val bookStackFmt = Json.format[BookStack]

  def toDomain(request: PostBookStackRequest): Future[BookStack] = {
    Future.value(BookStack(request.name, request.isActive, request.description, request.branchId))
  }
}

case class BookStack(name: String, is_active: Boolean, description: String, branch: String, datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}

object BookStackNested {
  implicit val bookStackFmt = Json.format[BookStackNested]

  def toDomain(id: String, bookStack: BookStack, bookBranch: Map[String, BookBranchWithId]): BookStackNested = {
    BookStackNested(id, bookStack.name, bookStack.is_active, bookStack.description, bookBranch.get(bookStack.branch), bookStack.datetime)
  }
}

case class BookStackNested(id: String, name: String, is_active: Boolean, description: String, branch: Option[BookBranchWithId], datetime: String) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}
