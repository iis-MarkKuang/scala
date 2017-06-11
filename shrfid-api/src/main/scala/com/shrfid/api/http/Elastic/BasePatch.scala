package com.shrfid.api.http.Elastic

import com.shrfid.api._

/**
  * Created by jiejin on 8/12/16.
  */
abstract class BasePatchByIdRequest(Authorization: String, id: String, datetime: String) {
  def patchRequest = {
    getCCParams(this, Seq("id", "Authorization"))
  }
}
