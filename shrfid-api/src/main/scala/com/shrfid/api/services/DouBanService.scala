package com.shrfid.api.services

import com.shrfid.api.http.DouBanRequest
import com.twitter.util.Future

/**
  * Created by Administrator on 2017/3/15.
  */
trait DouBanService {
  def findBookByISBNFromDouBan(request: DouBanRequest): Future[(Int, String)]

}
