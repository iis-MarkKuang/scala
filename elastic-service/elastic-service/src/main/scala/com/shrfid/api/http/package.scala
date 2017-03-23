package com.shrfid.api

/**
  * Created by jiejin on 13/09/2016.
  */
package object http {
  private val paging = (path: String, limit: Int, offset: Int) => (path + "&offset=" + offset + "&limit=" + limit).replace(" ", "")
  val next = (path: String, limit: Int, offset: Int, count: Int) =>
    if (offset + limit >= count || limit <= 0) "" else paging(path, if (limit > count) count else limit, offset + limit)
  val prev = (path: String, limit: Int, offset: Int, count: Int) =>
    if (offset <= 0 || offset > count || limit <= 0) "" else paging(path, if (limit > count) count else limit, if ((offset - limit) > 0) offset - limit else 0)
}
