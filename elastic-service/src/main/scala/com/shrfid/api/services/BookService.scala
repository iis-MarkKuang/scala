package com.shrfid.api.services

import java.io.FileInputStream
import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.controllers.Permission._
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.{GetBookCatalogingByISBNRequest, GetBookReferenceListRequest}
import com.twitter.util.Future
import org.marc4j.MarcStreamReader

/**
  * Created by jiejin on 9/10/16.
  */
@Singleton
class BookService @Inject()(mysqlService: MysqlService, redisService: RedisService, tokenService: TokenService,
                            elasticService: ElasticService,douBanService:DouBanService) {


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


  // book
  def insertItem(request: PostBookItemRequest, permission: PermissionCode) = {
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

  def findCatalogingByISBN(request: GetBookCatalogingByISBNRequest) = {
     douBanService.findBookCatalogingByISBN(request)
  }

}
