package com.shrfid.api.services

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.controllers.Permission.PermissionCode
import com.shrfid.api.http.Elastic.branch._
import com.twitter.util.Future

/**
  * Created by jiejin on 2/12/16.
  */
@Singleton
class BranchService @Inject()(mysqlService: MysqlService, redisService: RedisService, tokenService: TokenService,
                              elasticService: ElasticService) {

  def insertBranch(request: PostBookBranchRequest, permission: PermissionCode): Future[UpsertResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertBookBranch)
  }

  def findBranches(request: GetBookBranchListRequest): Future[SearchResponse] = {
    elasticService.findBookBranches(request)
  }

  def findBranchById(request: GetBookBranchByIdRequest, permission: PermissionCode): Future[SearchResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookBranchById)
  }

  def updateBranchById(request: PatchBookBranchByIdRequest, permission: PermissionCode): Future[UpdateResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateBookBranchById)

  }

  def deleteBranchById(request: DeleteBookBranchByIdRequest, permission: PermissionCode): Future[DeleteResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteBookBranchById)
  }

  def deleteBranchBulk(request: DeleteBookBranchBulkRequest, permission: PermissionCode): Future[DeleteResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteBookBranchBulk)
  }

}

