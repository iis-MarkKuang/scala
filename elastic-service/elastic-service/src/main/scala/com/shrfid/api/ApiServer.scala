package com.shrfid.api

import java.net.InetSocketAddress

import com.twitter.conversions.time._
import com.shrfid.api.modules.{ApiServiceThriftClientIdModule, ServicesModule}
import com.elastic_service.elasticServer.ElasticServerThrift$FinagleClient
import com.elastic_service.requestStructs._
import com.elastic_service.elasticServer._
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.finagle.{Filter, Http, Service}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.thrift.{ThriftClientExceptionMapper, ThriftFilter}
import com.twitter.finatra.thrift.filters._
import com.twitter.finagle.Thrift
import java.io.FileInputStream
import com.twitter.inject.Logging
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}

// Added by Kuang
import com.twitter.finatra.thrift.ThriftServer
import com.twitter.finatra.thrift.routing.ThriftRouter

import org.marc4j.MarcStreamReader
import org.marc4j.marc._

import play.api.libs.json.{JsValue, Json}

/**
  * Created by jiejin on 19/01/2016.
  */

//object CorsFilter {
//  private[this] val sep = ", *".r
//
//  def apply(origin: String = "*",
//            methods: String = "GET, POST, PUT, PATCH, DELETE, OPTIONS",
//            headers: String = "Content-Type, Authorization, x-requested-with",
//            exposes: String = ""): Filter[Request, Response, Request, Response] = {
//    val methodList = Some(sep.split(methods).toSeq)
//    val headerList = Some(sep.split(headers).toSeq)
//    val exposeList = sep.split(exposes).toSeq
//    new Cors.HttpFilter(Cors.Policy(
//      { _ => Some(origin) }, { _ => methodList }, { _ => headerList },
//      exposeList,
//      supportsCredentials = true))
//  }
//}
//
//object Proxy extends TwitterServer with Logging {
//  override def failfastOnFlagsNotParsed: Boolean = true
//
//  //apiUrl ensuring(apiServiceUrlFlag.isDefined)
//  //proxyUrl ensuring(proxyServiceUrlFlag.isDefined)
//  val client: Service[Request, Response] = Http.newService(Config.address+ Config.port)
//
//  val corsFilter = CorsFilter()
//  val service = corsFilter andThen client
//
//
//  val server = Http.serve(Config.proxy, service)
//
//  def main() {
//
//    Await.ready(server)
//  }
//}
//
//object ApiServerMain extends ApiServer
//
//class ApiServer extends HttpServer {
//
//  // override a couple of default settings
//  override val name = Config.name
//
//  override def defaultHttpPort = 9992
//
//  override def modules = Seq(ApiServiceThriftClientIdModule,
//    ServicesModule)
//
//  override def defaultFinatraHttpPort = Config.port
//
//  override val disableAdminHttpServer = Config.disableAdmin
//  override val isTraceEnabled = Config.isTraceEnabled
//
//  override def configureHttp(router: HttpRouter) {
//    router
//      //.filter(CorsFilter(origin = "*"))
//      .exceptionMapper[ThriftClientExceptionMapper]
//      .filter[LoggingMDCFilter[Request, Response]]
//      .filter[TraceIdMDCFilter[Request, Response]]
//      .filter[CommonFilters]
////      .add[controllers.AuthController]
//      .add[controllers.StackController]
//      .add[controllers.BranchController]
//      .add[controllers.BookController]
//      .add[controllers.ReaderController]
//      .add[controllers.VendorController]
//  }
//}
//
//
//
//object MarcTest {
//  def main(args: Array[String]) {
//    val in = new FileInputStream("/Users/jiejin/Projects/Scala/qiuxin/marcpost/29.mrc")
//    val reader = new MarcStreamReader(in, "UTF-8")
//    var a = 1
//    while (a <= 100) {
//      val record = CNMARC(reader.next())
//      println(s"""********************\n ${record.pretty}""")
//      a += 1
//    }
//  }
//}

object ElasticServerMain extends ElasticServer

