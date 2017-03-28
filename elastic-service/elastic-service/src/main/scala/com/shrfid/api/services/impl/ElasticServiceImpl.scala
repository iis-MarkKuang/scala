package com.shrfid.api.services.impl

import javax.inject.{Inject, Singleton}

import com.shrfid.api.TwitterFutureOps._
import com.shrfid.api._
import com.shrfid.api.domains.book._
import com.shrfid.api.domains.reader.{ReaderGroup, ReaderLevel, ReaderMember, ReaderMemberIsSuspend}
import com.shrfid.api.domains.vendor.{VendorMember, VendorMemberWithId, VendorOrder, VendorOrderNested}
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import com.shrfid.api.persistence.elastic4s._
import com.shrfid.api.services.ElasticService
import com.sksamuel.elastic4s.ElasticDsl._
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import org.joda.time.DateTime
import play.api.libs.json.Json

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
                                   readerGroupRepo: ReaderGroupRepo,
                                   readerMemberRepo: ReaderMemberRepo,
                                   borrowHistoryRepo: BorrowHistoryRepo) extends ElasticService {


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
  override def insertBookBranch(user: Username, request: PostBookBranchRequest): Future[UpsertResponse] = {
    for {
      result <- bookBranchRepo.dal.isUnique("name", request.name) flatMap {
        case true => for {
          bookBranch <- BookBranch.toDomain(request)
          id <- bookBranchRepo.dal.id(bookBranch)
          result <- bookBranchRepo.dal.upsertDoc(id, bookBranch.jsonStringify)
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
    bookBranchRepo.dal.deleteById(request.id)
  }

  override def deleteBookBranchBulk(user: Username, request: DeleteBookBranchBulkRequest): Future[DeleteResponse] = {
    bookBranchRepo.dal.deleteBulk(request.ids)
  }


  // stack
  override def insertBookStack(user: Username, request: PostBookStackRequest): Future[UpsertResponse] = {
    Future.collect(Seq(bookStackRepo.dal.isUnique("name", request.name), bookBranchRepo.dal.isExists(request.branchId))) flatMap {
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

  override def updateBookStackById(user: Username, request: PatchBookStackByIdRequest): Future[UpsertResponse] = {
    //println(request.patchRequest)
    (request.name, request.branchId) match {
      case (Some(name), Some(branchId)) =>
        Future.collect(Seq(bookStackRepo.dal.isUnique("name", name, request.id), bookBranchRepo.dal.isExists(branchId))) flatMap {
          case Seq(true, true) => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
          case Seq(false, true) => Future.value(updateResponse(request.id, s"name: '${name}' exists", false, Status.NotAcceptable.code))
          case Seq(true, false) => Future.value(updateResponse(request.id, s"branch_id: '${branchId}' does not exist", false, Status.NotAcceptable.code))
          case Seq(false, false) => Future.value(updateResponse(request.id, s"""name: '${name}' exists, branch_id: '${branchId}' does not exist""", false, Status.NotAcceptable.code))
        }
      case (Some(name), None) => bookStackRepo.dal.isUnique("name", name, request.id) flatMap {
        case true => bookStackRepo.dal.updateDoc(request.id, request.patchRequest)
        case false => Future.value(updateResponse(request.id, s"name: '${name}' exists", false, Status.NotAcceptable.code))
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
    bookStackRepo.dal.deleteById(request.id)
  }

  override def deleteBookStackBulk(user: Username, request: DeleteBookStackBulkRequest): Future[(StatusCode, Docs)] = {
    bookStackRepo.dal.deleteBulk(request.ids)
  }

  // reference
  override def insertReference(_id: String, _source: String): Future[UpsertResponse] = {
    bookReferenceRepo.dal.upsertDoc(_id, _source)
  }

  override def findReferenceById(_id: String): Future[GetResponse] = {
    bookReferenceRepo.dal.findById(_id)
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

  private def catalogue(request: PostBookItemRequest, reference: String, title: Option[String], clc: Option[String], user: Username): Future[UpsertResponse] = {
    val titleFixed = title match {
      case Some(t) => t
      case None => request.info.get.题名与责任者.正题名
    }
    val clcFixed = clc match {
      case Some(c) => c.take(Config.clcLength)
      case None => request.info.get.中国图书馆图书分类法分类号 //.take(Config.clcLength).replaceAll("[^a-zA-Z0-9]", "")
    }

    for {
      bookIndex <- bookItemRepo.dal.findNextBookIndex(clcFixed)
      result <- bookItemRepo.dal.catalogue(request.barcode, request.rfid.getOrElse(""), reference, titleFixed, request.stackId,
        clcFixed, bookIndex, user)
    } yield result

  }

  override def catalogue(user: Username, request: PostBookItemRequest): Future[UpsertResponse] = {
    def findRefTitleClc = request.reference match {
      case Some(ref) if ref != "" => for {(t, c) <- bookReferenceRepo.dal.findTitleById(ref)} yield (ref, t, c)
      case _ =>
        val bookRef = BookReference.withKeyword(request.info.get)
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
    readerLevelRepo.dal.deleteById(request.id)
  }

  override def deleteReaderLevelBulk(user: Username, request: DeleteReaderLevelBulkRequest): Future[DeleteResponse] = {
    readerLevelRepo.dal.deleteBulk(request.ids)
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

  override def insertReaderMemberBulk(user: Username, request: PostReaderMemberBulkRequest): Future[UpsertResponse] = {
    for {
      response <- Future.collect(request.data.map(i =>
        for {
          result <- readerLevelRepo.dal.findDepositById(i.levelId) flatMap {
            case Some(deposit) => for {
              reader <- ReaderMember.toDomain(i, deposit)
              id <- readerMemberRepo.dal.id(reader)
              result <- readerMemberRepo.dal.findById(id) flatMap {
                case (Status.NotFound.code, _) => readerMemberRepo.dal.insertDoc(id, reader.jsonStringify)
                case (_, _) => Future.value((Status.NotAcceptable.code, s"""{"id": "", "result":"reader already exists", "created": false}"""))
              }
            } yield result
            case None => Future.value((Status.NotAcceptable.code, s"""{"id": "", "result":" reader level: ${i.levelId} does not exist", "created": false}"""))
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

  override def patchReaderMemberById(user: Username, request: PatchReaderMemberByIdRequest): Future[UpdateResponse] = {
    readerMemberRepo.dal.updateDoc(request.id, request.patchRequest)
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

  def reserveBookResponse(id: String, result: String, reserved: Boolean, status: Int = 200) =
    (status, s"""{"id": "$id",  "result": "$result" , "reserved" : $reserved}""")

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

  private def borrow(version: Long, readerMember: ReaderMember, id: String, day: Int, bookBarcodes: Seq[String], location: String): Future[(StatusCode, Docs)] = {
    Future.collect(bookBarcodes.map(barcode => bookItemRepo.dal.findByIdAsOptString(barcode) flatMap {
      case None =>
        Future.value(borrowBookResponse("", s"${barcode}不存在", false))
      case Some(book) => val bookItem = Json.parse(book._2).as[Book]
        (bookItem.is_available, bookItem.is_active) match {
          case (_, false) =>
            Future.value(borrowBookResponse("", "非流通图书, 请将其归还至管理员处, 谢谢!", false))
          case (true, true) => for {
            _ <- bookItemRepo.dal.updateAvailability(barcode, false)
            borrowHistory = BorrowHistory.toDomain("borrow", readerMember.toReaderInfo(id), bookItem, TimeLocation(Time.now.toString, location), None, None, day)
            result <- borrowHistoryRepo.dal.insertDocReturnId(borrowHistory.jsonStringify)
          } yield result match {
            case (Status.Created.code, s, id) =>
              borrowBookResponse(id, s"成功借阅, 应还时间为${borrowHistory.due_at}", true)
            case (other, s, _) => borrowBookResponse("", s, false)
          }
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
            for {result <- borrowHistoryRepo.dal.renewAt(bhId, TimeLocation(now, location), dueAt, now)} yield result._1 match {
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
        case Some(id) => for {
          ret <- borrowHistoryRepo.dal.returnAt(id, TimeLocation(now, location), now)
          ava <- bookItemRepo.dal.updateAvailability(b, true, now)
        } yield (ret._1, ava._1) match {
          case (200, 200) => returnBookResponse(id, s"成功归还, 归还时间为${now}", true)
          case (_, _) => returnBookResponse("", "内部错误, 请联系管理员", false)
        }
      }
    } yield result
    )).map(a => (200, "[" + a.map(_._2).mkString(",") + "]"))
  }

  private def _notifyReserve(reader_id: String, barcode: String) = {
    // call phone api to send out text messages
  }

  // reserve
  override def reserveBooks(user: Username, request: PostReaderMemberReserveItemsRequest): Future[(UpsertResponse)] = {
    _reserveBulk(user, request.bookBarcodes, request.location)
  }

  override def reserveBook(user: Username, request: PostReaderMemberReserveItemRequest): Future[(UpsertResponse)] = {
    _reserve(user, request.bookBarcode, request.location)
  }

  private def _reserve(user: Username, bookBarcode: String, location: String) = {
    var bookBarcodes = Seq[String]()
    bookBarcodes = bookBarcodes :+ bookBarcode
    _reserveBulk(user, bookBarcodes, location)
  }

  private def _reserveBulk(user: Username, bookBarcodes: Seq[String], location: String) = {
    Future.collect(bookBarcodes.map(b => for {
      history <- borrowHistoryRepo.dal.findAll(_m = Seq(matchPhraseQuery("book.barcode", b)), _n = Seq(existsQuery("_return")))
      now = Time.now.toString
      result <- history.ids.headOption match {
//        case None => Future.value(reserveBookResponse("", s"图书在馆，请直接到馆借书", true))
        case None => for {
          ava <- bookItemRepo.dal.isAvailable(b)
        } yield (ava) match {
          case true => reserveBookResponse("", s"图书在馆，请直接到馆借书", true)
          case false => reserveBookResponse(b, s"成功预约，您将在该书可以借阅时收到通知", true)
//            val f = bookItemRepo.dal.updateAvailability(b, false, now)
//            f onSuccess (
//              a => if(a._2 != 200)
//                reserveBookResponse("", "内部错误，请联系管理员", false)
//            ) onFailure (
//              ex => reserveBookResponse("", ex.getMessage, false)
//            )
        }
        case Some(id) => for {
//          availability <- bookItemRepo.dal.findAll(_m = Seq(matchPhraseQuery("_id", id)), _n = Seq())
          res <- borrowHistoryRepo.dal.reserveAt(id,TimeLocation(now, location), Time.nextNdays(7).toString, Time.now.toString)
          ava <- bookItemRepo.dal.updateAvailability(b, false, now)
        } yield (res._1, ava._1) match {
          case (200, 200) => reserveBookResponse(id, s"成功预约，您将在该书可以借阅时收到通知", true)
          case (_, _) => reserveBookResponse("", "内部错误，请联系管理员", false)
        }
      }
    } yield result
    )).map(a => (200, "[" + a.map(_._2).mkString(",") + "]"))
  }
}
