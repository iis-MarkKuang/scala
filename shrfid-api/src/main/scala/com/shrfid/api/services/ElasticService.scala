package com.shrfid.api.services

import com.shrfid.api._
import com.shrfid.api.controllers.Permission.PermissionCode
import com.shrfid.api.http.Elastic.book.{PatchBookItemsActiveStatusRequest, PatchBookItemsStackIdRequest}
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.book.reservation.GetBookReservationListRequest
import com.shrfid.api.http.Elastic.book.solicitedPeriodicals._
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.http.Elastic.data._
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
  def catalogue(user: Username, request: PostBookItemsRequest): Future[UpsertResponse]

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
  def deleteReaderGroupById(user: Username, request: DeleteReaderGroupByIdRequest): Future[UpdateResponse]

  def deleteReaderGroupBulk(user: Username, request: DeleteReaderGroupBulkRequest): Future[UpdateResponse]


  // reader member
  // c
  def insertReaderMember(user: Username, request: PostReaderMemberRequest): Future[UpsertResponse]

  def insertReaderMemberBulk(user: Username, request: PostReaderMemberBulkRequest): Future[UpsertResponse]

  // r
  def findReaderMemberById(user: Username, request: GetReaderMemberByIdRequest): Future[SearchResponse]

  // added by kuangyuan 5/4/2017
  def findReaderMemberByBarcode(user: Username, request: GetReaderMemberByBarcodeRequest): Future[SearchResponse]

  def findReaderMembers(user: Username, request: GetReaderMemberListRequest): Future[SearchResponse]

  // u
  def patchReaderMemberBulk(user: Username, request: PatchReaderMemberBulkRequest): Future[UpdateResponse]

  def patchReaderMemberById(user: Username, request: PatchReaderMemberByIdRequest): Future[UpdateResponse]


  // borrow
  def borrowItems(user: Username, request: PostReaderMemberBorrowItemsRequest): Future[(StatusCode, Docs)]

  def renewItems(user: Username, request: PostReaderMemberRenewItemsRequest): Future[(StatusCode, Docs)]


  def reservationItems(user: Username, request: PostReaderMemberReservationItemsRequest): Future[(StatusCode, Docs)]

  def findBorrowRecords(user: Username, request: GetReaderMemberBorrowRecordListRequest): Future[SearchResponse]

  // return
  def returnBooks(user: Username, request: PostBookItemsReturnRequest): Future[(StatusCode, Docs)]



  //leixx,2017-4-6
  def updateBookItemBusinessAndPhysicalState(user:Username,request:PatchBookPhysicalStackByBarCodeRequest):Future[UpdateResponse]

  //leixx,2017-4-8
  //对图书架位信息按设定值进行初始化
  def initBookItemShelf(user:Username,request:PatchBookPhysicalStackByBarCodeRequest):Future[UpdateResponse]

  //leixx,2017-4-8
  //对图书业务和物理在架状态信息进行查询
  def getBookItemBusinessAndPhysicalState(user:Username,request:GetBookBusinessAndPhysicalStateByBarCodeRequest):Future[SearchResponse]

  // add by yuan kuang 4/12/2017
  // statistics
  def findReaderBorrowRanking(user: Username, request: GetReaderBorrowRankingRequest): Future[SearchResponse]

  def findBookBorrowRanking(user: Username, request: GetBookBorrowRankingRequest): Future[SearchResponse]

  def findBookFlowStatistics(user: Username, request: GetBookFlowStatisticsRequest): Future[SearchResponse]

  def findCategoryFlowStatistics(user: Username, request: GetCategoryFlowStatisticsRequest): Future[SearchResponse]

  def findStockStatistics(user: Username, request: GetStockStatisticsRequest): Future[SearchResponse]

  def findBookLeastBorrowStats(user: Username, request: GetBookLeastBorrowStatsRequest): Future[SearchResponse]

  // fine
  def getReaderDelayedFine(user: Username, request: GetReaderMemberDelayedFineRequest): Future[(StatusCode, Docs)]

  def getReaderLostFine(user: Username, request: GetReaderMemberLostFineRequest): Future[(StatusCode, Docs)]

  def updateReaderCredit(user: Username, request: UpdateReaderMemberCreditByIdRequest): Future[(StatusCode, Docs)]

  def findReservationById(_id: String): Future[GetResponse]

  def findReservation(request: GetBookReservationListRequest): Future[SearchResponse]

  // Added by kuangyuan 4/17/2017
  def findBookByBarcodeStr(user: Username, request: GetBookItemByBarcodesRequest): Future[SearchResponse]

  def insertNewReaderLevel(user: Username, request: PostReaderLevelNewRequest): Future[UpsertResponse]

  def findNewReaderLevels(user: Username, request: GetReaderLevelListRequest): Future[SearchResponse]

  def findNewReaderLevelById(user: Username, request: GetReaderLevelByIdRequest): Future[SearchResponse]

  def updateNewReaderLevelById(user: Username, request: PatchReaderLevelNewByIdRequest): Future[UpdateResponse]

  def deleteNewReaderLevelById(user: Username, request: DeleteReaderLevelByIdRequest): Future[DeleteResponse]

  def deleteNewReaderLevelBulk(user: Username, request: DeleteReaderLevelBulkRequest): Future[DeleteResponse]

  // added by kuangyuan 4/19/2017
  def borrowItemsNew(user: Username, request: PostReaderMemberBorrowItemsRequest): Future[(StatusCode, Docs)]

  def renewItemsNew(user: Username, request: PostReaderMemberRenewItemsRequest): Future[(StatusCode, Docs)]

  def reservationItemsNew(user: Username, request: PostReaderMemberReservationItemsRequest): Future[(StatusCode, Docs)]

  def updateBookItemsShelfStatusByDir(user: Username, request: PatchBookPhysicalShelfStatusViaFTPRequest): Future[(StatusCode, Docs)]

  // added by kuangyuan 4/20/2017
  def getCheckProgress(user: Username, request: GetCheckProgressRequest): Future[(StatusCode, Docs)]

  def getBookItemShelfStatusByDir(user: Username, request: GetBookPhysicalShelfStatusViaFTPRequest): Future[(StatusCode, Docs)]

  // added by kuangyuan 5/6/2017
  def bindPeriodicals(user: Username, request: PostBookItemBindRequest): Future[(StatusCode, Docs)]

  // added by kuangyuan 5/9/2017
  def findSolicitedById(user: Username, request: GetSolicitedByIdRequest): Future[SearchResponse]

  def findSolicitedList(user: Username, request: GetSolicitedListRequest): Future[SearchResponse]

  def updateSolicitedById(user: Username, request: PatchSolicitedRequest): Future[UpdateResponse]

  def deleteSolicitedById(user: Username, request: DeleteSolicitedByIdRequest): Future[DeleteResponse]

  def deleteSolicitedBulk(user: Username, request: DeleteSolicitedBulkRequest): Future[DeleteResponse]

  def postSolicited(user: Username, request: PostSolicitedRequest): Future[UpsertResponse]

  def getReaderMemberDelayedDetail(user: Username, request: GetReaderMemberDelayedDetailRequest): Future[(StatusCode, Docs)]

  // Added by kuangyuan 5/12/2017
  def findFineByBarcode(user: Username, request: GetBookFineByBarcodeRequest): Future[(StatusCode, Docs)]

  // Added by kuangyuan 5/13/2017
  def deactivateBooks(user: Username, request: PatchBookItemsActiveStatusRequest): Future[(StatusCode, Docs)]

  def reactivateBooks(user: Username, request: PatchBookItemsActiveStatusRequest): Future[(StatusCode, Docs)]

  def updateBookItemsStackId(user: Username, request: PatchBookItemsStackIdRequest): Future[(StatusCode, Docs)]

  // Added by kuangyuan 5/23/2017
  def renewPeriodical(user: Username, request: PatchSolicitedResolicitRequest): Future[(StatusCode, Docs)]

  def getLatePeriodical(user: Username, request: GetSolicitedLateRequest): Future[(StatusCode, Docs)]
}
