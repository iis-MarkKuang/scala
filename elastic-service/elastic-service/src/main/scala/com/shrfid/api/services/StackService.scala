package com.shrfid.api.services

import javax.inject.{Inject, Singleton}
import com.shrfid.api.http.Elastic.stack._
import com.elastic_service.elasticServer.ElasticServerThrift._
/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class StackService @Inject()(
  elasticService: ElasticService
){

  def insertStack(args: InsertBookStack.Args) = {
    elasticService.insertBookStack(args.user, PostBookStackRequest(
      args.request.authorization,
      args.request.name,
      args.request.isActive,
      args.request.branchId,
      args.request.description
    )).map(a => a._2)
  }

  def findStacks(args: FindBookStacks.Args) = {
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

  def findStackById(args: FindBookStackById.Args) = {
    elasticService.findBookStackById(args.user, GetBookStackByIdRequest(
      args.request.authorization,
      args.request.id
    )).map{ x => x._2}
  }

  def updateStack(args: UpdateBookStackById.Args) = {
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

  def deleteStackById(args: DeleteBookStackById.Args) = {
    elasticService.deleteBookStackById(args.user, DeleteBookStackByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  def deleteStackBulk(args: DeleteBookStackBulk.Args) = {
    elasticService.deleteBookStackBulk(args.user, DeleteBookStackBulkRequest(
      args.request.authorization,
      args.request.ids
    )).map(a => a._2)
  }
}
