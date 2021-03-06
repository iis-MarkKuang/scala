package com.shrfid

import java.security.MessageDigest

import com.google.common.base.CaseFormat
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, Duration}
/**
  * Created by jiejin on 6/09/2016.
  */
package object api {

  import TwitterConverters._
  import com.twitter.{util => twitter}

  import language.implicitConversions
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.{ExecutionContext, Future, Promise}
  import scala.util.{Failure, Success, Try}

  type StatusCode = Int
  type Count = Int
  type Docs = String
  type UpsertResponse = (StatusCode, Docs)
  type GetResponse = (StatusCode, Docs)
  type DeleteResponse = (StatusCode, Docs)
  type SearchResponse = (StatusCode, Docs)
  type IndexResponse = (StatusCode, Docs)
  type SearchDocsResponse = (StatusCode, (Count, Docs))
  type UpdateResponse = (StatusCode, Docs)

  type Id = String
  type Username = String

  val Delimiter = ","

  val testFolder = "ftp://localhost"

  val EmptyListResponse = """{"count":0, "prev": "", "next": "", "content": []}"""
  val UnauthorizedResponse = """{"errors":["You don't know the secret"]}"""
  val NoPermissionResponse = """{"errors":["You don't have the permission"]}"""
  val RecordNotFoundResponse = """{"errors":["There is no record can be found by specified parameters"]}"""
  val CreatedResponse = """{"created" : true}"""
  val NotCreatedResponse = """{"created" : false}"""
  val AcceptedResponse = """{"accepted" : true}"""
  val NotAcceptableResponse = """{"accepted" : false}"""
  val NotUpdatedResponse = """{"updated" : false}"""
  val UpdatedResponse = """{"updated" : true}"""
  val NotDeletedResponse = """{"deleted" : false}"""
  val DeletedResponse = """{"deleted" : true}"""
  val BadRequestResponse = (message: String ) => s"""{"errors":["${message}"]}"""

  // Added by kuangyuan 5/9/2017
  val InactiveUserResponse = """{"errors":["You are marked inactive and cannot login, please contact system administrator"]}"""

  val EmptyList = (0, Seq())
  val NotFound = "NotFound"
  val Empty = ""
  val InvalidToken = "InvalidToken"
  val NoPermission = "NoPermission"
  val UpdateFailed = "UpdateFailed"

  // Added by kuang yuan 05/01/2017
  val OldPasswordNotMatch = "Old password does not match, please input again"

  // Added by kuang yuan 05/02/2017
  val AuthGroupNotEmpty = "该权限已有用户关联，无法删除！"

  val UpdateSucceed = "UpdateSucceed"
  val DeleteFailed = "DeleteFailed"
  val DeleteSucceed = "DeleteSucceed"
  val CreateFailed = "CreateFailed"
  val CreateSucceed = "CreateSucceed"

  val defaultBookPrice = 50.00 //all 50 yuan
  val defaultInterval = "week"
  val defaultFlowDataLimit = "10"
  val defaultFlowDataMinDocCount = "3"

  // Added by kuang yuan 5/7/2017
  // elasticsearch search limit
  val elasticSearchLimit = 10000

  def boolean(i: Int) = i.!=(0)

  def boolean(i: Option[Int]) = i match {
    case None => None
    case Some(i) => Some(i.!=(0))
  }

  def optionStr(str: String) = {
    str match {
      case "" => None
      case s => Some(s)
    }
  }

  def optionStrToStr(str: Option[String]) = {
    str match {
      case Some(s) => s
      case _ => ""
    }
  }


  def optionStrToSeq[T](str: Option[String], delimiter: String = ",") = str match {
    case Some(s) => Some(s.split(delimiter).map(_.asInstanceOf[T]).toSeq)
    case None => None
  }

  def optionStrToSeqInt(str: Option[String], delimiter: String = ",") = str match {
    case Some(s) => Some(s.split(delimiter).map(_.toInt).toSeq)
    case None => None
  }


  def itemCategory(i: Int) = i match {
    case 1 => "普通图书"
    case 2 => "期刊"
    case 3 => "古籍"
    case 4 => "非书资料"
  }


  def today = new java.sql.Date(new java.util.Date().getTime)

  object Time {
    def now = DateTime.now(DateTimeZone.forOffsetHours(+8))

    def endOfDay = now.toLocalDate.plusDays(1).toDateTimeAtStartOfDay.minus(1000)

    def startOfDay = now.withTimeAtStartOfDay()

    def format(dateTime: DateTime) = dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))

    def nextNdays(days: Int) = now.toLocalDate.plusDays(days).toDateTimeAtStartOfDay.minus(1000)

    def daysFromNow(dat:String)=new Duration(new DateTime(dat), now).getStandardDays;

  }

  def isInt(str: String): Boolean = str match {
    case "" => false
    case s => s.forall(_.isDigit)
  }

  def valueJson(key: String, value: Any): String = {
    s"""{"${key}": "${value}"}"""
  }

  def valuesJson(keys: Seq[String], values: Seq[Any], isStr: Seq[Boolean] = Nil): String = {
    isStr match {
      case Nil =>
        s"""{${(keys, values).zipped.map(
          (a, b) =>
              s""""${a}": "${b}""""
        ).mkString(",")}}
        """.stripMargin
      case _ =>
        s"""{${(keys, values, isStr).zipped.map(
          (a, b, c) => c match {
            case true =>
              s""""${a}": "${b}""""
            case false =>
              s""""${a}": ${b}"""
          }
        ).mkString(",")}}
        """.stripMargin
    }
  }

  def valueJsonArrayObject(key: String, value: Any): String = {
    s"""{"${key}": ${value}}"""
  }

  object TwitterFutureOps {

    implicit class ScalaToTwitterFuture[T](f: Future[T]) {
      def toTwitterFuture: twitter.Future[T] = f
    }

    implicit class TwitterToScalaFuture[T](f: twitter.Future[T]) {
      def toScalaFuture: Future[T] = f
    }

    implicit class TwitterFutureFlatten[T](f: twitter.Future[twitter.Future[T]]) {
      def flatten(): twitter.Future[T] = f.flatMap(x => x)
    }

  }

  object TwitterConverters {
    implicit def scalaToTwitterTry[T](t: Try[T]): twitter.Try[T] = t match {
      case Success(r) => twitter.Return(r)
      case Failure(ex) => twitter.Throw(ex)
    }

    implicit def twitterToScalaTry[T](t: twitter.Try[T]): Try[T] = t match {
      case twitter.Return(r) => Success(r)
      case twitter.Throw(ex) => Failure(ex)
    }

    implicit def scalaToTwitterFuture[T](f: Future[T])(implicit ec: ExecutionContext): twitter.Future[T] = {
      val promise = twitter.Promise[T]()
      f.onComplete(promise update _)
      promise
    }

    implicit def twitterToScalaFuture[T](f: twitter.Future[T]): Future[T] = {
      val promise = Promise[T]()
      f.respond(promise complete _)
      promise.future
    }


  }

  def token(authorization: String) = authorization.substring(7)

