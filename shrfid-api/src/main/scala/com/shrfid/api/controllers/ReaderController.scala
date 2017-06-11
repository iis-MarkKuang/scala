package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import com.shrfid.api.services.ReaderService
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder

/**
  * Created by jiejin on 12/09/2016.
  */
@Singleton
class ReaderController @Inject()(readerService: ReaderService, response: ResponseBuilder) extends Controller {

  // reader level
  post(Router.ReaderLevel.list, name = "post_reader_level") { request: PostReaderLevelRequest =>
    for {
      r <- readerService.insertLevel(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  post(Router.ReaderLevelNew.list, name = "post_new_reader_level") { request: PostReaderLevelNewRequest =>
    for {
      r <- readerService.insertNewLevel(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderLevel.list, name = "get_reader_levels") { request: GetReaderLevelListRequest =>
    for {
      r <- readerService.findLevels(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderLevelNew.list, name = "get_new_reader_levels") { request: GetReaderLevelListRequest =>
    for {
      r <- readerService.findNewLevels(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderLevel.byId, name = "get_reader_level_by_id") { request: GetReaderLevelByIdRequest =>
    for {
      r <- readerService.findLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderLevelNew.byId, name = "get_new_reader_level_by_id") { request: GetReaderLevelByIdRequest =>
    for {
      r <- readerService.findNewLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.ReaderLevel.byId, name = "patch_reader_level") { request: PatchReaderLevelByIdRequest =>
    for {
      r <- readerService.updateLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.ReaderLevelNew.byId, name = "patch_new_reader_level") { request: PatchReaderLevelNewByIdRequest =>
    for {
      r <- readerService.updateNewLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderLevel.byId, name = "delete_reader_level_by_id") { request: DeleteReaderLevelByIdRequest =>
    for {
      r <- readerService.deleteLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderLevelNew.byId, name = "delete_new_reader_level_by_id") { request: DeleteReaderLevelByIdRequest =>
    for {
      r <- readerService.deleteNewLevelById(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderLevel.list, name = "delete_reader_level_bulk") { request: DeleteReaderLevelBulkRequest =>
    for {
      r <- readerService.deleteLevelBulk(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderLevelNew.list, name = "delete_new_reader_level_bulk") { request: DeleteReaderLevelBulkRequest =>
    for {
      r <- readerService.deleteNewLevelBulk(request, Permission.ReaderLevelManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  // reader group
  post(Router.ReaderGroup.list, name = "post_reader_group") { request: PostReaderGroupRequest =>
    for {
      r <- readerService.insertGroup(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderGroup.list, name = "get_reader_groups") { request: GetReaderGroupListRequest =>
    for {
      r <- readerService.findGroups(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderGroup.byId, name = "get_reader_group_by_id") { request: GetReaderGroupByIdRequest =>
    for {
      r <- readerService.findGroupById(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.ReaderGroup.byId, name = "patch_reader_group_by_id") { request: PatchReaderGroupByIdRequest =>
    for {
      r <- readerService.updateGroupById(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderGroup.byId, name = "delete_reader_group_by_id") { request: DeleteReaderGroupByIdRequest =>
    for {
      r <- readerService.deleteGroupById(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.ReaderGroup.list, name = "delete_reader_group_bulk") { request: DeleteReaderGroupBulkRequest =>
    for {
      r <- readerService.deleteGroupBulk(request, Permission.ReaderGroupManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }


  // reader member
  post(Router.ReaderMember.list, name = "post_reader_member") { request: PostReaderMemberRequest =>
    for {
      r <- readerService.insertMember(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  post(Router.ReaderMember.bulk, name = "post_bulk_reader_members") { request: PostReaderMemberBulkRequest =>
    for {
      r <- readerService.insertMemberBulk(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderMember.byId, name = "get_reader_member_by_id") { request: GetReaderMemberByIdRequest =>
    for {
      r <- readerService.findMemberById(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  // Added by kuangyuan 5/4/2017
  get(Router.ReaderMember.findByBarcode, name = "get_reader_member_by_barcode") { request: GetReaderMemberByBarcodeRequest =>
    for {
      r <- readerService.findMemberByBarcode(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderMember.list, name = "get_reader_members") { request: GetReaderMemberListRequest =>
    for {
      r <- readerService.findMembers(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.ReaderMember.bulk, name = "patch_reader_members") { request: PatchReaderMemberBulkRequest =>
    for {
      r <- readerService.updateMemberBulk(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.ReaderMember.byId, name = "patch_reader_member") { request: PatchReaderMemberByIdRequest =>
    for {
      r <- readerService.updateMemberById(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  post(Router.ReaderMember.byId + "/borrow") { request: PostReaderMemberBorrowItemsRequest =>
    for {
      r <- readerService.borrowItems(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.ReaderMember.byId + "/borrow_new") { request: PostReaderMemberBorrowItemsRequest =>
    for {
      r <- readerService.borrowItemsNew(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.ReaderMember.byId + "/renew") { request: PostReaderMemberRenewItemsRequest =>
    for {
      r <- readerService.renewItems(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.ReaderMember.byId + "/renew_new") { request: PostReaderMemberRenewItemsRequest =>
    for {
      r <- readerService.renewItemsNew(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  /*
  * 图书预约
  * */

  post(Router.ReaderMember.byId + "/reservation") { request: PostReaderMemberReservationItemsRequest =>
    for {
      r <- readerService.reservationItems(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.ReaderMember.byId + "/reservation_new") { request: PostReaderMemberReservationItemsRequest =>
    for {
      r <- readerService.reserveItemsNew(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.ReaderMember.currentHoldings) { request: GetReaderMemberBorrowRecordListRequest =>
    for {
      r <- readerService.findBorrowRecords(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  // added by k
  get(Router.ReaderMember.delayFine) { request: GetReaderMemberDelayedFineRequest =>
    for {
      r <- readerService.getReaderDelayedFine(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, s) => response.notFound(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.ReaderMember.lostFine) { request: GetReaderMemberLostFineRequest =>
    for {
      r <- readerService.getReaderLostFine(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, s) => response.notFound(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //may be ignored as we already have update reader api
  put(Router.ReaderMember.deductCredit) { request: UpdateReaderMemberCreditByIdRequest =>
    for {
      r <- readerService.updateReaderCredit(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, s) => response.notFound(s).contentTypeJson()
      case (Status.PaymentRequired.code, s) => response.preconditionFailed(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //added by kuangyuan 5/9/2017
  get(Router.ReaderMember.delayedDetail) { request: GetReaderMemberDelayedDetailRequest =>
    for {
      r <- readerService.getReaderMemberDelayedDetail(request, Permission.ReaderMemberManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, s) => response.notFound(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }

  }
}
