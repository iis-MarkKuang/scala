package com.shrfid.api.controllers

import java.sql.Date
import java.text.SimpleDateFormat

import com.shrfid.api._
import com.google.inject.Inject
import com.shrfid.api.domains.book.BookReference
import com.shrfid.api.domains.reader.{BorrowRule => DomainBorrowRule, PenaltyRule => DomainPenaltyRule}
import com.shrfid.api.services.ElasticService
import com.twitter.finatra.thrift.Controller
import com.elastic_service.elasticServer.ElasticServerThrift._
import com.elastic_service.elasticServer._
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Created by kuang on 2017/3/18.
  */
class ThriftController @Inject() (
  elasticService: ElasticService,
  responseBuilder: ResponseBuilder
)
  extends Controller
  with ElasticServerThrift.BaseServiceIface {

  override val increment = handle(Increment) { args: Increment.Args =>
    Future(args.a + 1)
  }

  override val updateBookInfoItem = handle(UpdateBookInfoItem) { args: UpdateBookInfoItem.Args =>
    elasticService.updateBookInfoItem(args.id, args.item, args.oldId)
  }

  override val insertBookBranch = handle(InsertBookBranch) { args: InsertBookBranch.Args =>
    elasticService.insertBookBranch(args.user, PostBookBranchRequest.toDomain(
      args.request
    )).map(a => a._2)
  }

  override val findBookBranchById = handle(FindBookBranchById) { args: FindBookBranchById.Args =>
    elasticService.findBookBranchById(args.user, GetBookBranchByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  override val findBookBranches = handle(FindBookBranches) { args: FindBookBranches.Args =>
    elasticService.findBookBranches(GetBookBranchListRequest(
      args.request.authorization,
      args.request.limit,
      args.request.offset,
      args.request.id,
      args.request.name,
      args.request.isActive,
      args.request.isRoot,
      args.request.ordering
    )).map(a => a._2)
  }

  override val updateBookBranchById = handle(UpdateBookBranchById) { args: UpdateBookBranchById.Args =>
    elasticService.updateBookBranchById(args.user, PatchBookBranchByIdRequest(
      args.request.authorization,
      args.request.id,
      args.request.name,
      args.request.isActive,
      args.request.isRoot,
      args.request.description,
      args.request.datetime
    )).map(a => a._2)
  }

  override val deleteBookBranchById = handle(DeleteBookBranchById) { args: DeleteBookBranchById.Args =>
    elasticService.deleteBookBranchById(args.user, DeleteBookBranchByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  override val deleteBookBranchBulk = handle(DeleteBookBranchBulk) { args: DeleteBookBranchBulk.Args =>
    elasticService.deleteBookBranchBulk(args.user, DeleteBookBranchBulkRequest(
      args.request.authorization,
      args.request.ids
    )).map(a => a._2)
  }

  override val insertBookStack = handle(InsertBookStack) { args: InsertBookStack.Args =>
    elasticService.insertBookStack(args.user, PostBookStackRequest(
      args.request.authorization,
      args.request.name,
      args.request.isActive,
      args.request.branchId,
      args.request.description
    )).map(a => a._2)
  }

  override val findBookStackById = handle(FindBookStackById) { args: FindBookStackById.Args =>
    elasticService.findBookStackById(args.user, GetBookStackByIdRequest(
      args.request.authorization,
      args.request.id
    )).map{ x => x._2}
  }

  override val findBookStacks = handle(FindBookStacks) { args: FindBookStacks.Args =>
    elasticService.findBookStacks(GetBookStackListRequest(
      args.request.authorization,
      args.request.limit,
      args.request.offset,
      args.request.id,
      args.request.name,
      args.request.branch,
      args.request.isActive,
      args.request.ordering
    )).map(a => a._2)
  }

  override val updateBookStackById = handle(UpdateBookStackById) { args: UpdateBookStackById.Args =>
    elasticService.updateBookStackById(args.user, PatchBookStackByIdRequest(
      args.request.authorization,
      args.request.id,
      args.request.name,
      args.request.isActive,
      args.request.branchId,
      args.request.description,
      args.request.datetime
    )).map(a => a._2)
  }

  override val deleteBookStackById = handle(DeleteBookStackById) { args: DeleteBookStackById.Args =>
    elasticService.deleteBookStackById(args.user, DeleteBookStackByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  override val deleteBookStackBulk = handle(DeleteBookStackBulk) { args: DeleteBookStackBulk.Args =>
    elasticService.deleteBookStackBulk(args.user, DeleteBookStackBulkRequest(
      args.request.authorization,
      args.request.ids
    )).map(a => a._2)
  }

  override val insertReference = handle(InsertReference) { args: InsertReference.Args =>
    elasticService.insertReference(args._id, args._source)
      .map(a => a._2)
  }

  override val findReferenceById = handle(FindReferenceById) { args: FindReferenceById.Args =>
    elasticService.findReferenceById(args._id)
      .map(a => a._2)
  }

  override val findReference = handle(FindReference) { args: FindReference.Args =>
    elasticService.findReference(GetBookReferenceListRequest(
      args.request.authorization,
      args.request.limit,
      args.request.offset,
      args.request.id,
      args.request.keyword,
      args.request.author,
      args.request.title,
      args.request.isbn,
      args.request.publisher,
      args.request.clc,
      args.request.publishYear,
      args.request.topic,
      args.request.ordering
    )).map(a => a._2)
  }

  override val preGenBookItems = handle(PreGenBookItems) { args: PreGenBookItems.Args =>
    elasticService.preGenBookItems(args.user, PostPreGenBookItemsRequest(
      args.request.authorization,
      args.request.categoryId,
      args.request.stackId,
      args.request.quantity
    )).map(a => a._2)
  }

  override val findBookItemById = handle(FindBookItemById) { args: FindBookItemById.Args =>
    elasticService.findBookItemById(args.user, GetBookItemByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  override val findBookItems = handle(FindBookItems) { args: FindBookItems.Args =>
    elasticService.findBookItems(args.user, GetBookItemListRequest(
      args.request.authorization,
      args.request.limit,
      args.request.offset,
      args.request.id,
      args.request.reference,
      args.request.title,
      args.request.barcode,
      args.request.rfid,
      args.request.categoryId,
      args.request.stackId,
      args.request.clc,
      args.request.isAvailable,
      args.request.isActive,
      args.request.ordering
    )).map(a => a._2)
  }

  override val catalogue = handle(Catalogue) { args: Catalogue.Args =>
    elasticService.catalogue(args.user, PostBookItemRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6.asInstanceOf[Option[BookReference]],
      args.request._7
    )).map(a => a._2)
  }

  override val insertVendorMember = handle(InsertVendorMember) { args: InsertVendorMember.Args =>
    elasticService.insertVendorMember(args.user, PostVendorMemberRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  override val findVendorMemberById = handle(FindVendorMemberById) { args: FindVendorMemberById.Args =>
    elasticService.findVendorMemberById(args.user, GetVendorMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val findVendorMembers = handle(FindVendorMembers) { args: FindVendorMembers.Args =>
    elasticService.findVendorMembers(args.user, GetVendorMemberListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  override val updateVendorMemberById = handle(UpdateVendorMemberById) { args: UpdateVendorMemberById.Args =>
    elasticService.updateVendorMemberById(args.user, PatchVendorMemberByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6
    )).map(a => a._2)
  }

  override val deleteVendorMemberById = handle(DeleteVendorMemberById) { args: DeleteVendorMemberById.Args =>
    elasticService.deleteVendorMemberById(args.user, DeleteVendorMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val deleteVendorMemberBulk = handle(DeleteVendorMemberBulk) { args: DeleteVendorMemberBulk.Args =>
    elasticService.deleteVendorMemberBulk(args.user, DeleteVendorMemberBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val insertVendorOrder = handle(InsertVendorOrder) { args: InsertVendorOrder.Args =>
    elasticService.insertVendorOrder(args.user, PostVendorOrderRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
//      args.request._11
      Date.valueOf(args.request._11)
    )).map(a => a._2)
  }

  override val insertVendorOrderBulk = handle(InsertVendorOrderBulk) { args: InsertVendorOrderBulk.Args =>
    elasticService.insertVendorOrderBulk(args.user, PostVendorOrderBulkRequest(
      args.request._1,
      // to be tested
      args.request._2.asInstanceOf[Seq[PutVendorOrderRow]]
    )).map(a => a._2)
  }

  override val findVendorOrderById = handle(FindVendorOrderById) { args: FindVendorOrderById.Args =>
    elasticService.findVendorOrderById(args.user, GetVendorOrderByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val findVendorOrders = handle(FindVendorOrders) { args: FindVendorOrders.Args =>
    elasticService.findVendorOrders(args.user, GetVendorOrderListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10
    )).map(a => a._2)
  }

  override val updateVendorOrders = handle(UpdateVendorOrders) { args: UpdateVendorOrders.Args =>
    elasticService.updateVendorOrders(args.user, PatchVendorOrderBulkRequest(
      args.request._1,
      args.request._2.asInstanceOf[Seq[PatchVendorOrderRow]]
    ))
      .map(a => a._2)
  }

  override val deleteVendorOrderById = handle(DeleteVendorOrderById) { args: DeleteVendorOrderById.Args =>
    elasticService.deleteVendorOrderById(args.user, DeleteVendorOrderByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val deleteVendorOrderBulk = handle(DeleteVendorOrderBulk) { args: DeleteVendorOrderBulk.Args =>
    elasticService.deleteVendorOrderBulk(args.user, DeleteVendorOrderBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val insertReaderLevel = handle(InsertReaderLevel) { args: InsertReaderLevel.Args =>
    elasticService.insertReaderLevel(args.user, PostReaderLevelRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4.asInstanceOf[DomainBorrowRule],
      args.request._5.asInstanceOf[DomainPenaltyRule],
      args.request._6
    )).map(a => a._2)
  }

  override val findReaderLevelById = handle(FindReaderLevelById) { args: FindReaderLevelById.Args =>
    elasticService.findReaderLevelById(args.user, GetReaderLevelByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val findReaderLevels = handle(FindReaderLevels) { args: FindReaderLevels.Args =>
    elasticService.findReaderLevels(args.user, GetReaderLevelListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  override val updateReaderLevelById = handle(UpdateReaderLevelById) { args: UpdateReaderLevelById.Args =>
    elasticService.updateReaderLevelById(args.user, PatchReaderLevelByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5.asInstanceOf[Option[BorrowRule]],
      args.request._6.asInstanceOf[Option[PenaltyRule]],
      args.request._7,
      args.request._8
    )).map(a => a._2)
  }

  override val deleteReaderLevelById = handle(DeleteReaderLevelById) { args: DeleteReaderLevelById.Args =>
    elasticService.deleteReaderLevelById(args.user, DeleteReaderLevelByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val deleteReaderLevelBulk = handle(DeleteReaderLevelBulk) { args: DeleteReaderLevelBulk.Args =>
    elasticService.deleteReaderLevelBulk(args.user, DeleteReaderLevelBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val insertReaderGroup = handle(InsertReaderGroup) { args: InsertReaderGroup.Args =>
    elasticService.insertReaderGroup(args.user, PostReaderGroupRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  override val findReaderGroupById = handle(FindReaderGroupById) { args: FindReaderGroupById.Args =>
    elasticService.findReaderGroupById(args.user, GetReaderGroupByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val findReaderGroups = handle(FindReaderGroups) { args: FindReaderGroups.Args =>
    elasticService.findReaderGroups(args.user, GetReaderGroupListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  override val updateReaderGroup = handle(UpdateReaderGroup) { args: UpdateReaderGroup.Args =>
    elasticService.updateReaderGroup(args.user, PatchReaderGroupByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5
    )).map(a => a._2)
  }

  override val deleteReaderGroupById = handle(DeleteReaderGroupById) { args: DeleteReaderGroupById.Args =>
    elasticService.deleteReaderGroupById(args.user, DeleteReaderGroupByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val deleteReaderGroupBulk = handle(DeleteReaderGroupBulk) { args: DeleteReaderGroupBulk.Args =>
    elasticService.deleteReaderGroupBulk(args.user, DeleteReaderGroupBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val insertReaderMember = handle(InsertReaderMember) { args: InsertReaderMember.Args =>
    elasticService.insertReaderMember(args.user, PostReaderMemberRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
      args.request._11,
      args.request._12,
      args.request._13,
      args.request._14,
      new DateTime(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(args.request._15))
    )).map(a => a._2)
  }

  override val insertReaderMemberBulk = handle(InsertReaderMemberBulk) { args: InsertReaderMemberBulk.Args =>
    elasticService.insertReaderMemberBulk(args.user, PostReaderMemberBulkRequest(
      args.request._1,
      args.request._2.asInstanceOf[Seq[ReaderMemberInsertion]]
    )).map(a => a._2)
  }

  override val findReaderMemberById = handle(FindReaderMemberById) { args: FindReaderMemberById.Args =>
    elasticService.findReaderMemberById(args.user, GetReaderMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  override val findReaderMembers = handle(FindReaderMembers) { args: FindReaderMembers.Args =>
    elasticService.findReaderMembers(args.user, GetReaderMemberListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
      args.request._11,
      args.request._12,
      args.request._13,
      args.request._14,
      args.request._15,
      args.request._16,
      args.request._17,
      args.request._18,
      args.request._19,
      args.request._20,
      args.request._21
    )).map(a => a._2)
  }

  override val patchReaderMemberBulk = handle(PatchReaderMemberBulk) { args: PatchReaderMemberBulk.Args =>
    elasticService.patchReaderMemberBulk(args.user, PatchReaderMemberBulkRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5
    )).map(a => a._2)
  }

  override val patchReaderMemberById = handle(PatchReaderMemberById) { args: PatchReaderMemberById.Args =>
    elasticService.patchReaderMemberById(args.user, PatchReaderMemberByIdRequest.toDomain(args.request)).map(a => a._2)
  }

  override val borrowItems = handle(BorrowItems) { args: BorrowItems.Args =>
    elasticService.borrowItems(args.user, PostReaderMemberBorrowItemsRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  override val renewItems = handle(RenewItems) { args: RenewItems.Args =>
    elasticService.renewItems(args.user, PostReaderMemberRenewItemsRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  override val findBorrowRecords = handle(FindBorrowRecords) { args: FindBorrowRecords.Args =>
    elasticService.findBorrowRecords(args.user, GetReaderMemberBorrowRecordListRequest(
      args.request._1,
      args.request._2,
      args.request._3.asInstanceOf[Option[String]],
      args.request._4,
      args.request._5,
      args.request._6
    )).map(a => a._2)
  }

  override val returnBooks = handle(ReturnBooks) { args: ReturnBooks.Args =>
    elasticService.returnBooks(args.user, PostBookItemsReturnRequest(
      args.request._1,
      args.request._2,
      args.request._3
    )).map(a => a._2)
  }
}