// Modified by kuang 5/25/2017
  def barcodeFmt(i: Int, categoryId: Int) = f"$categoryId${i}%010d"
  def barcodeDeFmt(s: String, categoryId: Int) = s.replaceFirst(categoryId.toString, "").toInt

//  def barcodeFmt(i: Int, categoryId: Int) = f"${i}%08d"
//  def barcodeDeFmt(s: String, categoryId: Int) = s.toInt

  def vendorOrderFmt(i: Int) = f"VO${DateTime.now().toString("yyyyMMdd")}${i}%010d"
  def vendorOrderDeFmt(s: String) = s.replaceFirst(s"VO${DateTime.now().toString("yyyyMMdd")}", "").toInt

// Modified by kuangyuan 5/26/2017
  def bookCategory(i: Int) = i match {
    case 1 => "普通图书"
    case 2 => "期刊"
    case 3 => "古籍"
    case 4 => "非书资料"
    case 5 => "期刊合订本"
  }

// Modified by kuangyuan 5/26/2017
  def bookCategoryId(s: String) = s match {
    case "普通图书" => 1
    case "期刊" => 2
    case "古籍" => 3
    case "非书资料" => 4
    case "期刊合订本" => 5
  }

  object Security {
    def digest(s: String) = MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }

  def snakecase(s: String) = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s)

  def getCCParams(cc: AnyRef, filter: Seq[String] = Seq()) = {

    (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
      f.setAccessible(true)
      filter match {
        case Seq() =>
          f.get(cc) match {
            case Some(s) => a + (snakecase(f.getName) -> s)
            case None => a
            case o => a + (snakecase(f.getName) -> o)
          }
        case other =>
          (!filter.contains(f.getName), f.get(cc)) match {
            case (false, _) => a
            case (_, None) => a
            case (true, Some(s)) => a + (snakecase(f.getName) -> s)

            case (true, o) => a + (snakecase(f.getName) -> o)
          }
      }

    }
  }

}
