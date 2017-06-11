package com.shrfid.api.services

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.controllers.Permission.PermissionCode
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import com.twitter.util.Future

/**
  * Created by jiejin on 6/11/16.
  */
@Singleton
class VendorService @Inject()(authService: AuthService,
                              mysqlService: MysqlService,
                              redisService: RedisService,
                              tokenService: TokenService,
                              elasticService: ElasticService) {

  // vendor
  def insertVendor(request: PostVendorMemberRequest, permission: PermissionCode): Future[UpsertResponse] = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertVendorMember)
  }

  def updateVendorById(request: PatchVendorMemberByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateVendorMemberById)
  }

  def findVendors(request: GetVendorMemberListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findVendorMembers)
  }

  def findVendorById(request: GetVendorMemberByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findVendorMemberById)
  }

  def deleteVendorById(request: DeleteVendorMemberByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteVendorMemberById)
  }

  def deleteVendorBulk(request: DeleteVendorMemberBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteVendorMemberBulk)
  }

  // order

  def insertOrder(request: PostVendorOrderRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertVendorOrder)
  }

  def insertOrderBulk(request: PostVendorOrderBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertVendorOrderBulk)
  }

  def patchOrders(request: PatchVendorOrderBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateVendorOrders)
  }

  def findOrders(request: GetVendorOrderListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findVendorOrders)
  }

  def findOrderById(request: GetVendorOrderByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findVendorOrderById)
  }

  def deleteOrderById(request: DeleteVendorOrderByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteVendorOrderById)
  }

  def deleteOrderBulk(request: DeleteVendorOrderBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteVendorOrderBulk)
  }
}
