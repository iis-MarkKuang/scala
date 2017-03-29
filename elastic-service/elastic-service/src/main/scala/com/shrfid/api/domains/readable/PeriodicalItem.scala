package com.shrfid.api.domains.readable

import com.shrfid.api._
import com.shrfid.api.persistence.elastic4s.BaseDoc
import com.shrfid.api.persistence.slick.BaseEntity
import com.shrfid.api.persistence.slick.readable.{BookItemEntity, PeriodicalItemEntity}
import play.api.libs.json.Json

/**
  * Created by kuang on 2017/3/28.
  */
object Periodical {
  implicit val periodicalFmt = Json.format[Periodical]
}

case class Periodical(barcode: String,
                      rfid: String,
                      reference: String = "",
                      category: String = "",
                      title: String = "",
                      stack: String,
                      clc: String = "",
                      periodical_index: Int = -1, //期刊次号
                      is_available: Boolean = true, //期刊是否在借
                      is_active: Boolean = true,
                      user: String = "", //编目人
                      datetime: String = Time.now.toString,
                      description: String = "") extends BaseDoc with Readable{
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

  def toPeriodicalInfo: PeriodicalInfo = {
    PeriodicalInfo(barcode, rfid, reference, category, title, stack, clc)
  }
}

object PeriodicalWithId {
  implicit val periodicalFmt = Json.format[PeriodicalWithId]

  def toDomain(id: String, periodical: Periodical) : PeriodicalWithId = {
    PeriodicalWithId(id, periodical.barcode, periodical.rfid, periodical.reference,
      periodical.category, periodical.title, periodical.stack, periodical.clc,
      if (periodical.periodical_index == -1) "" else periodical.periodical_index.toString,
      if (periodical.periodical_index == -1) "" else s"""{periodical.clc}.${periodical.periodical_index}""",
      periodical.is_available,
      periodical.is_active,
      periodical.user,
      periodical.description,
      periodical.datetime)
  }
}

case class PeriodicalWithId(id: String,
                            barcode: String,
                            rfid: String,
                            reference: String,
                            category: String ,
                            title: String,
                            stack: String, // 馆藏库id
                            clc: String,
                            periodical_index: String, // 期刊次号
                            call_no: String,
                            is_available: Boolean, // 图书是否在借
                            is_active: Boolean,
                            user: String, // 编目人
                            description: String,
                            datetime: String) extends BaseDoc {
  lazy val json = Json.toJson(this)
  lazy val jsonStringify = Json.stringify(json)

}

object PeriodicalItem {
  implicit val periodicalItemFmt = Json.format[PeriodicalItem]

  def toDomain(p: PeriodicalItemEntity): PeriodicalItem = {
    PeriodicalItem(p.id,
      optionStrToStr(p.reference),
      optionStrToStr(p.title),
      p.barcode,
      optionStrToStr(p.rfid),
      itemCategory(p.categoryId),
      p.stackId,
      optionStrToStr(p.clc),
      p.periodicalIndex,
      p.isAvailable,
      p.userId,
      p.createAt.toString,
      p.updateAt.toString)
  }
}

case class PeriodicalItem(id: Int,
                    reference: String,
                    title: String,
                    barcode: String,
                    rfid: String,
                    category: String, // 文献类型 待补充
                    stackId: Int,
                    clc: String,
                    periodicalIndex: Option[Int], // 书次号
                    isAvailable: Boolean, // 图书是否在借
                    userId: Option[Int], // 编目人
                    createAt: String, // 编目记录创建日期
                    updateAt: String) extends BaseEntity