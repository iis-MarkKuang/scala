package com.shrfid.api.domains.book

import com.shrfid.api._
import com.shrfid.api.persistence.elastic4s.BaseDoc
import play.api.libs.json.{JsValue, Json}

/**
  * Created by jiejin on 5/12/16.
  */

// test

object test {
  implicit val testfmt = Json.format[test]
}

case class test(ISBN: String = "", 获得方式和或定价: String = "", 装订方式: String = "", 错误的ISBN号: String = "")

object _010 {
  implicit val _010fmt = Json.format[_010]
}

case class _010(ISBN: String = "", 装订方式: String = "", 获得方式和或定价: String = "", 错误的ISBN号: String = "")


object _011 {
  implicit val _011fmt = Json.format[_011]
}

case class _011(ISSN: String = "", 修饰: String = "", 获得方式和或定价: String = "", 废除的ISSN: String = "", 错误的ISSN: String = "")

object _100 {
  implicit val _100fmt = Json.format[_100]
}

case class _100(入档日期: String = "", 出版日期类型: String = "", 出版年1: String = "", 出版年2: String = "", 阅读对象代码: String = "",
                政府出版物代码: String = "", 修改记录代码: String = "", 编目语种: String = "", 音译代码: String = "", 字符集: String = "")

object _101 {
  implicit val _101fmt = Json.format[_101]
}

case class _101(翻译指示符: String = "", 作品语种: String = "")

object _102 {
  implicit val _102fmt = Json.format[_102]
}

case class _102(出版国代码: String = "", 出版地区代码: String = "")


object _200 {
  implicit val _200fmt = Json.format[_200]
}

case class _200(正题名: String = "", 正题名汉语拼音: String = "", 一般资料标识: String = "", 另一作者的正题名: String = "",
                并列正题名: String = "", 副题名及其它说明题名的文字: String = "", 副题名或其他说明题名文字的汉语拼音: String = "",
                第一责任者: String = "", 第一责任者的汉语拼音: String = "", 其它责任者: String = "")

object _210 {
  implicit val _210fmt = Json.format[_210]
}

case class _210(出版发行地: String = "", 出版发行者地址: String = "", 出版发行者名称: String = "", 出版发行日期: Int = 0,
                印刷地: String = "", 印刷者地址: String = "", 印刷者: String = "", 印刷日期: String = "")


object _215 {
  implicit val _215fmt = Json.format[_215]
}

case class _215(页数或卷册数: String = "", 图及其它细节: String = "", 尺寸或开本: String = "", 附件: String = "")


object _225 {
  implicit val _225fmt = Json.format[_225]
}

case class _225(正丛编题名: String = "", 并列丛编题名: String = "", 丛编副题名及其它信息: String = "", 丛编责任者: String = "", 分册号: String = "",
                分册题名: String = "", 卷标识: String = "", ISSN: String = "", 并列丛编题名文种: String = "")

object _300 {
  implicit val _300fmt = Json.format[_300]
}

case class _300(附注内容: String = "")

object _606 {
  implicit val _606fmt = Json.format[_606]
}

case class _606(主标目: String = "", 主题复分: String = "", 地区复分: String = "", 年代复分: String = "")

object _7XX {
  implicit val _7XXfmt = Json.format[_7XX]
}

case class _7XX(主标目: String = "", 主标目汉语拼音: String = "", 人名的其它部分: String = "", 人名修饰语: String = "", 罗马数字: String = "",
                年代: String = "", 规范记录号: String = "", 著作责任: String = "")


object _801 {
  implicit val _801fmt = Json.format[_801]
}

case class _801(国家代码: String = "", 机构名称: String = "", 处理日期: String = "", 编目条例代码: String = "")


object BookReference {
  implicit val _bookReferencefmt = Json.format[BookReference]

  def withKeyword(ref: BookReference) = ref.copy(keywords = s"""${ref.题名与责任者.正题名} ${ref.责任者.主标目}""")
}

case class BookReference(ISBN: _010 = _010(),
                         ISSN: _011 = _011(),
                         一般处理数据: _100 = _100(),
                         作品语种: _101 = _101(),
                         出版国别: _102 = _102(),
                         题名与责任者: _200 = _200(),
                         出版发行: _210 = _210(),
                         载体形态: _215 = _215(),
                         丛编: _225 = _225(),
                         附注: _300 = _300(),
                         普通主题: _606 = _606(),
                         中国图书馆图书分类法分类号: String = "",
                         中国科学院图书馆图书分类法分类号: String = "",
                         责任者: _7XX = _7XX(),
                         记录来源: _801 = _801(),
                         keywords: String = "",
                         datetime: String = Time.now.toString) extends BaseDoc {
  val json: JsValue = Json.toJson(this)

  val jsonStringify: String = Json.stringify(json).replace("，", ",").trim

  def id: String = {
    this.copy(datetime = "").jsonStringify.hashCode().toString
  }
}