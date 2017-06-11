package com.shrfid.api.services.impl

import javax.inject.{Inject, Singleton}

import com.shrfid.api.Time
import com.shrfid.api.http.DouBanRequest
import com.shrfid.api.services.DouBanService
import com.twitter.finatra.httpclient.{HttpClient, RequestBuilder}
import com.twitter.util.Future



/**
  * Created by Administrator on 2017/3/15.
  */
@Singleton
object DouBanServiceImpl

@Singleton
class DouBanServiceImpl @Inject()(httpClient: HttpClient)extends DouBanService {


  override def findBookByISBNFromDouBan( request: DouBanRequest): Future[(Int, String)] = {

    this.get("/v2/book/isbn/"+request.isbn).map( a =>(200, a) )



  }


  private def get(path: String): Future[String] = {
    val getRequest = RequestBuilder.get(path)
    for {
      response <- httpClient.execute(getRequest)
    } yield {
      this.formatDouBan(response.contentString)
    }
  }

    private def formatDouBan(context: String)={
      import play.api.libs.json.Json
      val json = Json.parse(context)
      val isbn = (json \ "isbn13").asOpt[String].getOrElse("")
      val title = (json \ "title").asOpt[String].getOrElse("")
      val author = (json \ "author").asOpt[Seq[String]].fold("")(a => a.mkString(","))
      val translator = (json \ "translator").asOpt[Seq[String]].fold("")(a => a.mkString(","))
      val largeimage = (json \ "images" \"large").asOpt[String].getOrElse("")
      val mediumimage = (json \ "images" \ "medium").asOpt[String].getOrElse("")
      val smallimage = (json \ "images" \ "small").asOpt[String].getOrElse("")
      val publisher = (json \ "publisher").asOpt[String].getOrElse("")
      val pubdate = (json \ "pubdate").asOpt[String].getOrElse("")
      val keywords = (json \ "keywords").asOpt[String].getOrElse("")
      val clcnumber = (json \ "clcnumber").asOpt[String].getOrElse("")
      val pages = (json \ "pages").asOpt[String].getOrElse("")
      val price = (json \ "price").asOpt[String].getOrElse("")
      val binding = (json \ "binding").asOpt[String].getOrElse("")
      val author_intro = (json \ "author_intro").asOpt[String].getOrElse("")
      val catalog = (json \ "catalog").asOpt[String].getOrElse("").replaceAll("\n","<br>")
      val summary = (json \ "summary").asOpt[String].getOrElse("")
      val probation = (json \ "probation").asOpt[String].getOrElse("")
      val ebook_url = (json \ "ebook_url").asOpt[String].getOrElse("")

      val  now= Time.now.toString

      val temp=s"""{
                    "ISBN": {
                      "ISBN": "$isbn",
                      "装订方式": "",
                      "获得方式和或定价": "$price",
                      "错误的ISBN号": ""
                    },
                    "ISSN": {
                      "ISSN": "",
                      "修饰": "",
                      "获得方式和或定价": "$price",
                      "废除的ISSN": "",
                      "错误的ISSN": ""
                    },
                    "一般处理数据": {
                      "入档日期": "",
                      "出版日期类型": "",
                      "出版年1": "",
                      "出版年2": "",
                      "阅读对象代码": "",
                      "政府出版物代码": "",
                      "修改记录代码": "",
                      "编目语种": "",
                      "音译代码": "",
                      "字符集": ""
                    },
                    "作品语种": {
                      "翻译指示符": "",
                      "作品语种": ""
                    },
                    "出版国别": {
                      "出版国代码": "",
                      "出版地区代码": ""
                    },
                    "题名与责任者": {
                      "正题名": "$title",
                      "正题名汉语拼音": "",
                      "一般资料标识": "",
                      "另一作者的正题名": "",
                      "并列正题名": "",
                      "副题名及其它说明题名的文字": "",
                      "副题名或其他说明题名文字的汉语拼音": "",
                      "第一责任者": "",
                      "第一责任者的汉语拼音": "",
                      "其它责任者": ""
                    },
                    "出版发行": {
                      "出版发行地": "",
                      "出版发行者地址": "",
                      "出版发行者名称": "$publisher",
                      "出版发行日期": "$pubdate",
                      "印刷地": "",
                      "印刷者地址": "",
                      "印刷者": "",
                      "印刷日期": ""
                    },
                    "载体形态": {
                      "页数或卷册数": "$pages",
                      "图及其它细节": "",
                      "尺寸或开本": "$binding",
                      "附件": ""
                    },
                    "丛编": {
                      "正丛编题名": "",
                      "并列丛编题名": "",
                      "丛编副题名及其它信息": "",
                      "丛编责任者": "",
                      "分册号": "",
                      "分册题名": "",
                      "卷标识": "",
                      "ISSN": "",
                      "并列丛编题名文种": ""
                    },
                    "附注": {
                      "附注内容": ""
                    },
                    "普通主题": {
                      "主标目": "",
                      "主题复分": "",
                      "地区复分": "",
                      "年代复分": ""
                    },
                    "中国图书馆图书分类法分类号": "",
                    "中国科学院图书馆图书分类法分类号": "",
                    "责任者": {
                      "主标目": "$author",
                      "主标目汉语拼音": "",
                      "人名的其它部分": "",
                      "人名修饰语": "",
                      "罗马数字": "",
                      "年代": "",
                      "规范记录号": "",
                      "著作责任": ""
                    },
                    "记录来源": {
                      "国家代码": "",
                      "机构名称": "",
                      "处理日期": "",
                      "编目条例代码": ""
                    },
                    "keywords": "$keywords",
                    "datetime": "$now"
                  }"""
      val keyStr = "book_not_found".r
      keyStr.findFirstIn(context)  match {
        case Some(book_not_found) => context
        case default => temp
      }
    }




