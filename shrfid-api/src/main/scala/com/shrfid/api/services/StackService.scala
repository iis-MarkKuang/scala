package com.shrfid.api.services

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.controllers.Permission.PermissionCode
import com.shrfid.api.http.Elastic.stack._
import com.twitter.util.Future

/**
  * Created by jiejin on 2/12/16.
  */
@Singleton
class StackService @Inject()(mysqlService: MysqlService, redisService: RedisService, tokenService: TokenService,
                             elasticService: ElasticService) {
  def insertStack(request: PostBookStackRequest, permission: PermissionCode): Future[UpsertResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertBookStack)
  }

  def findStacks(request: GetBookStackListRequest): Future[(Int, String)] = {
     elasticService.findBookStacks(request)
  }

  def findStackById(request: GetBookStackByIdRequest, permission: PermissionCode): Future[(Int, String)] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBookStackById)
  }

  def updateStack(request: PatchBookStackByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateBookStackById)
  }

  def deleteStackById(request: DeleteBookStackByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteBookStackById)
  }

  def deleteStackBulk(request: DeleteBookStackBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteBookStackBulk)
  }

}
