package com.shrfid.api.services

import com.shrfid.api.http.Elastic.book.item.{GetBookItemByIdRequest, GetBookItemListRequest, PostBookItemRequest, PostPreGenBookItemsRequest}
import com.elastic_service.elasticServer.ElasticServerThrift._
import com.google.inject.{Inject, Singleton}
import com.shrfid.api.domains.book.BookReference
import com.shrfid.api.http.Elastic.book.reference.GetBookReferenceListRequest
import com.twitter.util.Future
/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class BookService @Inject()(
  elasticService: ElasticService
) {
  def preGenItems(args: PreGenBookItems.Args) = {
    elasticService.preGenBookItems(args.user, PostPreGenBookItemsRequest(
      args.request.authorization,
      args.request.categoryId,
      args.request.stackId,
      args.request.quantity
    )).map(a => a._2)
  }

  def insertReference(args: InsertReference.Args) = {
    elasticService.insertReference(args._id, args._source)
      .map(a => a._2)
  }

  def findReferenceById(args: FindReferenceById.Args) = {
    elasticService.findReferenceById(args._id)
      .map(a => a._2)
  }

  def findReference(args: FindReference.Args) = {
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

  def updateBookInfoItem(args: UpdateBookInfoItem.Args) = {
    elasticService.updateBookInfoItem(args.id, args.item, args.oldId)
  }

  def findItems(args: FindBookItems.Args) = {
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

  def findItemById(args: FindBookItemById.Args) = {
    elasticService.findBookItemById(args.user, GetBookItemByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  def insertBookItem(args: InsertBookItem.Args) = {
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

  def increment(args: Int) = {
    Future(true)
  }
}