  def isISBN(_isbn: Option[String]): Boolean = {
    val isbnRegex = "^[0-9]+$".r
    _isbn match {
      case None => false
      case Some(_isbn) => {
        val isbnStr = _isbn.replaceAll("-", "")
        val frontStr = isbnStr.substring(0, isbnStr.length() - 1)
        val backStr = isbnStr.substring(isbnStr.length() - 1)
        frontStr match {
          case a if (!isbnRegex.pattern.matcher(a).matches| (!(a.length() == 9| a.length() == 12))) => false
          case c if (c.length() == 9 && isbnRegex.pattern.matcher(c).matches) => {
            //第四组号码是校验码，其数值由前九位数字依次以10～2加权之和并以11为模计算得到。
            //假设某国际标准书号号码前9位是：7-309-04547；
            // 计算加权和S：S =7×10+3×9+0×8+9×7+0×6+4×5+5×4+4×3+7×2=226；
            // 计算S÷11的余数M：M=226 mod 11=6；计算11-M的差N：N=11−6=5如果N=10，校验码是字母“X”；
            // 如果N=11，校验码是数字“0”；如果N为其他数字，校验码是数字N。
            // 所以，本书的校验码是5，故该国际标准书号为ISBN 7-309-04547-5。
            val sum = frontStr.zipWithIndex.map(
              a => a._1.toString.toInt * (10 - a._2)
            ).sum
            val n = 11 - sum % 11
            val s = n match {
              case 11 => "0"
              case 10 => "x"
              case _ => "" + n
            }
            if (backStr.toLowerCase.equals(s)) true else false
          }
          case d if (d.length() == 12 && isbnRegex.pattern.matcher(d).matches) => {
            //用1分别乘ISBN的前12位中的奇数位（从左边开始数起），用3乘以偶数位，乘积之和以10为模，
            // 10与模值的差值再对10取模（即取个位的数字）即可得到校验位的值，
            // 其值范围应该为0~9。
            //计算和S：S =9*1+7*3+8*1+7*3+1*1+1*3+5*1+4*3+1*1+7*3+3*1+0*3=105；
            val frist_3= frontStr.substring(0, 3)

            if (!(frist_3.equals("978")||frist_3.equals("979")))
              return  false

            val sum = frontStr.zipWithIndex.map(
              a => a match {
                case a if (a._2 % 2 == 0) => a._1.toString.toInt
                case _ => a._1.toString.toInt * 3
              }
            ).sum
            val n = "" + (10 - sum % 10) % 10
            if (backStr.equals(n)) true else false
          }
          case _ => false
        }
      }
    }
  }





}


