package com.shrfid.api.persistence.elastic4s

import com.shrfid.api.modules.Elastic4SModule.Elastic4SDatabaseSource

/**
  * Created by kuang on 2017/3/28.
  */
class PeriodicalItemRepo @Inject()(db: Elastic4SDatabaseSource) {
  val _index = "periodical"
  val _type = "info"

  val dal = new BaseDalImpl[]
}
