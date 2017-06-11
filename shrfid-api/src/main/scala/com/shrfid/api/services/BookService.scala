package com.shrfid.api.services

import java.io.FileInputStream
import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.controllers.Permission._
import com.shrfid.api.http.Elastic.book.{PatchBookItemsActiveStatusRequest, PatchBookItemsStackIdRequest}
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.data._
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.shrfid.api.http.Elastic.book.reservation.GetBookReservationListRequest
import com.shrfid.api.http.Elastic.book.solicitedPeriodicals._
import com.shrfid.api.http.Elastic.stack._
import com.twitter.util.Future
import org.marc4j.MarcStreamReader

/**
  * Created by jiejin on 9/10/16.
  */
@Singleton
class BookService @Inject()(mysqlService: MysqlService, redisService: RedisService, tokenService: TokenService,
                            elasticService: ElasticService) {


  // reference
  def insertReference(filename: String) = {
    val in = new FileInputStream(s"/Users/jiejin/Projects/Scala/qiuxin/marcpost/$filename.mrc")
    val reader = new MarcStreamReader(in, "UTF-8")
    var count = 0
    while (reader.hasNext) {
      val record = CNMARC(reader.next())
      elasticService.insertReference(record.BookInfo.obj.id, record.BookInfo.obj.jsonStringfy)
      count = count + 1
    }
    println(count)
    Future.Unit
  }

  def preGenItems(request: PostPreGenBookItemsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request,
      elasticService.preGenBookItems)
  }

  def findReferenceById(id: String) = {
    elasticService.findReferenceById(id)
  }

  def findReferences(request: GetBookReferenceListRequest) = {
    elasticService.findReference(request)
  }

  def findReservationById(id: String) = {
    elasticService.findReservationById(id)
  }

  def findReservations(request: GetBookReservationListRequest) = {
    elasticService.findReservation(request)
  }


  // book
  def insertItem(request: PostBookItemsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request,
      elasticService.catalogue)
  }

  def findItems(request: GetBookItemListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookItems)
  }

  def findItemById(request: GetBookItemByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookItemById)
  }

  def returnBookItems(request: PostBookItemsReturnRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.returnBooks)
  }

  //leixx,2017-4-5
  def updateBookItemBusinessAndPhysicalState(request: PatchBookPhysicalStackByBarCodeRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateBookItemBusinessAndPhysicalState)
  }

  //leixx,2017-4-8
  //对图书在架物理位置进行初始化
  def initBookItemShelf(request: PatchBookPhysicalStackByBarCodeRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.initBookItemShelf)
  }

  //leixx,2017-4-8
  //对图书业务和物理在架状态查询
  def getBookItemBusinessAndPhysicalState(request:GetBookBusinessAndPhysicalStateByBarCodeRequest,permission: PermissionCode)={
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.getBookItemBusinessAndPhysicalState)
  }


  def findBookBorrowRanking(request: GetBookBorrowRankingRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookBorrowRanking)
  }

  def findCategoryFlowStatistics(request: GetCategoryFlowStatisticsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findCategoryFlowStatistics)
  }

  def findBookFlowStatistics(request: GetBookFlowStatisticsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookFlowStatistics)
  }

  def findStockStatistics(request: GetStockStatisticsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findStockStatistics)
  }

  // Added by kuangyuan 4/17/2017
  def findItemByBarcode(request: GetBookItemByBarcodesRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookByBarcodeStr)
  }

  // Added by kuangyuan 4/19/2017
  def patchBooksShelfStatusByDir(request: PatchBookPhysicalShelfStatusViaFTPRequest, permission: PermissionCode) = {
//    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateBookItemsShelfStatusByDir)
    elasticService.updateBookItemsShelfStatusByDir("shrfid", request)
  }

  // Added by kuangyuan 4/20/2017
  def getCheckProgress(request: GetCheckProgressRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.getCheckProgress)
  }

  // Added by kuangyuan 4/26/2017
  def getBooksShelfStatusByDir(request: GetBookPhysicalShelfStatusViaFTPRequest, permission: PermissionCode) = {
//    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.getBookItemShelfStatusByDir)
    elasticService.getBookItemShelfStatusByDir("shrfid", request)
  }

  // Added by kuangyuan 5/6/2017
  def bindPeriodicals(request: PostBookItemBindRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.bindPeriodicals)
  }

  // Added by kuangyuan 5/9/2017
  def findSolicitedById(request: GetSolicitedByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findSolicitedById)
  }

  def findSolicitedList(request: GetSolicitedListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findSolicitedList)
  }

  def updateSolicitedById(request: PatchSolicitedRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateSolicitedById)
  }

  def deleteSolicitedById(request: DeleteSolicitedByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteSolicitedById)
  }

  def deleteSolicitedBulk(request: DeleteSolicitedBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteSolicitedBulk)
  }

  def postSolicited(request: PostSolicitedRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.postSolicited)
  }

  // Added by kuangyuan 5/12/2017
  def findFineByBarcode(request: GetBookFineByBarcodeRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findFineByBarcode)
  }

  // Added by kuangyuan 5/13/2017
  def updateBookItemsStackId(request: PatchBookItemsStackIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateBookItemsStackId)
  }

  def deactivateBooks(request: PatchBookItemsActiveStatusRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deactivateBooks)
  }

  def reactivateBooks(request: PatchBookItemsActiveStatusRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.reactivateBooks)
  }

  // Added by kuangyuan 5/19/2017
  def findBookLeastBorrowStats(request: GetBookLeastBorrowStatsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookLeastBorrowStats)
  }

  // Added by kuangyuan 5/23/2017
  def resolicitPeriodical(request: PatchSolicitedResolicitRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.renewPeriodical)
  }

  def getLatePeriodical(request: GetSolicitedLateRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.getLatePeriodical)
  }
}
