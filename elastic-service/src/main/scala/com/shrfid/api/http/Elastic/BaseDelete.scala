package com.shrfid.api.http.Elastic

/**
  * Created by jiejin on 8/12/16.
  */
abstract class BaseDeleteById(Authorization: String, id: String)


abstract class BaseDeleteBulk(Authorization: String, ids: Seq[String])