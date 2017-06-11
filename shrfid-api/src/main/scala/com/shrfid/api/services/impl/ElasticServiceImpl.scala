package com.shrfid.api.services.impl

import java.io.IOException
import java.net.SocketException
import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book._
import com.shrfid.api.domains.reader._
import com.shrfid.api.domains.vendor.{VendorMember, VendorMemberWithId, VendorOrder, VendorOrderNested}
import com.shrfid.api.http.Elastic.book.{PatchBookItemsActiveStatusRequest, PatchBookItemsStackIdRequest}
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.book.reservation.GetBookReservationListRequest
import com.shrfid.api.http.Elastic.book.solicitedPeriodicals._
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.http.Elastic.data._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import com.shrfid.api.persistence.elastic4s._
import com.shrfid.api.services.{ElasticService, RedisService, TokenService}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.searches.QueryDefinition
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.joda.time.{DateTime, Days}
import play.api.libs.json.{JsObject, JsString, Json}
import org.apache.commons.net.ftp._

import scala.math.BigDecimal.RoundingMode

/**
  * Created by jiejin on 9/11/16.
  */
@Singleton
object ElasticServiceImpl

@Singleton
class ElasticServiceImpl @Inject()(bookBranchRepo: BookBranchRepo,
                                   bookStackRepo: BookStackRepo,
                                   bookItemRepo: BookItemRepo,
                                   bookReferenceRepo: BookReferenceRepo,
                                   vendorMemberRepo: VendorMemberRepo,
                                   vendorOrderRepo: VendorOrderRepo,
                                   readerLevelRepo: ReaderLevelRepo,
                                   readerLevelNewRepo: ReaderLevelNewRepo,
                                   readerGroupRepo: ReaderGroupRepo,
                                   readerMemberRepo: ReaderMemberRepo,
                                   borrowHistoryRepo: BorrowHistoryRepo,
                                   reservationHistoryRepo: ReservationHistoryRepo,

                                   solicitedRepo: SolicitedRepo,

                                  // Have some major doubt of redis end (synchronization?)
                                   redisService: RedisService,
                                   tokenService: TokenService) extends ElasticService {


  override def updateBookInfoItem(id: String, item: String, oldId: Option[String]) = {
    for {
      _ <- Future(oldId) flatMap {
        case Some(o) => bookItemRepo.dal.delete(o, item).toTwitterFuture
        case None => Future.Unit
      }
      r <- bookItemRepo.dal.insert(id, item).toTwitterFuture.map(_.status.getStatus.toString)
    } yield r
  }


  private def bulkResponse(response: Seq[(StatusCode, Docs)]) = {
    (Status.Ok.code, s"""[${response.map(e => e._2).mkString(",")}]""")
  }

  // branch
  // c
  // Modified by kuangyuan 2017/05/18
  override def insertBookBranch(user: Username, request: PostBookBranchRequest): Future[UpsertResponse] = {
    for {
      result <- bookBranchRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          bookBranch <- BookBranch.toDomain(request)
          id <- bookBranchRepo.dal.id(bookBranch)
          result <- bookBranchRepo.dal.upsertDoc(id, bookBranch.jsonStringify) flatMap {
            case a if a._1 == Status.Created.code =>
              bookItemRepo.dal.createBranchBookIndex(s"book_${id}", 5, 1) flatMap {
                case b if b.isShardsAcked => Future.value(a)
                case err => Future.value((a._1, valuesJson(Seq("insert_result", "create_index_result"), Seq(a._2, "error creating index"))))
              }
            case b => Future.value(b)
          }
        } yield result
        case false => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }

  // r
  override def findBookBranches(request: GetBookBranchListRequest): Future[SearchResponse] = {
    bookBranchRepo.dal.findAllDocs(request)
  }

  override def findBookBranchById(user: Username, request: GetBookBranchByIdRequest): Future[SearchResponse] = {
    bookBranchRepo.dal.findById(request.id)
  }

  // u
  override def updateBookBranchById(user: Username, request: PatchBookBranchByIdRequest): Future[UpdateResponse] = {

    request.name match {
      case Some(name) =>
        for {
          result <- bookBranchRepo.dal.isUnique("name", name, request.id) flatMap {
            case true => bookBranchRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse(request.id, s"name: '${name}' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => bookBranchRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  // d
  override def deleteBookBranchById(user: Username, request: DeleteBookBranchByIdRequest): Future[DeleteResponse] = {
    bookStackRepo.dal.findActiveStacksUnderBranchId(request.id) flatMap {
      case a if a._1 == 200 => Future((Status.NotAcceptable.code, valueJson("error", "The book branch to be deleted has stacks under it, to delete the branch, you need to delete stacks first")))
      case _ => bookBranchRepo.dal.deleteById(request.id)
    }
  }

  override def deleteBookBranchBulk(user: Username, request: DeleteBookBranchBulkRequest): Future[DeleteResponse] = {
    Future.collect(request.ids.map(
      id => bookStackRepo.dal.findActiveStacksUnderBranchId(id) flatMap {
        case a if a._1 == 200 => Future(id)
        case _ => Future(None)
      }
    )) flatMap {
      case all => all.filter(_ != None) match {
        case empty_arr if empty_arr.length == 0 => bookBranchRepo.dal.deleteBulk(request.ids)
        case arr => Future((Status.NotAcceptable.code, valueJsonArrayObject("branches that cannot be deleted", "[" + arr.map(ele => '"' + ele.toString + '"').mkString(",") + "]")))
      }
    }
  }

  // stack
  // Modified by kuang yuan 5/16/2017
  override def insertBookStack(user: Username, request: PostBookStackRequest): Future[UpsertResponse] = {
    Future.collect(Seq(bookStackRepo.dal.isUniqueWithConditions("name", request.name, "randomid", Seq(termQuery("branch_id", request.branchId))), bookBranchRepo.dal.isExists(request.branchId))) flatMap {
      case Seq(true, true) =>
        for {
          bookStack <- BookStack.toDomain(request)
          id <- bookStackRepo.dal.id(bookStack)
          result <- bookStackRepo.dal.upsertDoc(id, bookStack.jsonStringify)
        } yield result
      case Seq(false, true) => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      case Seq(true, false) => Future.value(insertResponse("", s"branch_id: '${request.branchId}' does not exist", false, Status.NotAcceptable.code))
      case Seq(false, false) => Future.value(insertResponse("", s"name: '${request.name}' exists, branch_id: '${request.branchId}' does not exist", false, Status.NotAcceptable.code))
    }
  }

  override def findBookStacks(request: GetBookStackListRequest): Future[SearchResponse] = {
    for {
      stacks <- bookStackRepo.dal.findAll(request)
      ids = stacks.hits.map(b => b.sourceAsMap.getOrElse("branch", "").toString).toSeq.filterNot(_ == "").distinct
      branches <- bookBranchRepo.dal.findByIds(ids).map(_.hits.map(b => BookBranchWithId.toDomain(b.id, Json.parse(b.sourceAsString).as[BookBranch])).toList.groupBy(_.id).map(a => (a._1, a._2.head)))
      temp = stacks.hits.map(a => BookStackNested.toDomain(a.id, Json.parse(a.sourceAsString).as[BookStack], branches)).toSeq
    } yield (Status.Ok.code, request.response(stacks.totalHits.toInt, Json.stringify(Json.toJson(temp))))
  }


  override def findBookStackById(user: Username, request: GetBookStackByIdRequest): Future[SearchResponse] = {
    bookStackRepo.dal.findByIdRich(request.id).map(a => a.exists match {
      case true => (Status.Ok.code, a.sourceAsString.replaceFirst("\\{", s"""{ "id":"${a.id}", """))
      case false => (Status.NotFound.code, NotFound)
    })
  }

  // modified by kuang 5/16/2017
  override def updateBookStackById(user: Username, request: PatchBookStackByIdRequest): Future[UpsertResponse] = {
    //println(request.patchRequest)
    (request.name, request.branchId) match {
      case (Some(name), Some(branchId)) =>
        Future.collect(Seq(bookStackRepo.dal.isUniqueWithConditions("name", name, request.id, Seq(termQuery("branch_id", branchId))), bookBranchRepo.dal.isExists(branchId))) flatMap {
          case Seq(true, true) => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
          case Seq(false, true) => Future.value(updateResponse(request.id, s"name: '${name}' exists under the same branch", false, Status.NotAcceptable.code))
          case Seq(true, false) => Future.value(updateResponse(request.id, s"branch_id: '${branchId}' does not exist", false, Status.NotAcceptable.code))
          case Seq(false, false) => Future.value(updateResponse(request.id, s"""name: '${name}' exists under the same branch, branch_id: '${branchId}' does not exist""", false, Status.NotAcceptable.code))
        }
      case (Some(name), None) =>
        bookStackRepo.dal.findById(request.id) flatMap {
          case a => bookStackRepo.dal.isUniqueWithConditions("name", name, request.id, Seq(termQuery("branch_id", Json.parse(a._2).as[BookStack].branch))) flatMap {
            case true => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse(request.id, s"name: '${name}' exists", false, Status.NotAcceptable.code))
          }
        }
      case (None, Some(branchId)) =>
        bookBranchRepo.dal.isExists(branchId) flatMap {
          case true => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
          case false => Future.value(updateResponse(request.id, s"branch_id: '${branchId}' does not exist", false, Status.NotAcceptable.code))
        }
      case (None, None) => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  override def deleteBookStackById(user: Username, request: DeleteBookStackByIdRequest): Future[DeleteResponse] = {
    bookItemRepo.dal.findActiveBooksUnderStackId(request.id) flatMap {
      case a if a._1 == 200 => Future((Status.NotAcceptable.code, valueJson("error", "Stack has books under it, cannot be deleted")))
      case _ => bookStackRepo.dal.deleteById(request.id)
    }
  }

  override def deleteBookStackBulk(user: Username, request: DeleteBookStackBulkRequest): Future[(StatusCode, Docs)] = {
    Future.collect(request.ids.map(id =>
      bookItemRepo.dal.findActiveBooksUnderStackId(id) flatMap {
        case a if a._1 == 200 => Future(id)
        case _ => Future(None)
      }
    )) flatMap {
      case all => all.filter(_ != None) match {
        case empty_arr if empty_arr.length == 0 => bookStackRepo.dal.deleteBulk(request.ids)
        case arr => Future((Status.NotAcceptable.code, valueJsonArrayObject("stacks_with_active_books", "[" + arr.map(ele => '"' + ele.toString + '"').mkString(",") + "]")))
      }
    }
  }

  // reference
  override def insertReference(_id: String, _source: String): Future[UpsertResponse] = {
    bookReferenceRepo.dal.upsertDoc(_id, _source)
  }

  override def findReferenceById(_id: String): Future[GetResponse] = {
    bookReferenceRepo.dal.findById(_id) flatMap {
      case a if a._1 == Status.Ok.code =>
        val ref = Json.parse(a._2).as[JsObject]
        bookItemRepo.dal.findByRefId(_id) flatMap {
          case r if r._1 == Status.Ok.code =>
            val info = Json.parse(r._2).as[Book]
            val refInner = ref + ("barcode",Json.toJson(info.barcode))
            Future.value((Status.Ok.code, refInner.toString()))
          case _ => Future.value((Status.Ok.code, ref.toString()))
        }
      case b => Future.value(b)
    }
  }

  override def findReference(request: GetBookReferenceListRequest): Future[SearchResponse] = {
    bookReferenceRepo.dal.findAllDocs(request)
  }

  // book item
  override def preGenBookItems(user: Username, request: PostPreGenBookItemsRequest): Future[(StatusCode, String)] = {
    val result = for {
      maxBarcode <- bookItemRepo.dal.findMaxBarcode(bookCategory(request.categoryId)).onSuccess(a => println(a))
      countForThisCategory = barcodeDeFmt(maxBarcode, request.categoryId)
      start = countForThisCategory + 1
      end = countForThisCategory + request.quantity
      items = (start to end).toList.map(i => Book(barcode = barcodeFmt(i, request.categoryId), category = bookCategory(request.categoryId), stack = request.stackId))
      response <- Future.collect(items.map(i => bookItemRepo.dal.upsertDoc(i.barcode, i.jsonStringify)))
    } yield (response, start, end) match {
      case (a, _, _) => a match {
        case error if error.map(e => e._1).exists(i => i != 201) => (Status.NotAcceptable.code, NotCreatedResponse)
        case other => (Status.Created.code, request.response(barcodeFmt(start, request.categoryId), barcodeFmt(end, request.categoryId)))
      }
    }
    result
  }

  // Modified by kuangyuan 5/10/2017
  // Modified by kuangyuan 5/25/2017
  private def catalogue(request: PostBookItemsRequest, reference: String, title: Option[String], clc: Option[String], user: Username): Future[UpsertResponse] = {

    val titleFixed = title match {
      case Some(t) => t
      case None => request.info.get.题名与责任者.正题名
    }
    val clcFixed = clc match {
      case Some(c) => c.take(Config.clcLength)
      case None => request.info.get.中国图书馆图书分类法分类号//.take(Config.clcLength).replaceAll("[^a-zA-Z0-9]", "")
    }

    def insertBookItem: Future[UpsertResponse] =
      for {
        bookIndex <- bookItemRepo.dal.findNextBookIndex(clcFixed)

        //      result <- bookItemRepo.dal.findByRefId(reference) flatMap {
        //        case r if r._1 == Status.Ok.code && (Json.parse(r._2) \ "barcode").toOption.getOrElse("none").toString.stripPrefix("\"").stripSuffix("\"") != request.barcode =>
        //          Future.value((Status.NotAcceptable.code, valueJson("error", s"This reference is mapped to another book ${(Json.parse(r._2) \ "barcode").toOption.getOrElse("none").toString.stripPrefix("\"").stripSuffix("\"")}.")))
        //
        //        case _ =>
        result <- Future.collect(request.barcodeString.split("[,;]").map(barcode =>
          bookItemRepo.dal.catalogue(barcode, request.rfid.getOrElse(""), reference, titleFixed, request.stackId, clcFixed, bookIndex, user, request.solicitedPeriodical, request.serialNumber)
        )).map(bulkResponse)
      //      }

      } yield result


    request.categoryId match {
      case 2 => solicitedRepo.dal.findLastSolicitedSerial(request.solicitedPeriodical) flatMap {
        case n if n._1 == Status.NotFound.code => Future.value((Status.NotAcceptable.code, valueJson("error", "Periodical not found")))
        case y if y._2.toInt + 1 == request.serialNumber =>
          Future.collect(Seq(solicitedRepo.dal.updateDoc(request.solicitedPeriodical, Map("last_serial" -> request.serialNumber))
          ,insertBookItem)) flatMap {
            case Seq(a, b) if b._1 == Status.Ok.code => Future.value((Status.Ok.code, valuesJson(Seq("solicit_update_result", "insert_result"), Seq(a._2, b._2))))
            case Seq(c, d) => Future.value((d._1, valuesJson(Seq("solicit_update_result", "insert_result"), Seq(c._2, d._2))))
          }
        case _ => Future.value((Status.NotAcceptable.code, valueJson("error", "serial number not right after the last serial of the periodical")))
      }
      case _ => insertBookItem
    }



  }

  override def catalogue(user: Username, request: PostBookItemsRequest): Future[UpsertResponse] = {
    catalogue_handle(user, request)
  }

  // is it ok if every time we update a reference we insert a new one?
  def catalogue_handle(user: Username, request: PostBookItemsRequest): Future[UpsertResponse] = {
    def findRefTitleClc = request.reference match {
      case Some(ref) if ref != "" => for {(t, c) <- bookReferenceRepo.dal.findTitleById(ref)} yield (ref, t, c)
      case _ =>
        // TODO problem with catalogue
//        //test
//      val test_str =
//        s"""
//           |{
//           |      "ISBN": "1241612412",
//           |      "装订方式": "124123123",
//           |      "获得方式和或定价": "22",
//           |      "错误的ISBN号": "1234709821"
//           |}
//           """.stripMargin
//        val zoz = Json.parse(test_str).as[_010]
        val bookRef = BookReference.withKeyword(request.info.get)

        val rr = bookRef.题名与责任者

        val _id = bookRef.id
        insertReference(_id, Json.stringify(Json.toJson(bookRef))).map(e => (_id, None, None))
    }
    for {
      (ref, title, clc) <- findRefTitleClc
      result <- catalogue(request, ref, title, clc, user)
    } yield result
  }

  override def findBookItems(user: Username, request: GetBookItemListRequest): Future[SearchResponse] = {
    for {
      books <- bookItemRepo.dal.findAll(request)
      temp = books.hits.map(b => BookWithId.toDomain(b.id, Json.parse(b.sourceAsString).as[Book]))
    } yield (Status.Ok.code, request.response(books.totalHits.toInt, Json.stringify(Json.toJson(temp))))
  }

  override def findBookItemById(user: Username, request: GetBookItemByIdRequest): Future[SearchResponse] = {
    bookItemRepo.dal.findById(request.id)
  }

  // vendor member
  override def insertVendorMember(user: Username, request: PostVendorMemberRequest): Future[UpsertResponse] = {
    for {
      result <- vendorMemberRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          vendorMember <- VendorMember.toDomain(request)
          id <- vendorMemberRepo.dal.id(vendorMember)
          result <- vendorMemberRepo.dal.upsertDoc(id, vendorMember.jsonStringify)
        } yield result
        case false => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }

  override def updateVendorMemberById(user: Username, request: PatchVendorMemberByIdRequest): Future[UpdateResponse] = {
    request.name match {
      case Some(name) =>
        for {
          result <- vendorMemberRepo.dal.isUnique("name", name, request.id) flatMap {
            case true => vendorMemberRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse("", s"name: '$name' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => vendorMemberRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  override def findVendorMembers(user: Username, request: GetVendorMemberListRequest): Future[SearchResponse] = {
    vendorMemberRepo.dal.findAllDocs(request)
  }

  override def findVendorMemberById(user: Username, request: GetVendorMemberByIdRequest): Future[SearchResponse] = {
    vendorMemberRepo.dal.findById(request.id)
  }

  override def deleteVendorMemberById(user: Username, request: DeleteVendorMemberByIdRequest): Future[DeleteResponse] = {
    vendorMemberRepo.dal.deleteById(request.id)
  }

  override def deleteVendorMemberBulk(user: Username, request: DeleteVendorMemberBulkRequest): Future[DeleteResponse] = {
    vendorMemberRepo.dal.deleteBulk(request.ids)
  }


  // vender order
  override def insertVendorOrder(user: Username, request: PostVendorOrderRequest): Future[UpsertResponse] = {
    vendorMemberRepo.dal.findByIdRich(request.vendorId) flatMap {
      case a => (a.exists, a.sourceAsMap.getOrElse("is_active", false)) match {
        case (true, true) => for {
          order <- VendorOrder.toDomain(request, user)
          result <- vendorOrderRepo.dal.insertDoc(order.jsonStringify)
        } yield result
        case (false, _) => Future.value(insertResponse("", s"vendor_id: '${request.vendorId}' does not exist", false, Status.NotAcceptable.code))
        case (true, false) => Future.value(insertResponse("", s"vendor_id: '${request.vendorId}' should be active", false, Status.NotAcceptable.code))

        // Added by kuangyuan 5/5 to match all possibilities
        case (true, _) => Future.value(insertResponse("", s"vendor_id: '${request.vendorId}' status value is abnormal", false, Status.NotAcceptable.code))
      }
    }
  }

  override def insertVendorOrderBulk(user: Username, request: PostVendorOrderBulkRequest): Future[UpsertResponse] = {
    for {
      orders <- VendorOrder.toDomain(request, user)
      response <- Future.collect(orders.map(i =>
        vendorMemberRepo.dal.findByIdRich(i.vendor) flatMap {
          case a => (a.exists, a.sourceAsMap.getOrElse("is_active", false)) match {
            case (true, true) =>
              vendorOrderRepo.dal.insertDoc(i.jsonStringify)
            case (false, _) => Future.value(insertResponse("", s"vendor_id: '${i.vendor}' does not exist", false, Status.NotAcceptable.code))
            case (true, false) => Future.value(insertResponse("", s"vendor_id: '${i.vendor}' should be active", false, Status.NotAcceptable.code))

            // Added by kuangyuan 5/5 to match all possibilities
            case (true, _) => Future.value(insertResponse("", s"vendor_id: '${i.vendor}' status value is abnormal", false, Status.NotAcceptable.code))
          }
        }))
    } yield (Status.Ok.code, s"""[${response.map(e => e._2).mkString(",")}]""")
  }

  override def updateVendorOrders(user: Username, request: PatchVendorOrderBulkRequest): Future[UpdateResponse] = {
    Future.collect(request.data.map(i => vendorOrderRepo.dal.updateDoc(i.id, i.patchRequest))).map(response => (Status.Ok.code, s"""[${response.map(e => e._2).mkString(",")}]"""))
  }

  override def findVendorOrders(user: Username, request: GetVendorOrderListRequest): Future[SearchResponse] = {
    for {
      orders <- vendorOrderRepo.dal.findAll(request)
      ids = orders.hits.map(b => b.sourceAsMap.getOrElse("vendor", "").toString).toSeq.filterNot(_ == "").distinct
      members <- vendorMemberRepo.dal.findByIds(ids).map(_.hits.map(b => VendorMemberWithId.toDomain(b.id, Json.parse(b.sourceAsString).as[VendorMember])).toList)
      temp = orders.hits.map(a => VendorOrderNested.toDomain(a.id, Json.parse(a.sourceAsString).as[VendorOrder], members)).toSeq
    } yield (Status.Ok.code, request.response(orders.totalHits.toInt, Json.stringify(Json.toJson(temp))))


  }

  override def findVendorOrderById(user: Username, request: GetVendorOrderByIdRequest): Future[SearchResponse] = {
    vendorOrderRepo.dal.findById(request.id)
  }

  override def deleteVendorOrderById(user: Username, request: DeleteVendorOrderByIdRequest): Future[DeleteResponse] = {
    vendorOrderRepo.dal.deleteById(request.id)
  }

  override def deleteVendorOrderBulk(user: Username, request: DeleteVendorOrderBulkRequest): Future[DeleteResponse] = {
    vendorOrderRepo.dal.deleteBulk(request.ids)
  }

  // reader level
  override def insertReaderLevel(user: Username, request: PostReaderLevelRequest): Future[UpsertResponse] = {
    for {
      result <- readerLevelRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          readerLevel <- ReaderLevel.toDomain(request)
          id <- readerLevelRepo.dal.id(readerLevel)
          result <- readerLevelRepo.dal.upsertDoc(id, readerLevel.jsonStringify)
        } yield result
        case false => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }

  override def findReaderLevels(user: Username, request: GetReaderLevelListRequest): Future[SearchResponse] = {
    readerLevelRepo.dal.findAllDocs(request)
  }

  override def findReaderLevelById(user: Username, request: GetReaderLevelByIdRequest): Future[SearchResponse] = {
    readerLevelRepo.dal.findById(request.id)
  }

  override def updateReaderLevelById(user: Username, request: PatchReaderLevelByIdRequest): Future[UpdateResponse] = {

    request.name match {
      case Some(name) =>
        for {
          result <- readerLevelRepo.dal.isUnique("name", name, request.id) flatMap {
            case true => readerLevelRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse("", s"name: '${name}' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => readerLevelRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  override def deleteReaderLevelById(user: Username, request: DeleteReaderLevelByIdRequest): Future[DeleteResponse] = {
    // Modified by kuang yuan 5/2/2017

    readerMemberRepo.dal.findIdsByLevelIds(Seq(request.id)) flatMap {
      case a if !a.isEmpty =>
        Future((Status.NotAcceptable.code, valueJson("error", "该类别已有读者关联，无法删除")))
      case _ => readerLevelRepo.dal.deleteById(request.id)
    }
  }

  override def deleteReaderLevelBulk(user: Username, request: DeleteReaderLevelBulkRequest): Future[DeleteResponse] = {
    // Modified by kuang yuan 5/2/2017

    Future.collect(request.ids.map(id =>
      readerMemberRepo.dal.findReaderMembersByLevelId(id) flatMap {
        case a if a._1 == 200 => Future(id)
        case _ => Future(None)
      }
    )) flatMap {
      case all => all.filter(_ != None) match {
        case empty_array if empty_array.length == 0 => readerLevelRepo.dal.deleteBulk(request.ids)
        case arr => Future(Status.NotAcceptable.code, valueJsonArrayObject("无法删除的读者类别", "[" + arr.map(ele => '"' + ele.toString + '"').mkString(",") + "]"))
      }
    }
  }

  // reader group
  override def insertReaderGroup(user: Username, request: PostReaderGroupRequest): Future[UpsertResponse] = {
    for {
      result <- readerGroupRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          readerGroup <- ReaderGroup.toDomain(request)
          id <- readerGroupRepo.dal.id(readerGroup)
          result <- readerGroupRepo.dal.upsertDoc(id, readerGroup.jsonStringify)
        } yield result
        case false => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }

  override def findReaderGroups(user: Username, request: GetReaderGroupListRequest): Future[SearchResponse] = {
    readerGroupRepo.dal.findAllDocs(request)
  }

  override def findReaderGroupById(user: Username, request: GetReaderGroupByIdRequest): Future[SearchResponse] = {
    readerGroupRepo.dal.findById(request.id)
  }

  override def updateReaderGroup(user: Username, request: PatchReaderGroupByIdRequest): Future[UpdateResponse] = {
    request.name match {
      case Some(name) =>
        for {
          result <- readerGroupRepo.dal.isUnique("name", name, request.id) flatMap {
            case true => readerGroupRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse("", s"name: '${name}' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => readerGroupRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  override def deleteReaderGroupById(user: Username, request: DeleteReaderGroupByIdRequest): Future[DeleteResponse] = {
    readerGroupRepo.dal.deleteById(request.id)
  }

  override def deleteReaderGroupBulk(user: Username, request: DeleteReaderGroupBulkRequest): Future[DeleteResponse] = {
    readerGroupRepo.dal.deleteBulk(request.ids)
  }

  // reader member

  override def insertReaderMember(user: Username, request: PostReaderMemberRequest): Future[UpsertResponse] = {
    Future.collect(Seq(readerMemberRepo.dal.isUnique("barcode", request.barcode),
      if (request.rfid == "") Future.value(true) else readerMemberRepo.dal.isUnique("rfid", request.rfid),
      readerMemberRepo.dal.isUnique("identity", request.identity))) flatMap {
      case Seq(true, true, true) => for {
        result <- readerLevelRepo.dal.findDepositById(request.levelId) flatMap {
          case Some(deposit) => for {
            reader <- ReaderMember.toDomain(request, deposit)
            level_name_response <- readerLevelRepo.dal.findById(reader.level)
            level_new <- readerLevelNewRepo.dal.findIdByName(Json.parse(level_name_response._2).as[ReaderLevel].name) flatMap {
              case a if a._1 == 200 => Future(Some(a._2))
              case _ => Future(Some("not configured"))
            }
            reader <- ReaderMember.toDomainWithNewLevel(request, deposit, level_new)
            id <- readerMemberRepo.dal.id(reader)
            result <- readerMemberRepo.dal.insertDoc(id, reader.jsonStringify)
          } yield result
          case None => Future.value(insertResponse("", s"""level_id: '${request.levelId}' does not exist""", false, Status.NotAcceptable.code))
        }
      } yield result
      case Seq(false, _, _) => Future.value(insertResponse("", s"""barcode: '${request.barcode}' exists""", false, Status.NotAcceptable.code))
      case Seq(_, false, _) => Future.value(insertResponse("", s"""rfid: '${request.rfid}' exists""", false, Status.NotAcceptable.code))
      case Seq(_, _, false) => Future.value(insertResponse("", s"""identity: '${request.identity}' exists""", false, Status.NotAcceptable.code))
    }
  }

  // Modified by kuangyuan 5/10/2017
  override def insertReaderMemberBulk(user: Username, request: PostReaderMemberBulkRequest): Future[UpsertResponse] = {
    for {
      response <- Future.collect(request.data.map(i =>
        for {
          result <-
          (i.identity, i.levelId) match {
            case (a, _) if a.length != 18 => Future.value((Status.NotAcceptable.code, s"""{"id": "${i.identity}", "barcode": "${i.barcode}", "result": "identity is empty or has the wrong number of digits", "created": false}"""))
            case (_, "") => Future.value((Status.NotAcceptable.code, s"""{"id": "${i.identity}", "barcode": "${i.barcode}", "result": "level id is empty", "created": false}"""))
            case (_, _) => readerLevelRepo.dal.findDepositById(i.levelId) flatMap {
              case Some(deposit) => for {
                reader <- ReaderMember.toDomain(i, deposit)
                id <- readerMemberRepo.dal.id(reader)
                result <- readerMemberRepo.dal.findById(id) flatMap {
                  case (Status.NotFound.code, _) => readerMemberRepo.dal.insertDoc(id, reader.jsonStringify)
                  case (_, _) => Future.value((Status.NotAcceptable.code, s"""{"id": "${i.identity}", "barcode": "${i.barcode}", "result":"reader already exists", "created": false}"""))
                }
              } yield result
              case None => Future.value((Status.NotAcceptable.code, s"""{"id": "${i.identity}", "barcode": "${i.barcode}", "result":" reader level: ${i.levelId} does not exist", "created": false}"""))
            }
          }

        } yield result))
    } yield bulkResponse(response)
  }

  override def findReaderMemberById(user: Username, request: GetReaderMemberByIdRequest): Future[SearchResponse] = {
    readerMemberRepo.dal.findByIdRich(request.id).map(a => a.exists match {
      case true => (Status.Ok.code, Json.stringify(Json.toJson(ReaderMemberIsSuspend.toDomain(a.id, Json.parse(a.sourceAsString).as[ReaderMember], true))))
      case false => (Status.NotFound.code, NotFound)
    })
  }

  // added by kuangyuan 5/4/2017
  override def findReaderMemberByBarcode(user: Username, request: GetReaderMemberByBarcodeRequest): Future[(StatusCode, Docs)] = {
    readerMemberRepo.dal.findReaderMemberByBarcode(request.barcode)
  }

  override def findReaderMembers(user: Username, request: GetReaderMemberListRequest): Future[SearchResponse] = {

    val result = for {
      response <- readerMemberRepo.dal.findAll(request)
      readers = response.hits.map(a => ReaderMemberIsSuspend.toDomain(a.id, Json.parse(a.sourceAsString).as[ReaderMember], false))
    } yield (response.original.status().getStatus, (response.totalHits.toInt, Json.stringify(Json.toJson(readers))))
    result.map(result =>
      (result._1, request.response(result._2._1, result._2._2)))
  }

  override def patchReaderMemberBulk(user: Username, request: PatchReaderMemberBulkRequest): Future[UpdateResponse] = {
    request.action match {
      case "inactivate" => inactivateReaderMemberBulk(user, request.ids.get)
      case "suspend" => suspendReaderMemberBulk(user, request.ids.get, request.days.get)
      case "inactivate_by_group_id" =>
        for {
          ids <- readerMemberRepo.dal.findIdsByGroupId(request.groupIds.get)
          result <- inactivateReaderMemberBulk(user, ids)
        } yield result

    }
  }

  private def patchReaderMemberBulk(user: Username, ids: Seq[String], _docMap: Map[String, Any]): Future[UpdateResponse] = {
    Future.collect(ids.map(i => readerMemberRepo.dal.updateDoc(i, _docMap))).map(bulkResponse)
  }

  private def inactivateReaderMemberBulk(user: Username, ids: Seq[String]): Future[UpdateResponse] = {
    patchReaderMemberBulk(user, ids, Map("is_active" -> false, "datetime" -> Time.now.toString))
  }

  private def suspendReaderMemberBulk(user: Username, ids: Seq[String], days: Int): Future[UpdateResponse] = {
    patchReaderMemberBulk(user, ids, Map("restore_at" -> Time.nextNdays(days).toString, "datetime" -> Time.now.toString))
  }

  // Added by kuangyuan 5/13/2017
  private def patchBookItemsBulk(user: Username, ids: Seq[String], _docMap: Map[String, Any]): Future[UpdateResponse] = {
    Future.collect(ids.map(i => bookItemRepo.dal.updateDoc(i, _docMap))).map(bulkResponse)
  }

  private def patchBookItemsActiveStatusBulk(user: Username, ids: Seq[String], is_active: Boolean = false): Future[UpdateResponse] = {
    Future.collect(ids.map(id => bookItemRepo.dal.findById(id) flatMap {
      case a if a._1 == Status.NotFound.code => Future.value((Status.NotFound.code, valueJson("error", s"Book with barcode ${id} not found.")))
      case b => val bookRecord = Json.parse(b._2).as[Book]
        bookRecord.is_active match {
          case a if a == is_active => Future.value((Status.NotAcceptable.code, valueJson("error", s"Duplicate action")))
          case b => patchBookItem(user, id, Map("is_active" -> is_active, "datetime" -> Time.now.toString))
        }
    })).map(bulkResponse)
//    patchBookItemsBulk(user, ids, Map("is_active" -> is_active, "datetime" -> Time.now.toString))
  }

  private def alterBookItemsStackId(user: Username, ids: Seq[String], stackId: String): Future[UpdateResponse] = {
    patchBookItemsBulk(user, ids, Map("stack" -> stackId, "datetime" -> Time.now.toString))
  }

  override def patchReaderMemberById(user: Username, request: PatchReaderMemberByIdRequest): Future[UpdateResponse] = {
    readerMemberRepo.dal.updateDoc(request.id, request.patchRequest)
  }

  // Added by kuangyuan 5/15/2017
  private def patchBookItem(user: Username, id: String, _docMap: Map[String, Any]): Future[UpdateResponse] = {
    bookItemRepo.dal.updateDoc(id, _docMap)
  }

  override def findBorrowRecords(user: Username, request: GetReaderMemberBorrowRecordListRequest): Future[SearchResponse] = {
    borrowHistoryRepo.dal.findAllDocs(request)
  }

  def insertResponse(id: String, result: String, created: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "created" : $created}""")

  def updateResponse(id: String, result: String, updated: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "updated" : $updated}""")

  def deleteResponse(id: String, result: String, deleted: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "deleted" : $deleted}""")

  def returnBookResponse(id: String, result: String, returned: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "returned" : $returned}""")

  def borrowBookResponse(id: String, result: String, borrowed: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "borrowed" : $borrowed}""")

  def renewBookResponse(id: String, result: String, renewed: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "renewed" : $renewed}""")

  def reservationBookResponse(id: String, result: String, reservation: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "reservationed" : $reservation}""")



  // borrow
  override def borrowItems(user: Username, request: PostReaderMemberBorrowItemsRequest): Future[(StatusCode, Docs)] = {

    for {
    r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
      case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
      case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
        (m.is_active, m.restore_at) match {
        case (true, suspend) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停证直至${suspend.toString}"]}"""))
        case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
        case (true, ok) =>
      readerLevelRepo.dal.findByIdAsOptString(m.level) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["未知读者类型 ${m.level}"]}"""))
        case Some(level) =>
        for {
          result <- borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id)), _n = Seq(existsQuery("_return"))) flatMap {
            case holdinglength =>
            val l = Json.parse(level._2).as[ReaderLevel]
            val currentLimit = l.borrow_rule.quantity - holdinglength
            (request.bookBarcodes.length <= currentLimit, m.credit > 0) match {
              case (true, true) => borrow(member._1, m, request.id, l.borrow_rule.day, request.bookBarcodes, request.location)
              case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["超过可借上限, 已借:${holdinglength}本, 当前可借: ${currentLimit}本, 此次请求: ${request.bookBarcodes.length}本"]}"""))
              case (_, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足, 当前为${m.credit}元 "}"""))
            }
          }
        } yield result
      }
      }
    }
    } yield r
  }

  // Added by kuangyuan 4/18/2017
  override def borrowItemsNew(user: Username, request: PostReaderMemberBorrowItemsRequest): Future[(StatusCode, Docs)] = {
    for {
      r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
        case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
          (m.is_active, m.restore_at) match {
            case (true, suspend) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停证直至${suspend.toString}"]}"""))
            case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
            case (true, ok) =>
              getReaderMemberDelayedFine(request.id) flatMap {
                case a if a._2.toDouble > 0 => Future.value((Status.NotAcceptable.code, valueJson("message", "读者有逾期未还的图书，请归还并缴纳罚款，才可继续借书。")))
                case _ =>
                  readerLevelNewRepo.dal.findByIdAsOptString(m.level_new.getOrElse("0")) flatMap {
                    case None => Future.value((Status.NotAcceptable.code, s"""{"errors": ["未知读者类型 ${m.level_new.getOrElse("no level_new configured")}]}"""))
                    case Some(level) =>
                      for {
                        booksCount <- countBookByCategory(request.bookBarcodes, "普通图书")
                        periodicalsCount <- countBookByCategory(request.bookBarcodes, "期刊")
                        result <- borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("book.category", "普通图书")), _n = Seq(existsQuery("_return"))) flatMap {
                          case holdingLength =>
                            val l = Json.parse(level._2).as[ReaderLevelNew]

                            val currentLimitBook = l.borrow_rule_new.borrow_rule_book.quantity - holdingLength
                            (booksCount <= currentLimitBook, m.credit > 0) match {
                              case (true, true) => borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("book.category", "期刊")), _n = Seq(existsQuery("_return"))) flatMap {
                                case holdingLengthP =>
                                  val currentLimitP = l.borrow_rule_new.borrow_rule_periodical.quantity - holdingLengthP
                                  periodicalsCount <= currentLimitP match {
                                    case true => borrowNew(member._1, m, request.id, l.borrow_rule_new.borrow_rule_book.day, l.borrow_rule_new.borrow_rule_periodical.day, request.bookBarcodes, request.location)
                                    case false => Future.value((Status.NotAcceptable.code, s"""{"errors": ["期刊超过可借上限，已借:${holdingLengthP}本，当前可借：${currentLimitP}本，此次请求：${periodicalsCount}本"]}"""))
                                  }
                              }

                              case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["普通图书超过可借上限, 已借:${holdingLength}本, 当前可借: ${currentLimitBook}本, 此次请求: ${booksCount}本"]}"""))
                              case (_, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足, 当前为${m.credit}元 "}"""))
                            }

                        }
                      } yield result
                  }
              }
          }
      }
    } yield r
  }

  private def borrowNew(version: Long, readerMember: ReaderMember, id: String, days_book: Int, days_periodical: Int, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    Future.collect(bookBarcodes.map(barcode => bookItemRepo.dal.findByIdAsOptString(barcode) flatMap {
      case None =>
        Future.value(borrowBookResponse("", s"${barcode}不存在", false))
      case Some(book_resp) => val bookItem = Json.parse(book_resp._2).as[Book]
        (bookItem.is_available, bookItem.is_active) match {
          case (_, false) =>
            Future.value(borrowBookResponse("", "非流通图书，请将其归还至管理员处，谢谢!", false))
          case (true, true) =>
            for {
              his <- reservationHistoryRepo.dal.findAll(_m = Seq(termQuery("book.barcode.keyword", barcode), termQuery("status.keyword", "reservation")))
              days = if (bookItem.category == "普通图书") days_book else days_periodical
              r <- his.ids.headOption match {
                case None => for {
                  _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                  borrowHistory = BorrowHistory.toDomain("borrow", readerMember.toReaderInfo(id), bookItem, TimeLocation(Time.now.toString, location), None, None, days)
                  result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
                } yield result match {
                  case (Status.Created.code, s, id) =>
                    borrowBookResponse(id, s"成功借阅，应还时间为${borrowHistory.due_at}", true)
                  case (other, s, _) => borrowBookResponse("", s, false)
                }
                case Some(hid) => {
                  reservationHistoryRepo.dal.findByIdAsOptString(hid) flatMap {
                    case None =>Future.value(borrowBookResponse("", "系统错误，请和管理员联系", false))
                    case Some(res) => val m = Json.parse(res._2).as[ReservationHistory]
                      m.reader.id match {
                        case selfRes if m.reader.id==id =>for {
                          _ <- reservationHistoryRepo.dal.updateStatus(hid)
                          _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                          borrowHistory = BorrowHistory.toDomain("borrow",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location), None, None, days)
                          result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
                        } yield result match {
                          case (Status.Created.code, s, id) =>
                            borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
                          case (other, s, _) => borrowBookResponse("", s, false)
                        }
                        case otherRes if m.reader.id!=id =>{
                          borrowHistoryRepo.dal.getReturnedDays(barcode) flatMap{
                            case s if(s-3)>0=> for {
                              _ <- reservationHistoryRepo.dal.updateStatus(hid)
                              _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                              borrowHistory = BorrowHistory.toDomain("borrow",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location), None, None, days)
                              result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
                            } yield result match {
                              case (Status.Created.code, s, id) =>
                                borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
                              case (other, s, _) => borrowBookResponse("", s, false)
                            }
                            case s if(s-3)<=0 =>  Future.value(borrowBookResponse("", "被预约图书，请交给管理员处理", false))
                          }
                        }
                      }
                  }
                }
              }
            } yield r

          case (false, _) => Future.value(borrowBookResponse("", "在借图书", false))
        }
    })) flatMap {
      case all =>
        Future.value((Status.Ok.code, "[" + all.map(_._2).mkString(",") + "]"))
    }
  }

  private def borrow(version: Long, readerMember: ReaderMember, id: String, day: Int, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    Future.collect(bookBarcodes.map(barcode => bookItemRepo.dal.findByIdAsOptString(barcode) flatMap {
      case None =>
      Future.value(borrowBookResponse("", s"${barcode}不存在", false))
      case Some(book) => val bookItem = Json.parse(book._2).as[Book]
        (bookItem.is_available, bookItem.is_active) match {
          case (_, false) =>
            Future.value(borrowBookResponse("", "非流通图书, 请将其归还至管理员处, 谢谢!", false))
          case (true, true) =>
            //查询是否是预约的图书，不是预约图书可以借书，是预约图书，是自己预约图书可以借书，并更新预约记录
            // 不是本人预约的，是否还书超过三天， 过三天可以借书，不超过三天不能借书
           for{
             his<- reservationHistoryRepo.dal.findAll(_m = Seq(termQuery("book.barcode.keyword", barcode),termQuery("status.keyword", "reservation")))
             r <- his.ids.headOption match {
               case None =>   for {
                 _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                 borrowHistory = BorrowHistory.toDomain("borrow",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location), None, None, day)
                 result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
               } yield result match {
                 case (Status.Created.code, s, id) =>
                   borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
                 case (other, s, _) => borrowBookResponse("", s, false)
               }
               case Some(hid) =>{
                 reservationHistoryRepo.dal.findByIdAsOptString(hid) flatMap {
                   case None =>Future.value(borrowBookResponse("", "系统错误，请和管理员联系", false))
                   case Some(res) => val m = Json.parse(res._2).as[ReservationHistory]
                     m.reader.id match {
                       case selfRes if m.reader.id==id =>for {
                         _ <- reservationHistoryRepo.dal.updateStatus(hid)
                         _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                         borrowHistory = BorrowHistory.toDomain("borrow",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location), None, None, day)
                         result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
                       } yield result match {
                         case (Status.Created.code, s, id) =>
                           borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
                         case (other, s, _) => borrowBookResponse("", s, false)
                       }
                       case otherRes if m.reader.id!=id =>{
                         borrowHistoryRepo.dal.getReturnedDays(barcode) flatMap{
                           case s if(s-3)>0=> for {
                             _ <- reservationHistoryRepo.dal.updateStatus(hid)
                             _ <- bookItemRepo.dal.updateAvailability(barcode, false)
                             borrowHistory = BorrowHistory.toDomain("borrow",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location), None, None, day)
                             result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
                           } yield result match {
                             case (Status.Created.code, s, id) =>
                               borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
                             case (other, s, _) => borrowBookResponse("", s, false)
                           }
                           case s if(s-3)<=0 =>  Future.value(borrowBookResponse("", "被预约图书，请交给管理员处理", false))
                         }
                       }
                     }
                 }
               }
             }
           }yield r


          case (false, _) => Future.value(borrowBookResponse("", "在借图书", false))
        }
    })) flatMap {
      case all =>
        Future.value((Status.Ok.code, "[" + all.map(_._2).mkString(",") + "]"))
    }

  }


  // renew
  override def renewItems(user: Username, request: PostReaderMemberRenewItemsRequest): Future[(StatusCode, Docs)] = {
    for {
    r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
      case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
      case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
      (m.is_active, m.restore_at, m.credit > 0) match {
        case (_, _, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足, 当前为${m.credit}元 "}"""))
        case (true, suspend, _) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停证直至${suspend.toString}"]}"""))
        case (false, _, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
        case (true, ok, _) =>
      readerLevelRepo.dal.findByIdAsOptString(m.level) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["未知读者类型 ${m.level}"]}"""))
        case Some(level) =>
        val l = Json.parse(level._2).as[ReaderLevel]
        l.borrow_rule.can_renew match {
          case true => renew(member._1, m, request.id, l.borrow_rule.day, request.bookBarcodes, request.location)
          case false => Future.value((Status.NotAcceptable.code,s"""{"errors": ["卡类型: ${l.name}无法续借图书 "]}"""))
        }

      }
      }
    }
    } yield r
  }

  override def renewItemsNew(user: Username, request: PostReaderMemberRenewItemsRequest): Future[(StatusCode, Docs)] = {
    for {
      r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
        case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
          (m.is_active, m.restore_at, m.credit > 0) match {
            case (_, _, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足，当前为${m.credit}元 "}"""))
            case (true, suspend, _) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停征直至${suspend.toString}"]}"""))
            case (false, _, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
            case (true, ok, _) =>
              getReaderMemberDelayedFine(request.id) flatMap {
                case a if a._2.toDouble > 0 => Future.value((Status.NotAcceptable.code, valueJson("message", "该读者有逾期未还的图书，请归还并缴纳罚款")))
                case _ =>
                  readerLevelRepo.dal.findByIdAsOptString(m.level_new.getOrElse("0")) flatMap {
                    case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["未知读者类型 ${m.level_new.getOrElse("No level_new configured")}"]}"""))
                    case Some(level) =>
                      val l = Json.parse(level._2).as[ReaderLevelNew]
                      (l.borrow_rule_new.borrow_rule_book.can_renew, l.borrow_rule_new.borrow_rule_periodical.can_renew) match {
                        case (true, true) => renewNew(member._1, m, request.id, l.borrow_rule_new.borrow_rule_book.day, l.borrow_rule_new.borrow_rule_periodical.day, request.bookBarcodes, request.location)
                        case (false, false) => Future.value((Status.NotAcceptable.code,s"""{"errors": ["卡类型: ${l.name}无法续借图书 "]}"""))
                        case (false, true) => renewNew(member._1, m, request.id, -1, l.borrow_rule_new.borrow_rule_periodical.day, request.bookBarcodes, request.location)
                        case (true, false) => renewNew(member._1, m, request.id, l.borrow_rule_new.borrow_rule_book.day, -1, request.bookBarcodes, request.location)
                      }

                  }
              }
          }
      }
    } yield r
  }

  private def renewNew(version: Long, readerMember: ReaderMember, id: String, days_book: Int, days_periodical: Int, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    val now = Time.now.toString
    Future.collect(bookBarcodes.map(b =>
      for {
        bh <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("book.barcode", b),
          matchPhraseQuery("reader.barcode", readerMember.barcode), rangeQuery("due_at").gte(now)), _n = Seq(existsQuery("_return"), existsQuery("_renew")))
        now = Time.now.toString
        category = Json.parse(b).as[BookItem].category
        result <- (bh.ids.headOption, category) match {
          case (None, _) =>
            Future.value(renewBookResponse("", s"图书${b}无法续借, 已续借过此图书或已被归还", false))
          case (Some(bhId), "普通图书") =>
            days_book match {
              case -1 => Future.value(renewBookResponse("", "读者无续借普通图书权限", false))
              case _ =>
                val dueAt = Time.nextNdays(days_book).toString
                for {result <- borrowHistoryRepo.dal.renewAt(bhId, TimeLocation(now,location), dueAt, now)} yield result._1 match {
                  case 200 => renewBookResponse(bhId, s"成功续借, 应还时间为${dueAt}", true)
                  case _ => renewBookResponse("", "内部错误, 请联系管理员", false)
                }
            }
          case (Some(bhId), "期刊") =>
            days_periodical match {
              case -1 => Future.value(renewBookResponse("", "读者无续借期刊权限", false))
              case _ =>
                val dueAt = Time.nextNdays(days_periodical).toString
                for {result <- borrowHistoryRepo.dal.renewAt(bhId, TimeLocation(now,location), dueAt, now)} yield result._1 match {
                  case 200 => renewBookResponse(bhId, s"成功续借, 应还时间为${dueAt}", true)
                  case _ => renewBookResponse("", "内部错误, 请联系管理员", false)
                }
            }
          case (Some(s), categ) =>
            Future.value(renewBookResponse(s, s"${categ}类型图书尚不支持", false))
        }
      } yield result
    )) flatMap {
      case all =>
        Future.value((Status.Ok.code, "[" + all.map(_._2).mkString(",") + "]"))
    }

  }

  private def renew(version: Long, readerMember: ReaderMember, id: String, days: Int, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    val now = Time.now.toString
    Future.collect(bookBarcodes.map(b =>
      for {
        bh <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("book.barcode", b),
          matchPhraseQuery("reader.barcode", readerMember.barcode), rangeQuery("due_at").gte(now)), _n = Seq(existsQuery("_return"), existsQuery("_renew")))
        now = Time.now.toString
        result <- bh.ids.headOption match {
          case None =>
            Future.value(renewBookResponse("", s"图书${b}无法续借, 已续借过此图书或已被归还", false))
          case Some(bhId) =>
            val dueAt = Time.nextNdays(days).toString
            for {result <- borrowHistoryRepo.dal.renewAt(bhId, TimeLocation(now,location), dueAt, now)} yield result._1 match {
              case 200 => renewBookResponse(bhId, s"成功续借, 应还时间为${dueAt}", true)
              case _ => renewBookResponse("", "内部错误, 请联系管理员", false)
            }
        }
      } yield result
    )) flatMap {
      case all =>
        Future.value((Status.Ok.code, "[" + all.map(_._2).mkString(",") + "]"))
    }

  }

  // added by kuangyuan 5/15/2017
  def countBookByCategory(barcodes: Seq[String], category: String): Future[Int] = {
    Future.collect(
      barcodes.map(barcode =>
        bookItemRepo.dal.findByIdAsOptString(barcode) flatMap {
          case None => Future(0)
          case Some(book) => val bookItem = Json.parse(book._2).as[Book]
            bookItem.category match {
              case a if a.equals(category) => Future(1)
              case _ => Future(0)
            }
        }
      )
    ) flatMap {
      case all => Future.value(all.sum)
    }
  }

  override def reservationItemsNew(user: Username, request: PostReaderMemberReservationItemsRequest): Future[(StatusCode, Docs)] = {

    for {
      r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
        case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
          (m.is_active, m.restore_at) match {
            case (true, suspend) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停证直至${suspend.toString}"]}"""))
            case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
            case (true, ok) =>
              readerLevelNewRepo.dal.findByIdAsOptString(m.level_new.getOrElse("0")) flatMap {
                case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["未知读者类型 ${m.level_new.getOrElse("no level_new configured")}"]}"""))
                case Some(level) =>
                  for {
                    booksCount <- countBookByCategory(request.bookBarcodes, "普通图书")
                    periodicalsCount <- countBookByCategory(request.bookBarcodes, "期刊")
                    result <- borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("book.category", "普通图书")), _n = Seq(existsQuery("_return"))) flatMap {
                      case holdinglength_book =>
                        val l = Json.parse(level._2).as[ReaderLevelNew]
                        val currentLimit = l.borrow_rule_new.borrow_rule_book.quantity - holdinglength_book
                        (booksCount <= currentLimit, m.credit > 0) match {
                          case (true, true) => borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("book.category", "期刊")), _n = Seq(existsQuery("_return"))) flatMap {
                            case holdinglengthP =>
                              val currentLimitP = l.borrow_rule_new.borrow_rule_periodical.quantity - holdinglengthP

                              periodicalsCount <= currentLimitP match {
                                case true => reservation(member._1, m, request.id, request.bookBarcodes, request.location)
                                case false => Future.value((Status.NotAcceptable.code, s"""{"errors": ["期刊超过可借上限，已借:${holdinglengthP}本，当前可借：${currentLimitP}本，此次请求：${periodicalsCount}本"]}"""))
                              }
                          }

                          case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["普通图书超过可借上限, 已借:${holdinglength_book}本, 当前可借: ${currentLimit}本, 此次请求: ${booksCount}本"]}"""))
                          case (_, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足, 当前为${m.credit}元 "}"""))
                        }
                    }
                  } yield result
              }
          }
      }
    } yield r
  }

  // 预约图书
  override def reservationItems(user: Username, request: PostReaderMemberReservationItemsRequest): Future[(StatusCode, Docs)] = {

    for {
      r <- readerMemberRepo.dal.findByIdAsOptString(request.id) flatMap {
        case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["读者不存在"]}"""))
        case Some(member) => val m = Json.parse(member._2).as[ReaderMember]
          (m.is_active, m.restore_at) match {
            case (true, suspend) if DateTime.parse(suspend).isAfterNow => Future.value((Status.NotAcceptable.code, s"""{"errors": ["读者被停证直至${suspend.toString}"]}"""))
            case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["该读者证已被注销"]}"""))
            case (true, ok) =>
              readerLevelRepo.dal.findByIdAsOptString(m.level) flatMap {
                case None => Future.value((Status.NotAcceptable.code,s"""{"errors": ["未知读者类型 ${m.level}"]}"""))
                case Some(level) =>
                  for {
                    result <- borrowHistoryRepo.dal.count(_m = Seq(matchPhraseQuery("reader.id", request.id)), _n = Seq(existsQuery("_return"))) flatMap {
                      case holdinglength =>
                        val l = Json.parse(level._2).as[ReaderLevel]
                        val currentLimit = l.borrow_rule.quantity - holdinglength
                        (request.bookBarcodes.length <= currentLimit, m.credit > 0) match {
                          case (true, true) => reservation(member._1, m, request.id, request.bookBarcodes, request.location)
                          case (false, _) => Future.value((Status.NotAcceptable.code, s"""{"errors": ["超过可借上限, 已借:${holdinglength}本, 当前可借: ${currentLimit}本, 此次请求: ${request.bookBarcodes.length}本"]}"""))
                          case (_, false) => Future.value((Status.NotAcceptable.code, s"""{"errors": "剩余押金不足, 当前为${m.credit}元 "}"""))
                        }
                    }
                  } yield result
              }
          }
      }
    } yield r
  }

  private def reservation(version: Long, readerMember: ReaderMember, id: String, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    val isReservationed=true
    Future.collect(bookBarcodes.map(barcode => bookItemRepo.dal.findByIdAsOptString(barcode) flatMap {
      case None =>
        Future.value(reservationBookResponse("", s"${barcode}不存在", false))
      case Some(book) => val bookItem = Json.parse(book._2).as[Book]
        (bookItem.is_available, bookItem.is_active) match {
          case (_, false) =>
            Future.value(reservationBookResponse("", "非流通图书, 请将其归还至管理员处, 谢谢!", false))
            //在借图书,才可以预约
          case (false, _) =>

            //查询预约记录，判断此书是否已经预约
            reservationHistoryRepo.dal.count(_m = Seq(termQuery("book.barcode.keyword", barcode),termQuery("status.keyword", "reservation"))) flatMap {
              case 0=>for {
              //在预约表中写入记录
                result <- reservationHistoryRepo.dal.insertDocReturnId(ReservationHistory.toDomain("reservation",readerMember.toReaderInfo(id),bookItem, TimeLocation(Time.now.toString, location)).jsonStringify)
              } yield  result  match {
                case (Status.Created.code, s, id) =>
                  reservationBookResponse(barcode, s"成功预约, 收到取书通知后3天内到管理员处取书", true)
                case (other, s, _) =>  reservationBookResponse(barcode, s, false)
              }
              case _=>  Future.value(reservationBookResponse(barcode, "本书已经被预约，不可以预约", false))
            }
          case (true,true) => Future.value(reservationBookResponse(barcode, "在馆图书，可以直接借书", false))
        }
    })) flatMap {
      case all =>
        Future.value((Status.Ok.code, "[" + all.map(_._2).mkString(",") + "]"))
    }

  }

  // return
  override def returnBooks(user: Username, request: PostBookItemsReturnRequest): Future[(StatusCode, Docs)] = {
    _return(user, request.bookBarcodes, request.location)
  }

  private def _return(user: Username, bookBarcodes: Seq[String], location: String) = {
    Future.collect(bookBarcodes.map(b => for {
      history <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("book.barcode", b)), _n = Seq(existsQuery("_return")))
      now = Time.now.toString
      result <- history.ids.headOption match {
        case None => Future.value(returnBookResponse("", "图书非在借状态, 无法归还", false))
          //查询是否是别人预约的图书，是预约图书，判断是否在管理员处还书，是,正常还书，并发送信息给预约图书的人
        // 不是则需要还到管理员处，并发送信息给预约图书的人，如果不是预约图书就正常还书

        case Some(id) =>
          reservationHistoryRepo.dal.count(_m = Seq(termQuery("book.barcode.keyword", b),termQuery("status.keyword", "reservation"))) flatMap {
            case 0=>for {
              ret <- borrowHistoryRepo.dal.returnAt(id,TimeLocation(now, location), now)
              ava <- bookItemRepo.dal.updateAvailability(b, true, now)
            } yield (ret._1, ava._1) match {
              case (200, 200) => returnBookResponse(id, s"成功归还, 归还时间为${now}", true)
              case (_, _) => returnBookResponse("", "内部错误, 请联系管理员", false)
            }
            case _=> location match {
              case s if (location=="管理员")=>for {
                ret <- borrowHistoryRepo.dal.returnAt(id,TimeLocation(now, location), now)
                ava <- bookItemRepo.dal.updateAvailability(b, true, now)
              } yield (ret._1, ava._1) match {
                case (200, 200) => returnBookResponse(id, s"成功归还, 归还时间为${now}", true)
                case (_, _) => returnBookResponse("", "内部错误, 请联系管理员", false)
              }
              case s if (location!="管理员")=>Future.value(returnBookResponse("", "预约图书, 请归还在管理员处", false))
            }

          }
      }
    } yield result
    )).map(a => (200, "[" + a.map(_._2).mkString(",") + "]"))
  }



  //leixx,2017-4-6
  //TODO:更改图书业务和物理状态
  override def updateBookItemBusinessAndPhysicalState(user:Username,request:PatchBookPhysicalStackByBarCodeRequest):Future[UpdateResponse]= {
    bookItemRepo.dal.findByBarcode(request.barcode).flatMap{
      case None => Future.value((404,valueJson("error", "Not Found")))
      case Some((id, bookDoc))=>bookDoc.shelf match {
        case "" => bookItemRepo.dal.updateDoc(id, Map("shelf"-> request.shelf, "act_shelf"-> request.shelf))
          Future.value((200,valueJson("result", " 在馆正常在架")))
        case shelf =>(request.shelf.equals(shelf) ,bookDoc.is_available) match{
          case (true,true) =>bookItemRepo.dal.updateDoc(id, Map("act_shelf"-> request.shelf))
            Future.value((200,valueJson("result", " 在馆正常在架")))
          case (true,false)=>bookItemRepo.dal.updateDoc(id, Map("act_shelf"-> request.shelf))
            Future.value((200,valueJson("result", "外借正常在架")))
          case(false,true)=>request.shelf match {
            case "" => Future.value((200,valueJson("result", "在馆不在架") ))
            case _ => bookItemRepo.dal.updateDoc(id, Map("act_shelf" -> request.shelf))
              Future.value((200,valueJson("result", "在馆错架")))
          }
          case(false,false)=>request.shelf match{
            case ""=> Future.value((200,valueJson("result", "外借不在架")))
            case _=>bookItemRepo.dal.updateDoc(id, Map("act_shelf"-> request.shelf))
              Future.value((200,valueJson("result", "外借错架")))
          }
        }
      }
    }
  }

  //leixx,2017-4-8
  // 对图书在架物理位置进行初始化
  override def initBookItemShelf(user:Username,request:PatchBookPhysicalStackByBarCodeRequest):Future[UpdateResponse]= {
    bookItemRepo.dal.findByBarcode(request.barcode).flatMap{
      case None => Future.value((404,valueJson("error", "Not Found")))
      case Some((id, bookDoc))=>bookDoc.is_available match{
        case true=>bookItemRepo.dal.updateDoc(id, Map("shelf"-> request.shelf, "act_shelf"-> request.shelf))
          Future.value((200,valueJson("result","初始化架位信息成功")))
        case false=> Future.value((403,valueJson("error", "Fail because book item is borrowed")))
      }
    }
  }


  override def getCheckProgress(user: Username, request: GetCheckProgressRequest): Future[(StatusCode, Docs)] = {

    redisService.get_new("current_entries") flatMap {
      case NotFound => Future((Status.PreconditionFailed.code, valueJson("error", "当前没有进行中的盘点")))
      case current => redisService.get_new("total_entries") flatMap {
        case NotFound => Future((Status.PreconditionFailed.code, valueJson("error", "当前没有进行中的盘点")))
        case total => Future((Status.Ok.code, valueJson("progress", BigDecimal.decimal(current.toFloat / total.toFloat).setScale(2, RoundingMode.HALF_EVEN).toString)))
      }
    }
  }

  override def getBookItemShelfStatusByDir(user: Username, request: GetBookPhysicalShelfStatusViaFTPRequest): Future[(StatusCode, Docs)] = {
    val client = new FTPClient
    val ip_path_tuple = request.directory.indexOf('/') match {
      case -1 => (request.directory, "")
      case a => request.directory.splitAt(a)
    }

    try {
      client.connect(ip_path_tuple._1)
      client.login(request.username, request.password)
      client.enterLocalPassiveMode()
    } catch {
      case ex: SocketException =>
        Future.value((Status.InternalServerError.code, valueJson("exception", ex.getMessage)))
      case ex: IOException =>
        Future.value((Status.BadRequest.code, valueJson("exception", ex.getMessage)))
    }

    client.isConnected match {
      case false => Future((Status.ClientClosedRequest.code, valueJson("error", "ftp directory does not exist")))
      case true =>
        val body = s"""[ ${client.listFiles(ip_path_tuple._2.substring(1)).map(a => a.getName.toLowerCase.endsWith(".json") match {
          case true =>
            val stream = client.retrieveFileStream(ip_path_tuple._2.substring(1) + "/" + a.getName)
            client.completePendingCommand()

            val source = scala.io.Source.fromInputStream(stream).mkString

            val checkObject = Json.parse(source).as[CheckObject]
            valuesJson(
              Seq("file_name", "result"),
              Seq(
                a.getName,
                s"[ ${checkObject.data.map(checkItem => bookItemRepo.dal.findByBarcode(checkItem.barcode) flatMap {
                  case None =>
                    Future(valuesJson(Seq("barcode", "result"), Seq(checkItem.barcode, "图书未找到")))
                  case Some(bookTuple) =>

                    getBookItemBusinessAndPhysicalStateByObject(user, bookTuple._2).map(s =>
                      valuesJson(Seq("book_info", "business_status"), Seq(Json.stringify(Json.toJson(bookTuple._2)), s), Seq(false, false))
                    )
                }
                ).map(a => Await.result(a)).mkString(",")}]"
              ),
              Seq(true, false)
            )

//            s"""{"${a.getName}": [ ${checkObject.data.map(checkItem => bookItemRepo.dal.findByBarcode(checkItem.barcode) flatMap {
//              case None =>
//                Future(valuesJson(Seq("barcode", "result"), Seq(checkItem.barcode, "图书未找到")))
//              case Some(bookTuple) =>
//                getBookItemBusinessAndPhysicalStateByObject(user, bookTuple._2)
//            }
//            ).map(a => Await.result(a)).mkString(",")}  ]}"""
//            s"""{"${a.getName}": [ ${Future.collect(checkObject.data.map(checkItem => bookItemRepo.dal.findByBarcode(checkItem.barcode) flatMap {
//              case None =>
//                Future(valueJson("result", s"条码号为${checkItem.barcode}的图书未找到"))
//              case Some(bookTuple) =>
//                getBookItemBusinessAndPhysicalStateByObject(user, bookTuple._2)
//            }
//            )) flatMap {
//              case all => Future.value(all.mkString(","))
//            }}  ]}"""
          case false =>
            a.isFile match {
              case false =>
                valuesJson(Seq("folder_name", "result"), Seq(a.getName, "Skipping since it's a folder"))
              case true =>
                valuesJson(Seq("file_name", "result"), Seq(a.getName, "Skipping since it's not a .json file"))
            }
        }
        ).mkString(",")} ]"""

        Future((Status.Ok.code, valueJsonArrayObject("check_result_all_files", body)))

    }

  }

  override def updateBookItemsShelfStatusByDir(user: Username, request: PatchBookPhysicalShelfStatusViaFTPRequest): Future[(StatusCode, Docs)] = {
//    val dir = new File(request.directory)
    val client = new FTPClient
    val ip_path_tuple = request.directory.indexOf('/') match {
      case -1 => (request.directory, "")
      case a => request.directory.splitAt(a)
    }
    try {
      client.connect(ip_path_tuple._1)
      client.login(request.username, request.password)
      client.enterLocalPassiveMode()
    } catch {
      case ex: SocketException =>
        Future.value((Status.InternalServerError.code, valueJson("exception", ex.getMessage)))
      case ex: IOException =>
        Future.value((Status.BadRequest.code, valueJson("exception", ex.getMessage)))
    }

    // dir.exists() match {
    // TODO with try catch clause above I might be able to dump the pattern matching here :)
    client.isConnected match {
      case false => Future((Status.ClientClosedRequest.code, valueJson("error", "ftp directory does not exist")))
      case true =>
        var totalEntries = 0
        var currentEntries = 0
//        dir.listFiles.map(file => file.getName.toLowerCase.endsWith(".json") match {
        client.listFiles(ip_path_tuple._2.substring(1)).map(file => file.getName.toLowerCase.endsWith(".json") match {
          case true =>
//            val source = scala.io.Source.fromFile(file.getAbsolutePath).mkString
            val stream = client.retrieveFileStream(ip_path_tuple._2.substring(1) + "/" + file.getName)
            client.completePendingCommand

            val source = scala.io.Source.fromInputStream(stream).mkString
            totalEntries += Json.parse(source).as[CheckObject].data.length
          case false =>
            println(s"Skipping ${file.getName} in count")
        })
        redisService.set_new("total_entries", totalEntries.toString)
//        val body = "[" + dir.listFiles.map(a => a.getName.toLowerCase.endsWith(".json") match {
        val body = "[" + client.listFiles(ip_path_tuple._2.substring(1)).map(a => a.getName.toLowerCase.endsWith(".json") match {
          case true =>
//            val source = scala.io.Source.fromFile(a.getAbsolutePath).mkString
            val stream = client.retrieveFileStream(ip_path_tuple._2.substring(1) + "/" + a.getName)
            client.completePendingCommand()

            val source = scala.io.Source.fromInputStream(stream).mkString

            val checkObject = Json.parse(source).as[CheckObject]
            redisService.set_new("last_check_id", checkObject.check_id)
            s"""{"${a.getName}": [
              ${checkObject.data.map(checkItem => bookItemRepo.dal.findByBarcode(checkItem.barcode) flatMap {
                case None =>
                  currentEntries += 1
                  redisService.set_new("current_entries", currentEntries.toString)
                  Future(valueJson(s"${checkItem.barcode}", s"图书未找到"))
                case Some(book) =>
                  currentEntries += 1
                  redisService.set_new("current_entries", currentEntries.toString)
                  bookItemRepo.dal.updateDoc(checkItem.barcode, Map("last_check_id" -> checkObject.check_id)) flatMap {
                    case a if a._1 != 200 => println(s"书目${checkItem.barcode}实际在架信息初始化失败")
                      bookItemRepo.dal.updateDoc(checkItem.barcode, Map("shelf" -> checkItem.shelf, "act_shelf" -> checkItem.act_shelf, "last_check_time" -> checkItem.timestamp)) flatMap {
                        case a if a._1 != 200 => println(s"书目${checkItem.barcode}更新在架信息失败")
                          Future.value(valueJsonArrayObject(s"${checkItem.barcode}", a._2))
                        case b => Future.value(valueJsonArrayObject(s"${checkItem.barcode}", b._2))
                      }
                    case b => bookItemRepo.dal.updateDoc(checkItem.barcode, Map("shelf" -> checkItem.shelf, "act_shelf" -> checkItem.act_shelf, "last_check_time" -> checkItem.timestamp)) flatMap {
                      case a if a._1 != 200 => println(s"书目${checkItem.barcode}更新在架信息失败")
                        Future(valueJsonArrayObject(s"${checkItem.barcode}", a._2))
                      case b => Future.value(valueJsonArrayObject(s"${checkItem.barcode}", b._2))
                    }
                  }
              }
              ).map(a => Await.result(a)).mkString(",")}
            ]}"""
          case false =>
            a.isFile match {
              case false =>
                s"""{"${a.getName}": "skipping folder in check"}"""
              case true =>
                s"""{"${a.getName}": "skipping not json file in check"}"""
            }
        }
        ).mkString(",") + "]"
        println(s"current entries : ${currentEntries} \r\n of total entries: ${totalEntries}")
        redisService.dels(Seq("current_entries", "total_entries"))
        println("deleted!")
        try {
          client.disconnect()
        } catch {
          case ex: IOException => println("FTPClient closing failure:" + ex.getMessage)
        }
        Future((Status.Ok.code, valueJsonArrayObject("update_result_all_files", body)))
    }
  }

  def getBookItemBusinessAndPhysicalStateByObject(user: Username, bookDoc: Book):Future[String] = {
    bookDoc.last_check_id match {
      case "" => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "未盘点")))
      case check_id => (bookDoc.act_shelf.equals(bookDoc.shelf), bookDoc.is_available) match {
        case (true, true) => redisService.get_new("last_check_id") flatMap {
          case NotFound => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "在馆正常在架")))

          // exception
          case id if !id.equals(check_id) => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "在馆不在架")))
          case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "在馆正常在架")))
        }
        case (true, false) => redisService.get_new("last_check_id") flatMap {
          case NotFound => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "外借不在架")))

          // exception
          case id if id.equals(check_id) => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "外借正常在架")))
          case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "外借不在架")))
        }
        case (false, true) => redisService.get_new("last_check_id") flatMap {
          case id if !id.equals(check_id) => bookDoc.act_shelf match {
            case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "上一轮盘点未盘点此书")))
          }
          case _ => bookDoc.act_shelf match {
            case "" => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "在馆不在架")))
            case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "在馆错架")))
          }
        }
        case (false, false) => redisService.get_new("last_check_id") flatMap {
          case id if id.equals(check_id) => bookDoc.act_shelf match {
            case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "外借不在架")))
          }
          case _ => Future.value(valuesJson(Seq("barcode", "result"), Seq(bookDoc.barcode, "外借不在架")))
        }
      }
    }
  }

  // Added by kuang 4/20/2017
  //对图书业务和物理在架状态信息进行查询(新逻辑)
  def getBookItemBusinessAndPhysicalStateNew(user:Username,request:GetBookBusinessAndPhysicalStateByBarCodeRequest):Future[SearchResponse]={
    bookItemRepo.dal.findByBarcode(request.bar_code).flatMap{
      case None => Future.value((404,valueJson("error", "Not Found")))
      case Some((id, bookDoc))=> bookDoc.last_check_id match {
        case "" => Future.value((200, valueJson("result", "未盘点")))
        case s => getBookItemBusinessAndPhysicalStateByObject(user, bookDoc) flatMap {
          case a => Future(200, a)
        }
      }
    }
  }

  //leixx,2017-4-8
  //对图书业务和物理在架状态信息进行查询
  override def getBookItemBusinessAndPhysicalState(user:Username,request:GetBookBusinessAndPhysicalStateByBarCodeRequest):Future[SearchResponse]={
    bookItemRepo.dal.findByBarcode(request.bar_code).flatMap{
      case None => Future.value((404,valueJson("error", "Not Found")))
      case Some((id, bookDoc))=> bookDoc.last_check_id match {
        case "" => Future.value((200, valueJson("result", "未盘点")))
        case check_id => (bookDoc.act_shelf.equals(bookDoc.shelf), bookDoc.is_available) match {
          case (true,true) => Future.value(200, valueJson("result", "在馆正常在架"))
          case (true,false) => Future.value((200,valueJson("result", "外借正常在架")))
          case (false,true) => bookDoc.act_shelf match {
            case "" => Future.value((200, valueJson("result", "在馆不在架")))
            case _ => Future.value((200, valueJson("result", "在馆不在架")))
          }
          case (false,false) => bookDoc.act_shelf match{
            case "0"=> Future.value((200,valueJson("result", "外借不在架")))
            case _=>Future.value((200,valueJson("result", "外借错架")))
          }
        }
      }
    }
  }


  override def getReaderDelayedFine(user: Username, request: GetReaderMemberDelayedFineRequest): Future[(StatusCode, Docs)] = {
    getReaderMemberDelayedFine(request.id) flatMap {
      case a if a._1 == Status.Ok.code => Future.value((Status.Ok.code, valueJson("delayed_fine", a._2)))
      case b => Future.value(b)
    }
  }

  override def getReaderLostFine(user: Username, request: GetReaderMemberLostFineRequest): Future[(StatusCode, Docs)] = {
    readerMemberRepo.dal.findById(request.id) flatMap {
      case reader_not_found if reader_not_found._1 != 200 => Future((404, valueJson("error", "Reader not found")))
      case reader_response =>
        borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("book.barcode", request.bookBarcode)),
                                    _n = Seq(matchPhraseQuery("fined", true), matchPhraseQuery("lost", true))) flatMap {
          case none if none.hits.headOption == None => Future.value((Status.FailedDependency.code, valueJson("error", "reader isn't borrowing the book")))
          case some => for {
            lost_factor <- readerLevelRepo.dal.findById(Json.parse(reader_response._2).as[ReaderMember].level).map(res =>
              Json.parse(res._2).as[ReaderLevel].penalty_rule.lost_factor
            )

            r <- bookItemRepo.dal.findByBarcode(request.bookBarcode) flatMap {
              case None => Future((404, valueJson("error", "Book index doesn't exist or Book not found")))
              case Some(book) =>
                book._2.reference match {
                  case "" => println("Book reference not found")
                    Future((200, valueJson("lost_fine", BigDecimal(lost_factor * defaultBookPrice).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toDouble.toString)))
                  case s =>
                    // barcode equals id
                    bookItemRepo.dal.updateActive(book._2.barcode, false, Time.now.toString)
                    val res =
                      for {
                        book_ref <- bookReferenceRepo.dal.findById(s)
                        book_price <- "[1-9][0-9]*.[0-9]{0,2}".r.findFirstIn(Json.parse(book_ref._2).as[BookReference].ISBN.获得方式和或定价) match {
                          case None => Future(defaultBookPrice)
                          case Some(sd) => Future(sd.toDouble)
                        }
                      } yield BigDecimal(book_price * lost_factor).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toDouble.toString
                    res.map(a => (200, valueJson("lost_fine", a)))
                }

            }
          } yield r
        }


    }
  }

  override def updateReaderCredit(user: Username, request: UpdateReaderMemberCreditByIdRequest): Future[(StatusCode, Docs)] = {
    readerMemberRepo.dal.deductCredit(request.id, request.amount, Time.now.toString) flatMap {
      case a if a._1 == Status.Ok.code =>
        for {
          late_no_return_future_seq <- borrowHistoryRepo.dal.findAll(
            _m = Seq(matchPhraseQuery("reader.id", request.id), rangeQuery("due_at").lte(Time.now.toString)),
            _n = Seq(existsQuery("_return"), matchPhraseQuery("fined", true))) flatMap {
              case a => Future.collect(a.hits.map(s => borrowHistoryRepo.dal.updateFined(s.id, true)))
            }
          late_returned_future_seq <- borrowHistoryRepo.dal.findAll(
            _m = Seq(matchPhraseQuery("reader.id", request.id), existsQuery("_return"), scriptQuery("doc['due_at'].value < doc['_return.datetime'].value")),
            _n = Seq(matchPhraseQuery("fined", true))) flatMap {
              case a => Future.collect(a.hits.map(s => borrowHistoryRepo.dal.updateFined(s.id, true)))
            }
          lost_future_seq <- borrowHistoryRepo.dal.findAll(
            _m = Seq(matchPhraseQuery("reader.id", request.id), matchPhraseQuery("lost", true)),
            _n = Seq(matchPhraseQuery("fined", true))) flatMap {
              case a => Future.collect(a.hits.map(s => borrowHistoryRepo.dal.updateFined(s.id, true)))
            }

          // TODO bulkResponse
//        } yield (Status.Ok.code, valueJsonArrayObject("borrow_history_updates", s"[${late_no_return_future_seq.++:(late_returned_future_seq).++:(lost_future_seq).map(res => res._2).mkString(",")}]"))
        } yield bulkResponse(late_no_return_future_seq.++:(late_returned_future_seq).++:(lost_future_seq))
      case b => Future.value(b)
    }
  }

  //Statistics relevant
  override def findReaderBorrowRanking(user: Username, request: GetReaderBorrowRankingRequest): Future[(StatusCode, Docs)] = {
    borrowHistoryRepo.dal.getReaderBorrowRanking(request.start_time, request.end_time)
  }

  override def findBookBorrowRanking(user: Username, request: GetBookBorrowRankingRequest): Future[(StatusCode, Docs)] = {
    borrowHistoryRepo.dal.getBookBorrowRanking(request.start_time, request.end_time)
  }

  override def findBookFlowStatistics(user: Username, request: GetBookFlowStatisticsRequest): Future[(StatusCode, Docs)] = {
    borrowHistoryRepo.dal.getFlowData(request.start_time, request.end_time, request.interval, request.limit, request.minCount)
  }

  override def findCategoryFlowStatistics(user: Username, request: GetCategoryFlowStatisticsRequest): Future[(StatusCode, Docs)] = {
    borrowHistoryRepo.dal.getFlowDataByCategory(request.start_time, request.end_time, request.interval, request.limit, request.minCount)
  }

  override def findStockStatistics(user: Username, request: GetStockStatisticsRequest): Future[(StatusCode, Docs)] = {
    bookItemRepo.dal.getStackStockData()
  }

  override def findBookLeastBorrowStats(user: Username, request: GetBookLeastBorrowStatsRequest): Future[(StatusCode, Docs)] = {
//    bookItemRepo.dal.findOldBooks(request.milestone) flatMap {
//      case ids => borrowHistoryRepo.dal.getLeastBorrowRankingFromBarcodes flatMap {
//        case a if a._2
//      }
//    }
    borrowHistoryRepo.dal.getBookBorrowRanking(request.start_time, request.milestone, true)
  }

  override def findReservationById(_id: String): Future[GetResponse] = {
    reservationHistoryRepo.dal.findById(_id)
  }

  override def findReservation(request: GetBookReservationListRequest): Future[SearchResponse] = {
    reservationHistoryRepo.dal.findAllDocs(request)
  }

  // Added by kuangyuan 4/17/2017
  override def findBookByBarcodeStr(user: Username, request: GetBookItemByBarcodesRequest): Future[SearchResponse] = {
    bookItemRepo.dal.findWithReferenceByBarcodeStr(request.barcode_string)
  }

  override def insertNewReaderLevel(user: Username, request: PostReaderLevelNewRequest): Future[UpsertResponse] = {
    for {
      result <- readerLevelNewRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          readerLevelNew <- ReaderLevelNew.toDomain(request)
          id <- readerLevelNewRepo.dal.id(readerLevelNew)
          result <- readerLevelNewRepo.dal.upsertDoc(id, readerLevelNew.jsonStringify)
        } yield result
        case false => Future.value(insertResponse("", s"name: '${request.name}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }

  override def findNewReaderLevels(user: Username, request: GetReaderLevelListRequest): Future[(SearchResponse)] = {
    readerLevelNewRepo.dal.findAllDocs(request)
  }


  override def findNewReaderLevelById(user: Username, request: GetReaderLevelByIdRequest): Future[(SearchResponse)] = {
    readerLevelNewRepo.dal.findById(request.id)
  }

  override def updateNewReaderLevelById(user: Username, request: PatchReaderLevelNewByIdRequest): Future[UpdateResponse] = {

    request.name match {
      case Some(name) =>
        for {
          result <- readerLevelNewRepo.dal.isUnique("name", name, request.id) flatMap {
            case true => readerLevelNewRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse("", s"name: '${name}' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => readerLevelNewRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  // Modified by kuangyuan 5/2/2017
  override def deleteNewReaderLevelById(user: Username, request: DeleteReaderLevelByIdRequest): Future[DeleteResponse] = {
    readerMemberRepo.dal.findIdsByNewLevelIds(Seq(request.id)) flatMap {
      case a if !a.isEmpty => Future((Status.NotAcceptable.code, valueJson("error", "New reader level is bound to readers, and cannot be deleted")))
      case b => readerLevelNewRepo.dal.deleteById(request.id)
    }
  }

  override def deleteNewReaderLevelBulk(user: Username, request: DeleteReaderLevelBulkRequest): Future[DeleteResponse] = {
    Future.collect(
      request.ids.map(
        id => readerMemberRepo.dal.findReaderMemberByNewLevelId(id) flatMap {
          case a if a._1 == 200 => Future(id)
          case b => Future(None)
        }
      )
    ) flatMap {
      case all => all.filter(_ != None) match {
        case empty_arr if empty_arr.length == 0 => readerLevelNewRepo.dal.deleteBulk(request.ids)
        case a => Future((Status.NotAcceptable.code, valueJsonArrayObject("reader levels bound to readers", "[" + a.map(ele => '"' + ele.toString + '"').mkString(",") + "]")))
      }
    }
  }

  // Added by kuangyuan 5/6/2017
  // Catalogue action involved
  override def bindPeriodicals(user: Username, request: PostBookItemBindRequest): Future[UpsertResponse] = {
    Future.collect(request.periodicalBarcodesStr.split("[,;]").map(
    b => bookItemRepo.dal.findByBarcode(b) flatMap {
      case Some((a, wrongCateg)) if wrongCateg.category != "期刊" => Future.value((Status.NotAcceptable.code, s"book ${wrongCateg.barcode} is in the wrong category, only periodicals can be bound"))
      case Some((a1, wrongPeriod)) if wrongPeriod.solicited_periodical != request.solicitedPeriodical => Future.value((Status.NotAcceptable.code, s"book ${wrongPeriod.barcode} doesn't belong to periodical ${request.solicitedPeriodical}, therefore cannot be bound"))
      case Some((id, book)) =>
        bookItemRepo.dal.updateActive(id, false)
      case None => Future.value((Status.NotFound.code, b))
    })) flatMap {
      case all => all.filter(a => Seq(404, 406).contains(a._1)) match {
        case empty_arr if empty_arr.length == 0 =>
          catalogue_handle(user, PostBookItemsRequest.toDomain(request))
        case a => Future.value((Status.NotAcceptable.code, valueJsonArrayObject("periodicals with barcodes not valid", "[" + a.map(ele => '"' + ele._2.toString + '"').mkString(",") + "]")))
      }
    }
  }

  // Added by kuangyuan 5/9/2017
  override def findSolicitedById(user: Username, request: GetSolicitedByIdRequest): Future[SearchResponse] = {
    solicitedRepo.dal.findById(request.id)
  }

  override def findSolicitedList(user: Username, request: GetSolicitedListRequest): Future[SearchResponse] = {
    solicitedRepo.dal.findAllDocs(request)
  }

  override def updateSolicitedById(user: Username, request: PatchSolicitedRequest): Future[UpdateResponse] = {
    request.title match {
      case Some(title) =>
        for {
          result <- solicitedRepo.dal.isUnique("title", title, request.id) flatMap {
            case true => solicitedRepo.dal.updateDoc(request.id, request.patchRequest)
            case false => Future.value(updateResponse(request.id, s"title: '${title}' exists", false, Status.NotAcceptable.code))
          }
        } yield result
      case None => solicitedRepo.dal.updateDoc(request.id, request.patchRequest)
    }
  }

  override def deleteSolicitedById(user: Username, request: DeleteSolicitedByIdRequest): Future[(StatusCode, Docs)] = {
    solicitedRepo.dal.deleteById(request.id)
  }

  override def deleteSolicitedBulk(user: Username, request: DeleteSolicitedBulkRequest): Future[(StatusCode, Docs)] = {
    solicitedRepo.dal.deleteBulk(request.ids)
  }

  override def postSolicited(user: Username, request: PostSolicitedRequest): Future[(StatusCode, Docs)] = {
    for {
      result <- solicitedRepo.dal.isUnique("title", request.title) flatMap {
        case true => for {
          solicitedPeriodical <- SolicitedPeriodical.toDomain(request)
          id <- solicitedRepo.dal.id(solicitedPeriodical)
          result <- solicitedRepo.dal.upsertDoc(id, solicitedPeriodical.jsonStringify)
        } yield result
        case false => Future.value(insertResponse("", s"title: '${request.title}' exists", false, Status.NotAcceptable.code))
      }
    } yield result
  }


  // TODO: data format not unified
  override def getReaderMemberDelayedDetail(user: Username, request: GetReaderMemberDelayedDetailRequest): Future[(StatusCode, Docs)] = {
    val now = Time.now.toString
    readerMemberRepo.dal.findById(request.id) flatMap {
      case n if n._1 == Status.NotFound.code => Future(n)
      case y =>
        readerLevelNewRepo.dal.findById(Json.parse(y._2).as[ReaderMember].level_new.get) flatMap {
          case a if a._1 == Status.NotFound.code => Future.value((a._1, valueJson("error", "new reader level not found")))
          case b =>  for {
            response <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("reader.id", request.id), rangeQuery("due_at").lte(now)), _n = Seq(existsQuery("_return"), matchPhraseQuery("fined", true)))
            penalty_rule_new = Json.parse(b._2).as[ReaderLevelNew].penalty_rule_new

            responseReturned <- borrowHistoryRepo.dal.findAll(
              _m = Seq(matchPhraseQuery("reader.id", request.id), existsQuery("_return"), scriptQuery("doc['due_at'].value < doc['_return.datetime'].value")),
              _n = Seq(matchPhraseQuery("fined", true))
            )
            expire_factor_book = penalty_rule_new.penalty_rule_book.expire_factor
            expire_factor_periodical = penalty_rule_new.penalty_rule_periodical.expire_factor
            late_lines = response.hits.map(a => Json.parse(a.sourceAsString).as[BorrowHistory])
              .map(b =>
                s""""${b.book.title}":
                   |{
                   |  "barcode": "${b.book.barcode}",
                   |  "borrowed_date": "${b._borrow.datetime}",
                   |  "due_date": "${b.due_at}",
                   |  "fine_due_today": ${
                  if (b.book.category == "普通图书")
                    Days.daysBetween(DateTime.parse(b.due_at), Time.now).getDays * expire_factor_book
                  else if(b.book.category == "期刊")
                    Days.daysBetween(DateTime.parse(b.due_at), Time.now).getDays * expire_factor_periodical
                }
                   |}
                 """.stripMargin)
            late_lines_returned = responseReturned.hits.map(a => Json.parse(a.sourceAsString).as[BorrowHistory])
              .map(b =>
                s""""${b.book.title}":
                   |{
                   |   "barcode": "${b.book.barcode}",
                   |   "borrowed_date": "${b._borrow.datetime}",
                   |   "due_date": "${b.due_at}",
                   |   "returned_date": "${b._return.get.datetime}",
                   |   "fine_due": ${
                  if (b.book.category == "普通图书")
                    Days.daysBetween(DateTime.parse(b.due_at), DateTime.parse(b._return.get.datetime)).getDays * expire_factor_book
                  else if (b.book.category == "期刊")
                    Days.daysBetween(DateTime.parse(b.due_at), DateTime.parse(b._return.get.datetime)).getDays * expire_factor_periodical
                }
                   |}
                """.stripMargin)

          } yield (200, valueJsonArrayObject("late_detail", s"{ ${late_lines.++:(late_lines_returned).mkString(",")} }"))
        }



    }
  }

  // added by kuangyuan 5/10/2017
  def getReaderMemberDelayedFine(id: String): Future[(StatusCode, Docs)] = {
    val now = Time.now.toString
    readerMemberRepo.dal.findById(id) flatMap {
      case a if a._1 == Status.NotFound.code => Future.value((Status.NotFound.code, valueJson("error", "Reader not found")))
      case b =>
        for {
          response <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("reader.id", id), rangeQuery("due_at").lte(now)), _n = Seq(existsQuery("_return"), matchPhraseQuery("fined", true)))
          lateDays = response.hits.map(rec => Days.daysBetween(DateTime.parse(Json.parse(rec.sourceAsString).as[BorrowHistory].due_at), Time.now))
          responseReturned <- borrowHistoryRepo.dal.findAll(
            _m = Seq(
              matchPhraseQuery("reader.id", id),
              existsQuery("_return"),
              scriptQuery("doc['due_at'].value < doc['_return.datetime'].value"
            )),
            _n = Seq(matchPhraseQuery("fined", true)))
          lateDaysReturned = responseReturned.hits.map(r => Days.daysBetween(
            DateTime.parse(Json.parse(r.sourceAsString).as[BorrowHistory].due_at),
            DateTime.parse(Json.parse(r.sourceAsString).as[BorrowHistory]._return.get.datetime)))


          expire_factor <- readerLevelRepo.dal.findById(Json.parse(b._2).as[ReaderMember].level).map(res =>
            Json.parse(res._2).as[ReaderLevel].penalty_rule.expire_factor
          )
        } yield (Status.Ok.code, BigDecimal(lateDays.++:(lateDaysReturned).map(a => a.getDays * expire_factor).sum).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toDouble.toString)
    }

  }

  def getDelayedFineFromBorrowHistoryString(h: String, returned: Boolean = true): Future[(StatusCode, Docs)] = {
    val borrowHistoryRecord = Json.parse(h).as[BorrowHistory]
    readerMemberRepo.dal.findById(borrowHistoryRecord.reader.id) flatMap {
      case no if no._1 == Status.NotFound.code => Future.value((Status.NotFound.code, valueJson("error", "reader not found")))
      case yes => for {
        penalty_rule <- readerLevelNewRepo.dal.findById(Json.parse(yes._2).as[ReaderMember].level_new.get).map(a => Json.parse(a._2).as[ReaderLevelNew].penalty_rule_new)
        category <- bookItemRepo.dal.findByBarcode(borrowHistoryRecord.book.barcode) flatMap {
          case None => Future.value(None)
          case Some((s, book)) => Future.value(book.category)
        }

        late_days = returned match {
          case false => Days.daysBetween(DateTime.parse(borrowHistoryRecord.due_at), DateTime.parse(Time.now.toString)).getDays()
          case true => Days.daysBetween(DateTime.parse(borrowHistoryRecord._return.get.datetime), DateTime.parse(borrowHistoryRecord.due_at)).getDays()
        }
      } yield category match {
        case None => (Status.NotFound.code, valueJson("error", "book not found"))
        case "普通图书" => (Status.Ok.code, valueJson("fine", BigDecimal(penalty_rule.penalty_rule_book.expire_factor * late_days).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toString))
        case "期刊" => (Status.Ok.code, valueJson("fine", BigDecimal(penalty_rule.penalty_rule_periodical.expire_factor * late_days).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toString))
      }
    }
  }

  // added by kuangyuan 5/12/2017
  override def findFineByBarcode(user: Username, request: GetBookFineByBarcodeRequest): Future[(StatusCode, Docs)] = {
    val now = Time.now.toString
    for {
      responseNotReturned <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("book.barcode", request.barcode), rangeQuery("due_at").lte(now)), _n = Seq(existsQuery("_return"), matchPhraseQuery("fined", true)))
      responseReturned <- borrowHistoryRepo.dal.findAll(
        _m = Seq(
          matchPhraseQuery("book.barcode", request.barcode),
          existsQuery("_return"),
          scriptQuery("doc['due_at'].value < doc['_return.datetime'].value"
          )),
        _n = Seq(matchPhraseQuery("fined", true)))
      r <- (responseNotReturned.hits.headOption, responseReturned.hits.headOption) match {
        case (None, None) => Future.value((Status.FailedDependency.code, valueJson("message", "reader has no borrow record with the book that should be fined")))
        case (Some(s), Some(t)) => Future.value((Status.InternalServerError.code, valueJson("error", "reader has conflict borrow records with the book that should be fined")))
        case (Some(s), _) =>
          getDelayedFineFromBorrowHistoryString(s.sourceAsString, false)
        case (_, Some(s)) =>
          getDelayedFineFromBorrowHistoryString(s.sourceAsString)
      }
    } yield r
  }

  override def deactivateBooks(user: Username, request: PatchBookItemsActiveStatusRequest): Future[(StatusCode, Docs)] = {
    patchBookItemsActiveStatusBulk(user, request.ids.get, false)
  }

  override def reactivateBooks(user: Username, request: PatchBookItemsActiveStatusRequest): Future[(StatusCode, Docs)] = {
    patchBookItemsActiveStatusBulk(user, request.ids.get, true)
  }

  override def updateBookItemsStackId(user: Username, request: PatchBookItemsStackIdRequest): Future[(StatusCode, Docs)] = {
    alterBookItemsStackId(user, request.ids.get, request.stackId)
  }

  override def renewPeriodical(user: Username, request: PatchSolicitedResolicitRequest): Future[(StatusCode, Docs)] = {
    solicitedRepo.dal.findById(request.id) flatMap {
      case a if a._1 == Status.Ok.code =>
        DateTime.parse(Json.parse(a._2).as[SolicitedPeriodical].solicited_date).year.get match {
          case n if n == Time.now.getYear => Future.value((Status.NotAcceptable.code, valueJson("error", "Periodical doesn't need to be resolicited")))
          case _ => solicitedRepo.dal.updateDoc(request.id, Map("solicited_date" -> Time.now.toString))
        }
      case b => Future.value(b)
    }
  }

  override def getLatePeriodical(user: Username, request: GetSolicitedLateRequest): Future[(StatusCode, Docs)] = {
    for {
      all_active <- solicitedRepo.dal.findAll(_m = Seq(rangeQuery("solicited_date").from(Time.now.minusDays(Time.now.getDayOfYear)).to(Time.now.toString)))
      r = all_active.hits.map(hit => Json.parse(hit.sourceAsString).as[SolicitedPeriodical]
        match {
          case w if w.period.equals("周刊") && w.last_serial < Time.now.getWeekOfWeekyear => (true, hit.id, w)
          case hm if hm.period.equals("半月刊") && (hm.last_serial < Time.now.getMonthOfYear * 2 - 1 || (hm.last_serial < Time.now.getMonthOfYear * 2 && Time.now.getDayOfMonth < 15)) => (true, hit.id, hm)
          case m if m.period.equals("月刊") && m.last_serial < Time.now.getMonthOfYear => (true, hit.id, m)
          case dm if dm.period.equals("双月刊") && (dm.last_serial < (Time.now.getMonthOfYear + 1) / 2) => (true, hit.id, dm)
          case hy if hy.period.equals("半年刊") && hy.last_serial < Time.now.getMonthOfYear / 6 + 1 => (true, hit.id, hy)
          case y if y.period.equals("年刊") && y.last_serial < 1 => (true, hit.id, y)
          case a => (false, hit.id, a)
        }
      ).toSeq.filter(_._1).map(r => SolicitedPeriodicalWithId.toDomain(r._2, r._3))
    } yield (Status.Ok.code, Json.stringify(Json.toJson(r)))
  }
}
