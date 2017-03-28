package com.shrfid.api.domains.readable

import com.shrfid.api._
import com.shrfid.api.http.Elastic.branch.PostBookBranchRequest
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Created by jiejin on 25/11/16.
  */
object BookBranch {
  implicit val fmt = Json.format[BookBranch]

  def toDomain(request: PostBookBranchRequest): Future[BookBranch] = {
    Future.value(BookBranch(request.name, request.isActive, request.isRoot, request.description))
  }
}

case class BookBranch(name: String, is_active: Boolean, is_root: Boolean, description: String, datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}


object BookBranchWithId {
  implicit val fmt = Json.format[BookBranchWithId]

  def toDomain(id: String, bookBranch: BookBranch): BookBranchWithId = {
    println(bookBranch)
    BookBranchWithId(id, bookBranch.name, bookBranch.is_active, bookBranch.is_root, bookBranch.description, bookBranch.datetime)
  }
}

case class BookBranchWithId(id: String, name: String, is_active: Boolean, is_root: Boolean, description: String, datetime: String = Time.now.toString) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)
}