package com.shrfid.api.services

import com.shrfid.api._
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import com.twitter.util.Future

/**
  * Created by jiejin on 9/11/16.
  */
trait ElasticService {


  def updateBookInfoItem(id: String, item: String, oldId: Option[String]): Future[String]


  // branch
  // c
  def insertBookBranch(user: Username, request: PostBookBranchRequest): Future[UpsertResponse]

  // r
  def findBookBranchById(user: Username, request: GetBookBranchByIdRequest): Future[SearchResponse]

  def findBookBranches(request: GetBookBranchListRequest): Future[SearchResponse]

  // u
  def updateBookBranchById(user: Username, request: PatchBookBranchByIdRequest): Future[UpdateResponse]

  // d
  def deleteBookBranchById(user: Username, request: DeleteBookBranchByIdRequest): Future[DeleteResponse]

  def deleteBookBranchBulk(user: Username, request: DeleteBookBranchBulkRequest): Future[DeleteResponse]


  //stack
  // c
  def insertBookStack(user: Username, request: PostBookStackRequest): Future[UpsertResponse]

  // r
  def findBookStackById(user: Username, request: GetBookStackByIdRequest): Future[SearchResponse]

  def findBookStacks(request: GetBookStackListRequest): Future[SearchResponse]

  // u
  def updateBookStackById(user: Username, request: PatchBookStackByIdRequest): Future[UpdateResponse]

  // d
  def deleteBookStackById(user: Username, request: DeleteBookStackByIdRequest): Future[DeleteResponse]

  def deleteBookStackBulk(user: Username, request: DeleteBookStackBulkRequest): Future[DeleteResponse]

  // reference
  // c
  def insertReference(_id: String, _source: String): Future[UpsertResponse]

  // r
  def findReferenceById(_id: String): Future[GetResponse]

  def findReference(request: GetBookReferenceListRequest): Future[SearchResponse]

  // book item
  def preGenBookItems(user: Username, request: PostPreGenBookItemsRequest): Future[UpsertResponse]

  //def upsertBookItem(request: PostBookItemRequest, reference: String, title: Option[String], clc: Option[String]): Future[UpsertResponse]
  def findBookItemById(user: Username, request: GetBookItemByIdRequest): Future[SearchResponse]

  def findBookItems(user: Username, request: GetBookItemListRequest): Future[SearchResponse]

  // catalogue
  def catalogue(user: Username, request: PostBookItemRequest): Future[UpsertResponse]

  // vendor member
  // c
  def insertVendorMember(user: Username, request: PostVendorMemberRequest): Future[UpsertResponse]

  // r
  def findVendorMemberById(user: Username, request: GetVendorMemberByIdRequest): Future[SearchResponse]

  def findVendorMembers(user: Username, request: GetVendorMemberListRequest): Future[SearchResponse]

  // u
  def updateVendorMemberById(user: Username, request: PatchVendorMemberByIdRequest): Future[UpdateResponse]

  // d
  def deleteVendorMemberById(user: Username, request: DeleteVendorMemberByIdRequest): Future[DeleteResponse]

  def deleteVendorMemberBulk(user: Username, request: DeleteVendorMemberBulkRequest): Future[DeleteResponse]


  // vendor order
  // c
  def insertVendorOrder(user: Username, request: PostVendorOrderRequest): Future[UpsertResponse]

  def insertVendorOrderBulk(user: Username, request: PostVendorOrderBulkRequest): Future[UpsertResponse]

  // r
  def findVendorOrderById(user: Username, request: GetVendorOrderByIdRequest): Future[SearchResponse]

  def findVendorOrders(user: Username, request: GetVendorOrderListRequest): Future[SearchResponse]

  // u
  def updateVendorOrders(user: Username, request: PatchVendorOrderBulkRequest): Future[UpdateResponse]

  // d
  def deleteVendorOrderById(user: Username, request: DeleteVendorOrderByIdRequest): Future[DeleteResponse]

  def deleteVendorOrderBulk(user: Username, request: DeleteVendorOrderBulkRequest): Future[DeleteResponse]

  // reader level
  // c
  def insertReaderLevel(user: Username, request: PostReaderLevelRequest): Future[UpsertResponse]

  // r
  def findReaderLevelById(user: Username, request: GetReaderLevelByIdRequest): Future[SearchResponse]

  def findReaderLevels(user: Username, request: GetReaderLevelListRequest): Future[SearchResponse]

  // u
  def updateReaderLevelById(user: Username, request: PatchReaderLevelByIdRequest): Future[UpdateResponse]

  //d
  def deleteReaderLevelById(user: Username, request: DeleteReaderLevelByIdRequest): Future[DeleteResponse]

  def deleteReaderLevelBulk(user: Username, request: DeleteReaderLevelBulkRequest): Future[DeleteResponse]


  // reader group
  // c
  def insertReaderGroup(user: Username, request: PostReaderGroupRequest): Future[UpsertResponse]

  // r
  def findReaderGroupById(user: Username, request: GetReaderGroupByIdRequest): Future[SearchResponse]

  def findReaderGroups(user: Username, request: GetReaderGroupListRequest): Future[SearchResponse]

  // u
  def updateReaderGroup(user: Username, request: PatchReaderGroupByIdRequest): Future[UpdateResponse]

  // d
  // why update responses?
  def deleteReaderGroupById(user: Username, request: DeleteReaderGroupByIdRequest): Future[UpdateResponse]

  def deleteReaderGroupBulk(user: Username, request: DeleteReaderGroupBulkRequest): Future[UpdateResponse]


  // reader member
  // c
  def insertReaderMember(user: Username, request: PostReaderMemberRequest): Future[UpsertResponse]

  def insertReaderMemberBulk(user: Username, request: PostReaderMemberBulkRequest): Future[UpsertResponse]

  // r
  def findReaderMemberById(user: Username, request: GetReaderMemberByIdRequest): Future[SearchResponse]

  def findReaderMembers(user: Username, request: GetReaderMemberListRequest): Future[SearchResponse]

  // u
  def patchReaderMemberBulk(user: Username, request: PatchReaderMemberBulkRequest): Future[UpdateResponse]

  def patchReaderMemberById(user: Username, request: PatchReaderMemberByIdRequest): Future[UpdateResponse]


  // borrow
  def borrowItems(user: Username, request: PostReaderMemberBorrowItemsRequest): Future[(StatusCode, Docs)]

  def renewItems(user: Username, request: PostReaderMemberRenewItemsRequest): Future[(StatusCode, Docs)]

  def findBorrowRecords(user: Username, request: GetReaderMemberBorrowRecordListRequest): Future[SearchResponse]

  // return
  def returnBooks(user: Username, request: PostBookItemsReturnRequest): Future[(StatusCode, Docs)]

  // reserve
  def reserveBooks(user: Username, request: PostReaderMemberReserveItemsRequest): Future[UpsertResponse]

  def reserveBook(user: Username, request: PostReaderMemberReserveItemRequest): Future[UpsertResponse]

  // fines
  def fineReaderMember(user: Username, request: PatchReaderMemberByIdRequest): Future[UpdateResponse]
}