class ElasticServer
  extends ThriftServer
  with HttpServer {

  override val name = Config.name

  // DO NOT MODIFY FORMAT
  override def defaultFinatraThriftPort = "127.0.0.1:9995"
  override def defaultHttpPort = 9992

  override def modules = Seq(ApiServiceThriftClientIdModule,
    ServicesModule)

  override val disableAdminHttpServer = Config.disableAdmin
  override val isTraceEnabled = Config.isTraceEnabled
  override val failfastOnFlagsNotParsed = false

  override val defaultShutdownTimeout = 30.seconds
  override val defaultThriftShutdownTimeout = 45.seconds

  override protected def configureHttp(router: HttpRouter) = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
//      .add[controllers.StackController]
//      .add[controllers.BranchController]
//      .add[controllers.BookController]
//      .add[controllers.ReaderController]
//      .add[controllers.VendorController]
  }

  override protected def configureThrift(router: ThriftRouter) {
    router
//      .filter[ThriftMDCFilter]
//      .filter[AccessLoggingFilter]
      .filter[ThriftMDCFilter]
      .filter[StatsFilter]
      .filter(ThriftFilter.Identity)
      .add[controllers.ThriftController]
  }
}


class GetBookStackByIdRequestThriftImpl extends GetBookStackByIdRequestThrift {
  override def authorization = "Bearer TOKEN"

  override def id = "3c893b3740b3a70b097247f68d9266b6"
}

class PostBookStackRequestThriftImpl extends PostBookStackRequestThrift {
  override def authorization = "BBB"
  override def name = "stack 1"
  override def isActive = false
  override def branchId = "1a32c619d67d6a67b708b32de053ab20"
  override def description = "stack 1 create test book stack"
}

class PostVendorOrderRequestThriftImpl extends PostVendorOrderRequestThrift {
  override def authorization = "AAA"
  override def title = "kuangyuan"
  override def author = "kuangyuan"
  override def publisher = "123123"
  override def isbn = "123123123123"
  override def price = 99.99
  override def quantity = 100
  override def total = 9500.00
  override def orderDate = "2017-03-22"
  override def description = ""
  override def vendorId = "0d49fb6a06a07ccc00d378666acd1fc6"
}

class PatchVendorMemberByIdRequestThriftImpl extends PatchVendorMemberByIdRequestThrift {
  override def authorization = "abc"
  override def id = "91e59b9657cee7f469d830bb9384c0ce"
  val nameParam : Option[String] = Some("bykuang")
  override def name = Some("bykuang")
  override def isActive = None
  override def description = Some("updatedByKuang")
  override def datetime = "2017-03-22T00:00:00"
}

object ElasticClientMain {

  def main(args: Array[String]): Unit = {
    val service = ClientBuilder()
//      .hosts(new InetSocketAddress(args(0), args(1).toInt))
      .hosts(new InetSocketAddress("127.0.0.1", 9995))
      .codec(ThriftClientFramedCodec())
      .hostConnectionLimit(1)
      .tcpConnectTimeout(3.seconds)
      .build()

    val client = new ElasticServerThrift$FinagleClient(service)
//    val client = Thrift.client.newIface[ElasticServerThrift[Future]]("127.0.0.1:9995")
    val futureRes = client.increment(1)

    futureRes onSuccess( a => println(a) )
    futureRes onFailure( ex => println(ex) )
    val futureRes2 = client.findBookStackById("user123", new GetBookStackByIdRequestThriftImpl())

    futureRes2 onSuccess( a => println(a) )
    futureRes2 onFailure( ex => println(ex) )

    //    val futureRes2 = client.insertBookStack("user246", new PostBookStackRequestThriftImpl())
//    futureRes2 onSuccess( a => println(a) )
//    futureRes2 onFailure( ex => println(ex) )
//
//    val futureRes3 = client.insertVendorOrder("user2k3", new PostVendorOrderRequestThriftImpl())
//    futureRes3 onSuccess( a => println(a) )
//    futureRes3 onFailure( ex => println(ex) )

//    val futureRes4 = client.updateVendorMemberById("user132", new PatchVendorMemberByIdRequestThriftImpl())
//    futureRes4 onSuccess( a => println(a) )
//    futureRes4 onFailure( ex => println(ex) )
  }
}




