package com.shrfid.api.services

import com.elastic_service.elasticServer.ElasticServerThrift._
import com.google.inject.{Inject, Singleton}
import com.shrfid.api.http.Elastic.branch._
/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class BranchService @Inject()(
  elasticService: ElasticService
) {

  def insertBookBranch(args: InsertBookBranch.Args) = {
    elasticService.insertBookBranch(args.user, PostBookBranchRequest.wrap(
      args.request
    )).map(a => a._2)
  }

  def findBookBranchById(args: FindBookBranchById.Args) = {
    elasticService.findBookBranchById(args.user, GetBookBranchByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  def findBranches(args: FindBookBranches.Args) = {
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

  def updateBookBranchById(args: UpdateBookBranchById.Args) = {
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

  def deleteBranchById(args: DeleteBookBranchById.Args) = {
    elasticService.deleteBookBranchById(args.user, DeleteBookBranchByIdRequest(
      args.request.authorization,
      args.request.id
    )).map(a => a._2)
  }

  def deleteBranchBulk(args: DeleteBookBranchBulk.Args) = {
    elasticService.deleteBookBranchBulk(args.user, DeleteBookBranchBulkRequest(
      args.request.authorization,
      args.request.ids
    )).map(a => a._2)
  }
}
