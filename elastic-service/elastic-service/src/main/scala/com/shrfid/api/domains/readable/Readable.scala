package com.shrfid.api.domains.readable

import com.twitter.util.Future
/**
  * Created by kuang on 2017/3/28.
  */
trait Readable { // 读物trait
  val barcode: String

  val rfid: String

  val reference: String

  val category: String

  val title: String

  val stack: String //馆藏库id

  val is_available: Boolean

  val is_active: Boolean

  val datetime: String

  val description: String
}
