package com.shrfid.api.services.impl

import javax.inject.{Inject, Singleton}

import com.shrfid.api.{Docs, NotFound, StatusCode}
import com.shrfid.api.http.Elastic.book.reference.GetBookCatalogingByISBNRequest
import com.shrfid.api.services.DouBanService
import com.twitter.finagle.http.Status
import com.twitter.finatra.httpclient.{HttpClient, RequestBuilder}
import com.twitter.finatra.utils.FuturePools
import com.twitter.util.Future

/**
  * Created by jiejin on 9/11/16.
  */
@Singleton
object DouBanServiceImpl
@Singleton
class DouBanServiceImpl @Inject()(httpClient: HttpClient)extends DouBanService {


  override def findBookCatalogingByISBN( request: GetBookCatalogingByISBNRequest): Future[(Int, String)] = {


    this.get("/v2/book/isbn/"+request.isbn).map( a =>(200, a) )
  }


  private def get(path: String): Future[String] = {
    val getRequest = RequestBuilder.get(path)
    for {
      response <- httpClient.execute(getRequest)
    } yield {
      response.contentString
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
          case a if (!isbnRegex.pattern.matcher(a).matches || (!(a.length() == 9 || a.length() == 12))) => false
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

