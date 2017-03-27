package com.shrfid.api.services

import java.sql.Date

import com.elastic_service.elasticServer.ElasticServerThrift._
import com.google.inject.{Inject, Singleton}
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class VendorService @Inject()(
  elasticService: ElasticService
) {
  def insertVendor(args: InsertVendorMember.Args) = {
    elasticService.insertVendorMember(args.user, PostVendorMemberRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  def updateVendorById(args: UpdateVendorMemberById.Args) = {
    elasticService.updateVendorMemberById(args.user, PatchVendorMemberByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6
    )).map(a => a._2)
  }

  def findVendors(args: FindVendorMembers.Args) = {
    elasticService.findVendorMembers(args.user, GetVendorMemberListRequest(
      args.request.authorization,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  def findVendorById(args: FindVendorMemberById.Args) = {
    elasticService.findVendorMemberById(args.user, GetVendorMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def deleteVendorById(args: DeleteVendorMemberById.Args) = {
    elasticService.deleteVendorMemberById(args.user, DeleteVendorMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def deleteVendorBulk(args: DeleteVendorMemberBulk.Args) = {
    elasticService.deleteVendorMemberBulk(args.user, DeleteVendorMemberBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def insertOrder(args: InsertVendorOrder.Args) = {
    elasticService.insertVendorOrder(args.user, PostVendorOrderRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
      Date.valueOf(args.request._11)
    )).map(a => a._2)
  }

  def insertOrderBulk(args: InsertVendorOrderBulk.Args) = {
    elasticService.insertVendorOrderBulk(args.user, PostVendorOrderBulkRequest(
      args.request._1,
      // to be tested
      args.request._2.asInstanceOf[Seq[PutVendorOrderRow]]
    )).map(a => a._2)
  }

  def findOrderById(args: FindVendorOrderById.Args) = {
    elasticService.findVendorOrderById(args.user, GetVendorOrderByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def findOrders(args: FindVendorOrders.Args) = {
    elasticService.findVendorOrders(args.user, GetVendorOrderListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10
    )).map(a => a._2)
  }

  def patchOrders(args: UpdateVendorOrders.Args) = {
    elasticService.updateVendorOrders(args.user, PatchVendorOrderBulkRequest(
      args.request._1,
      args.request._2.asInstanceOf[Seq[PatchVendorOrderRow]]
    )).map(a => a._2)
  }

  def deleteOrderById(args: DeleteVendorOrderById.Args) = {
    elasticService.deleteVendorOrderById(args.user, DeleteVendorOrderByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def deleteOrderBulk(args: DeleteVendorOrderBulk.Args) = {
    elasticService.deleteVendorOrderBulk(args.user, DeleteVendorOrderBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }
}
