package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.vendor.member._
import com.shrfid.api.http.Elastic.vendor.order._
import com.shrfid.api.services.VendorService
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder

/**
  * Created by jiejin on 20/10/16.
  */

@Singleton
class VendorController @Inject()(response: ResponseBuilder, venderService: VendorService) extends Controller {

  // member
  post(Router.VendorMember.list, name = "post_vendor_member") { request: PostVendorMemberRequest =>
    for {
      r <- venderService.insertVendor(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.VendorMember.byId, name = "patch_vendor_member_by_id") { request: PatchVendorMemberByIdRequest =>
    for {
      r <- venderService.updateVendorById(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.VendorMember.list, name = "get_vendor_members") { request: GetVendorMemberListRequest =>
    for {
      r <- venderService.findVendors(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.VendorMember.byId, name = "get_vendor_member_by_id") { request: GetVendorMemberByIdRequest =>
    for {
      r <- venderService.findVendorById(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.VendorMember.byId, name = "delete_vendor_member_by_id") { request: DeleteVendorMemberByIdRequest =>
    for {
      r <- venderService.deleteVendorById(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  // order
  post(Router.VendorOrder.list, name = "post_vendor_order") { request: PostVendorOrderRequest =>
    for {
      r <- venderService.insertOrder(request, Permission.VendorPlaceOrder)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  post(Router.VendorOrder.bulk, name = "post_vendor_order_bulk") { request: PostVendorOrderBulkRequest =>
    for {
      r <- venderService.insertOrderBulk(request, Permission.VendorPlaceOrder)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.VendorOrder.bulk, name = "patch_vendor_order") { request: PatchVendorOrderBulkRequest =>
    for {
      r <- venderService.patchOrders(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.VendorOrder.list, name = "get_vendor_orders") { request: GetVendorOrderListRequest =>
    for {
      r <- venderService.findOrders(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.VendorOrder.byId, name = "get_vendor_order_by_id") { request: GetVendorOrderByIdRequest =>
    for {
      r <- venderService.findOrderById(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.VendorOrder.byId, name = "delete_vendor_order_by_id") { request: DeleteVendorOrderByIdRequest =>

    for {
      r <- venderService.deleteOrderById(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.VendorOrder.list, name = "delete_vendor_orders") { request: DeleteVendorOrderBulkRequest =>
    for {
      r <- venderService.deleteOrderBulk(request, Permission.VendorManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

}