case class CNMARC(raw: Record) {
  private val EMPTY = ""

  def language(languageCode: String) = languageCode match {
    case "chi" => "汉语"
    case "eng" => "英语"

    case "alb" => "阿尔巴尼亚语"

    case "ben" => "孟加拉语"
    case "bul" => "保加利亚语"
    case "bur" => "缅甸语"

    case "cam" => "高棉语"
    case "cze" => "捷克语"

    case "dan" => "丹麦语"
    case "dut" => "衫加餇"

    case "egy" => "埃及语"
    case "esp" => "世界语"

    case "fre" => "法语"

    case "ger" => "德语"
    case "gre" => "西腊语(近代)"

    case "heb" => "希伯来语"
    case "hun" => "匈牙利语"

    case "ice" => "冰岛语"
    case "inc" => "印度语"
    case "ind" => "印尼语"
    case "ita" => "意大利语 "

    case "jpn" => "日语"

    case "kaz" => "哈萨克语"
    case "kir" => "吉尔吉斯语"
    case "kon" => "刚果语"
    case "kor" => "朝鲜语"

    case "lao" => "寮国语"
    case "lat" => "拉丁语"

    case "may" => "马来语"
    case "mlt" => "马尔他语"
    case "mon" => "蒙古语"
    case "mul" => "多种语言"

    case "nep" => "尼泊尔语"
    case "nor" => "挪威语"

    case "per" => "波斯语(近代)"
    case "pol" => "波兰语"
    case "por" => "葡萄牙语"

    case "rum" => "罗马尼亚语"
    case "rus" => "俄语"

    case "san" => "梵语"
    case "slo" => "斯洛伐克语"
    case "spa" => "西班牙语"
    case "swe" => "瑞典语"
    case "syr" => "叙利亚语"

    case "tha" => "泰语"
    case "tib" => "藏语"
    case "tur" => "土耳其语"

    case "uig" => "维吾尔语"

    case "vie" => "越南语"

    case "yao" => "瑶族语"
    case "yid" => "犹太语"

    case _ => ""
  }

  private def indicator1(data: List[DataField]) = {
    data.map {
      case null => EMPTY
      case exist =>
        exist.getIndicator1
    }.distinct.mkString(",").trim
  }

  private def subField(data: List[DataField], subField: Char) = {
    data.map {
      case null => EMPTY
      case exist =>
        exist.getSubfield(subField) match {
          case null => EMPTY
          case o => o.getData
        }
    }.distinct.mkString(",").trim
  }

  import scala.collection.JavaConverters._

  private def dataField(term: String) = raw.getVariableFields(term).asScala.toList.asInstanceOf[List[DataField]]

  private def dataField(terms: Seq[String]) = raw.getVariableFields(terms.toArray).asScala.toList.asInstanceOf[List[DataField]]
  /*
  记录控制号--必备
  本字段包含能唯一标识本记录的控制号,由编制本书目记录的机构
  提供。
  本字段不可重复。
  本字段没有指示符。
  本字段没有子字段标识符。
  本字段数据为10个字符长。
  固定长数据元素
  数据元素名称        字符数    字符位置
  (1)资料类型代码     2         0-1
  (2)编目年          2         2-3
  (3)编目流水号       6         4-9

  资料类型代码：
  中文图书      01              中文期刊       11
  西文图书      02              西文期刊       12
  日文图书      03              日文期刊       13
  俄文图书      04              俄文期刊       14
  */
  lazy val _001 = raw.getControlNumberField

  lazy val 资料类型代码 = _001.getData.take(2) match {
    case "01" => "中文图书"
    case "02" => "西文图书"
    case "03" => "日文图书"
    case "04" => "俄文图书"
    case "11" => "中文期刊"
    case "12" => "西文期刊"
    case "13" => "日文期刊"
    case "14" => "俄文期刊"
    case _ => EMPTY
  }

  /*
  国际标准书号(ISBN)
  本字段包含国际标准书号.当记录包含多个ISBN时.需加区分的
  修饰成分.该字段还包含作品的可获得方式/或价格.
  本字段可重复.在有效的ISBN超过一个的情况下,可重复使用。
  两个指示符未定,填空.
  ---------子字段标识符、内容、可否重复---------
  a  ISBN号            不重复
  b  装订方式           不重复
  d  获得方式和或定价    不重复
  z  错误的ISBN号       可重复
  */
  implicit val _010fmt = Json.format[_010]
  case class _010(ISBN: String, 装订方式: String, 获得方式和或定价: String, 错误的ISBN号: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringify: String = Json.stringify(json)
  }

  object _010 {
    private lazy val raw = dataField("010")
    lazy val ISBN = subField(raw, 'a').replace("-", "")
    lazy val 装订方式 = subField(raw, 'b')
    lazy val 获得方式和或定价 = subField(raw, 'd')
    lazy val 错误的ISBN号 = subField(raw, 'z')
    lazy val obj = _010(ISBN, 装订方式, 获得方式和或定价, 错误的ISBN号)
  }

  /*
  国际标准连续出版物号(ISSN)
  本字段包含由ISDS中心指定的ISSN,作品的可获得方式,定价以及
  错误的ISSN.
    本字段可重复。
    指示符1：未定义,填空格。
    指示符2：未定义,填空格。
  --------- 子字段标识符、内容、可否重复 ---------
  a  ISSN              不重复
  b  修饰(ISDS不用)      不重复
  d  获得方式和/或定价    可重复
  y  废除的ISSN          可重复
  z  错误的ISSN          可重复
   */
  implicit lazy val _011Fmt = Json.format[_011]
  case class _011(ISSN: String, 修饰: String, 获得方式和或定价: String, 废除的ISSN: String, 错误的ISSN: String) {

    val json: JsValue = Json.toJson(this)
    val jsonStringify: String = Json.stringify(json)
  }

  object _011 {
    private lazy val raw = dataField("011")
    lazy val ISSN = subField(raw, 'a').replace("-", "")
    lazy val 修饰 = subField(raw, 'b')
    lazy val 获得方式和或定价 = subField(raw, 'd')
    lazy val 废除的ISSN = subField(raw, 'y')
    lazy val 错误的ISSN = subField(raw, 'z')
    lazy val obj = _011(ISSN, 修饰, 获得方式和或定价, 废除的ISSN, 错误的ISSN)
  }

  // TODO: 020

  // TODO: 021

  // TODO: 022

  // TODO: 040

  /*
  一般处理数据--必备
  本字段包含适用于任何载体作品记录的基本代码数据。
  本字段不可重复。
  指示符1：未定义，填空格。
  指示符2：未定义，填空格。
  ──────  子字段标识符、内容、可否重复 ───────
  a  一般的处理数据          不重复     固定为36个字符长
  固定长数据元素：
  数据元素名称         字符数         字符位置
  ──────             ───           ────
  (1)入档日期(必备)     8            0-7
  (2)出版日期类型       1            8
  (3)出版日期1         4            9-12
  (4)出版日期2         4            13-16
  (5)阅读对象代码

   */
  implicit lazy val _100Fmt = Json.format[_100]
  case class _100(入档日期: String, 出版日期类型: String, 出版年1: String, 出版年2: String, 阅读对象代码: String,
                  政府出版物代码: String, 修改记录代码: String, 编目语种: String, 音译代码: String, 字符集: String) {

    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }
  object _100 {
    private lazy val raw = subField(dataField("100"), 'a')
    lazy val 入档日期 = raw.substring(0, 8)
    lazy val 出版日期类型 = raw.substring(8, 9) match {
      case "a" => "现期出版的连续出版物"
      case "b" => "已停刊的连续出版物"
      case "c" => "刊行状态不明的连续出版物"
      case "d" => "一次或一年内出全的专著"
      case "e" => "复制本(如重印本,摹写本,再版本)"
      case "f" => "出版时间不能确定的专著"
      case "g" => "连续出版超过一年的专著"
      case "h" => "既有出版时间又有版权日期的专著"
      case "i" => "既有发行/出版日期, 又有生产日期的专著"
      case "j" => "具有详细出版日期的专著"
      case _ => EMPTY
    }
    lazy val 出版年1 = raw.substring(9, 13).trim
    lazy val 出版年2 = raw.substring(13, 17).trim
    lazy val 阅读对象代码 = raw.substring(17, 20).trim.split("").map(
      _ match {
        case "a" => "普通青少年"
        case "b" => "学龄前儿童(0-5岁)"
        case "c" => "小学生(5-10岁)"
        case "d" => "少年(9-14岁)"
        case "e" => "青年(14-20岁)"
        case "k" => "研究人员"
        case "m" => "普通成人"
        case "u" => "不详"
        case "z" => "特殊读者(如盲人)"
        case _ => EMPTY
      }
    ).filterNot(_ == EMPTY).mkString(",")

    lazy val 政府出版物代码 = raw.substring(20, 21).map(
      _ match {
        case 'a' => "中央政府, 各部委"
        case 'b' => "直辖市、省、自治区"
        case 'c' => "省直辖市、县级"
        case 'd' => "市、镇、乡机构"
        case 'f' => "政府间组织机构"
        case 'h' => "层次未定"
        case 'u' => "不能确定是否是政府出版物"
        case 'y' => "非政府出版物"
        case 'z' => "其他政府层次"
        case _ => EMPTY
      }
    ).filterNot(_ == "").mkString(", ")

    lazy val 修改记录代码 = raw.substring(21, 22)
    lazy val 编目语种 = language(raw.substring(22, 25))

    lazy val 音译代码 = raw.substring(25, 26) match {
      case "a" => "ISO音译体系"
      case "b" => "其它"
      case "c" => "多种音译体系;ISO或其他体系"
      case "y" => "未使用音译"
      case _ => EMPTY
    }

    lazy val 字符集G1 = raw.substring(26, 28) match {
      case "01" => "ISO 646, IRV version(基本拉丁集)"
      case "02" => "ISO Registration #37(基本基里尔集)"
      case "03" => "ISO 5426(扩充拉丁集)"
      case "04" => "ISO DIS 5427(扩充基里尔集)"
      case "05" => "ISO 5428(西腊集)"
      case "06" => "ISO 6438(非洲编码字符集)"
      case "10" => "GB2312-80信息交换用汉字编码字符集资本集(双7位)"
      case "11" => "信息交换用汉字编码字符集基本集第一辅助集(双7位)"
      case "20" => "信息交换用汉字编码字符集基本集(双8位表示。基本、辅3、5集为一个集合)"
      case "21" => "信息交换用汉字编码字符集基本集(双8位表示的辅1、3、5集所构成的集合)"
      case _ => EMPTY
    }
    lazy val 补充字符集G2 = raw.substring(28, 30) match {
      case "12" => "信息交换试用汉字编码字符集辅助集3(双7位)"
      case _ => EMPTY
    }
    lazy val 补充字符集G3 = raw.substring(30, 32) match {
      case "13" => "信息交换试用汉字编码字符集辅助集5(双7位)"
      case _ => EMPTY
    }

    lazy val 题名语系代码 = raw.substring(32, 34) match {
      case "ea" => "汉语-文字类型未指明"
      case "eb" => "汉语-汉字"
      case "ec" => "汉语-汉语拼音"

      case "ba" => "拉丁语"
      case "ca" => "基里尔语"
      case "da" => "日语-文字类型未指明"
      case "db" => "日语-汉字"
      case "dc" => "日语-假名"
      case "fa" => "阿拉伯语"
      case "ga" => "西腊语"
      case "ha" => "希伯来语"
      case "ia" => "泰语"
      case "ja" => "梵语"
      case "ka" => "朝鲜语"
      case "la" => "泰米尔语"
      case "zz" => "其它"
      case _ => EMPTY
    }
    lazy val obj = _100(入档日期, 出版日期类型, 出版年1, 出版年2, 阅读对象代码, 政府出版物代码, 修改记录代码, 编目语种, 音译代码, 字符集G1)
  }

  // TODO: 101
  implicit lazy val _101Fmt = Json.format[_101]
  case class _101(翻译指示符: String, 作品语种: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _101 {
    private lazy val raw = dataField("101")
    lazy val 翻译指示符 = indicator1(raw) match {
      case "0" => "原文"
      case "1" => "译文"
      case "2" => "包含译文(摘要除外)"
      case _ => EMPTY
    }
    lazy val 作品语种 = language(subField(raw, 'a'))
    lazy val obj = _101(翻译指示符, 作品语种)
  }

  // TODO: 102
  implicit lazy val _102Fmt = Json.format[_102]
  case class _102(出版国代码: String, 出版地区代码: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _102 {
    private lazy val raw = dataField("102")
    lazy val 出版国代码 = subField(raw, 'a')
    lazy val 出版地区代码 = subField(raw, 'b')
    lazy val obj = _102(出版国代码, 出版地区代码)
  }

  // TODO: 105

  // TODO: 106

  // TODO: 200
  implicit lazy val _200Fmt = Json.format[_200]
  case class _200(正题名: String, 正题名汉语拼音: String, 一般资料标识: String, 另一作者的正题名: String,
                  并列正题名: String, 副题名及其它说明题名的文字: String, 副题名或其他说明题名文字的汉语拼音: String,
                  第一责任者: String, 第一责任者的汉语拼音: String, 其它责任者: String) {

    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _200 {
    private lazy val raw = dataField("200")
    //题名与责任说明项
    lazy val 正题名 = subField(raw, 'a')
    lazy val 正题名汉语拼音 = subField(raw, 'A')
    lazy val 一般资料标识 = subField(raw, 'b')
    lazy val 另一作者的正题名 = subField(raw, 'c')
    lazy val 并列正题名 = subField(raw, 'd')
    lazy val 副题名及其它说明题名的文字 = subField(raw, 'e')
    lazy val 副题名或其他说明题名文字的汉语拼音 = subField(raw, 'E')
    lazy val 第一责任者 = subField(raw, 'f')
    lazy val 第一责任者的汉语拼音 = subField(raw, 'F')
    lazy val 其它责任者 = subField(raw, 'g')
    lazy val obj = _200(正题名, 正题名汉语拼音, 一般资料标识, 另一作者的正题名, 并列正题名,
      副题名及其它说明题名的文字, 副题名或其他说明题名文字的汉语拼音, 第一责任者, 第一责任者的汉语拼音, 其它责任者)
  }

  // TODO: 210
  implicit lazy val _210Fmt = Json.format[_210]

  case class _210(出版发行地: String, 出版发行者地址: String, 出版发行者名称: String, 出版发行日期: Int,
                  印刷地: String, 印刷者地址: String, 印刷者: String, 印刷日期: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _210 {
    private lazy val raw = dataField("210")
    lazy val 出版发行地 = subField(raw, 'a')
    lazy val 出版发行者地址 = subField(raw, 'b')
    lazy val 出版发行者名称 = subField(raw, 'c')
    lazy val 出版发行日期 = subField(raw, 'd').replaceAll("[^\\d.]", "").take(4) match {
      case a if a.length == 4 => a.toInt
      case _ => 0
    }
    lazy val 印刷地 = subField(raw, 'e')
    lazy val 印刷者地址 = subField(raw, 'f')
    lazy val 印刷者 = subField(raw, 'g')
    lazy val 印刷日期 = subField(raw, 'h')
    lazy val obj = _210(出版发行地, 出版发行者地址, 出版发行者名称, 出版发行日期, 印刷地, 印刷者地址, 印刷者, 印刷日期)
  }


  // TODO: 215
  implicit lazy val _215Fmt = Json.format[_215]
  case class _215(页数或卷册数: String, 图及其它细节: String, 尺寸或开本: String, 附件: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _215 {
    private lazy val raw = dataField("215")
    lazy val 页数或卷册数 = subField(raw, 'a')
    lazy val 图及其它细节 = subField(raw, 'c')
    lazy val 尺寸或开本 = subField(raw, 'd')
    lazy val 附件 = subField(raw, 'e')
    lazy val obj = _215(页数或卷册数, 图及其它细节, 尺寸或开本, 附件)
  }

  implicit lazy val _225Fmt = Json.format[_225]
  case class _225(正丛编题名: String, 并列丛编题名: String, 丛编副题名及其它信息: String, 丛编责任者: String, 分册号: String,
                  分册题名: String, 卷标识: String, ISSN: String, 并列丛编题名文种: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _225 {
    private lazy val raw = dataField("225")
    lazy val 正丛编题名 = subField(raw, 'a')
    lazy val 并列丛编题名 = subField(raw, 'd')
    lazy val 丛编副题名及其它信息 = subField(raw, 'e')
    lazy val 丛编责任者 = subField(raw, 'f')
    lazy val 分册号 = subField(raw, 'h')
    lazy val 分册题名 = subField(raw, 'i')
    lazy val 卷标识 = subField(raw, 'v')
    lazy val ISSN = subField(raw, 'x')
    lazy val 并列丛编题名文种 = language(subField(raw, 'z'))
    lazy val obj = _225(正丛编题名, 并列丛编题名, 丛编副题名及其它信息, 丛编责任者, 分册号,
      分册题名, 卷标识, ISSN, 并列丛编题名文种)

  }

  implicit lazy val _300Fmt = Json.format[_300]

  case class _300(附注内容: String) {
    val json: JsValue = Json.toJson(this)
    val JsonStringFy: String = Json.stringify(json)
  }

  object _300 {
    private lazy val raw = dataField("300")
    lazy val 附注内容 = subField(raw, 'a')
    lazy val obj = _300(附注内容)
  }

  implicit lazy val _606Fmt = Json.format[_606]

  case class _606(主标目: String, 主题复分: String, 地区复分: String, 年代复分: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _606 {
    private lazy val raw = dataField("606")
    lazy val 主标目 = subField(raw, 'a').split(",").flatMap(_.trim.split("-")).distinct.mkString(",")
    lazy val 主题复分 = subField(raw, 'x')
    lazy val 地区复分 = subField(raw, 'y')
    lazy val 年代复分 = subField(raw, 'z')
    lazy val obj = _606(主标目, 主题复分, 地区复分, 年代复分)
  }

  implicit lazy val _690Fmt = Json.format[_690]

  case class _690(中国图书馆图书分类法分类号: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _690 {
    private lazy val raw = dataField("690")
    lazy val 中国图书馆图书分类法分类号 = subField(raw, 'a')
    lazy val obj = _690(中国图书馆图书分类法分类号)
  }

  implicit lazy val _692Fmt = Json.format[_692]

  case class _692(中国科学院图书馆图书分类法分类号: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _692 {
    private lazy val raw = dataField("690")
    lazy val 中国科学院图书馆图书分类法分类号 = subField(raw, 'a')
    lazy val obj = _692(中国科学院图书馆图书分类法分类号)
  }

  implicit lazy val _7XXFmt = Json.format[_7XX]

  case class _7XX(主标目: String, 主标目汉语拼音: String, 人名的其它部分: String, 人名修饰语: String, 罗马数字: String,
                  年代: String, 规范记录号: String, 著作责任: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _7XX {
    private lazy val raw = dataField(Seq("700", "701", "702", "710", "711", "712", "720", "721", "722"))
    lazy val 主标目 = subField(raw, 'a')
    lazy val 主标目汉语拼音 = subField(raw, 'A')
    lazy val 人名的其它部分 = subField(raw, 'b')
    lazy val 人名修饰语 = subField(raw, 'c')
    lazy val 罗马数字 = subField(raw, 'd')
    lazy val 年代 = subField(raw, 'f')
    lazy val 规范记录号 = subField(raw, '3')
    lazy val 著作责任 = subField(raw, '4')
    lazy val obj = _7XX(主标目, 主标目汉语拼音, 人名的其它部分, 人名修饰语, 罗马数字, 年代, 规范记录号, 著作责任)
  }

  implicit lazy val _801Fmt = Json.format[_801]

  case class _801(国家代码: String, 机构名称: String, 处理日期: String, 编目条例代码: String) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json)
  }

  object _801 {
    private lazy val raw = dataField("801")
    lazy val 国家代码 = subField(raw, 'a')
    lazy val 机构名称 = subField(raw, 'b')
    lazy val 处理日期 = subField(raw, 'c')
    lazy val 编目条例代码 = subField(raw, 'g')
    lazy val obj = _801(国家代码, 机构名称, 处理日期, 编目条例代码)
  }


  implicit lazy val bookInfoFmt = Json.format[BookInfo]

  case class BookInfo(ISBN: _010, ISSN: _011, 一般处理数据: _100, 作品语种: _101, 出版国别: _102, 题名与责任者: _200,
                      出版发行: _210, 载体形态: _215, 丛编: _225, 附注: _300, 普通主题: _606,
                      中国图书馆图书分类法分类号: String, 中国科学院图书馆图书分类法分类号: String, 责任者: _7XX, 记录来源: _801, keywords: String, datetime: String = Time.now.toString) {
    val json: JsValue = Json.toJson(this)
    val jsonStringfy: String = Json.stringify(json).replace("，", ",").trim

    def id: String = {
      Security.digest(this.copy(datetime = "").jsonStringfy)
    }


  }

  object BookInfo {
    lazy val obj = BookInfo(_010.obj, _011.obj, _100.obj, _101.obj, _102.obj, _200.obj, _210.obj, _215.obj, _225.obj,
      _300.obj, _606.obj, _690.中国图书馆图书分类法分类号, _692.中国科学院图书馆图书分类法分类号, _7XX.obj, _801.obj,
      s"""${_200.正题名} ${_7XX.主标目}""")
  }

  def pretty = raw.toString
}
