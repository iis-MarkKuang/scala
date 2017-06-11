package com.shrfid.api.controllers


import java.util.concurrent.Executors
import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.book.{PatchBookItemsActiveStatusRequest, PatchBookItemsStackIdRequest}
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.{GetBookReferenceById, GetBookReferenceListRequest}
import com.shrfid.api.http.Elastic.book.reservation.GetBookReservationListRequest
import com.shrfid.api.http.Elastic.book.solicitedPeriodicals._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.services.BookService
import com.twitter.finagle.http.{Request, Status}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import play.libs.Json

/**
  * Created by jiejin on 9/10/16.
  */
@Singleton
class BookController @Inject()(bookService: BookService, response: ResponseBuilder)
  extends Controller {

  // reference
  get(Router.Book.importReference, "get_mrc") { request: Request =>
    for {
      _ <- bookService.insertReference(request.getParam("filename"))
    } yield response.ok("""{"result": "ok"}""").contentTypeJson()
  }

  get(Router.Reference.byId, "get_book_info_by_reference") { request: GetBookReferenceById =>
    for {
      r <- bookService.findReferenceById(request.id)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Reference.list, "get_search_book_elastic") { request: GetBookReferenceListRequest =>
    for {
      r <- bookService.findReferences(request)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.InternalServerError.code, error) => response.internalServerError(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  // book item
  get(Router.Book.writingMethod, "get_book_writing_method") { request: Request =>
    response.ok(Json.stringify(Json.toJson(Config.writingMethod))).contentTypeJson()
  }

  get(Router.Book.language, "get_book_language") { request: Request =>
    response.ok(Json.stringify(Json.toJson(Config.language))).contentTypeJson()
  }

  post(Router.Book.preGen, "post_pre_gen_book_items") { request: PostPreGenBookItemsRequest =>
    for {
      r <- bookService.preGenItems(request, Permission.BookItemPreGen)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.NotAcceptable.code, _) => response.notAcceptable(NotCreatedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Created.code, a) => response.created(a).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }
  // catalogue
  post(Router.Book.items, "post_book_item") { request: PostBookItemsRequest =>
    for {
      r <- bookService.insertItem(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Book.items, "get_book_items") { request: GetBookItemListRequest =>
    for {
      r <- bookService.findItems(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }
  get(Router.Book.itemById, "get_book_item_by_id") { request: GetBookItemByIdRequest =>
    for {
      r <- bookService.findItemById(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  post(Router.Book._return, "post_return_book_items") { request: PostBookItemsReturnRequest =>
    for {
      r <- bookService.returnBookItems(request, Permission.BookTransaction)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.InternalServerError.code, error) => response.internalServerError(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.Book.transfer, "post_transfer_book_items") { request: PostBookItemsTransferRequest =>


  }

  //预约查询
  get(Router.Reservation.byId, "get_book_reservation_by_id") { request: GetBookReferenceById =>
    for {
      r <- bookService.findReservationById(request.id)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  //预约查询
  get(Router.Reservation.list, "get_search_book_reservation") { request: GetBookReservationListRequest =>
    for {
      r <- bookService.findReservations(request)
    } yield r match {

      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.InternalServerError.code, error) => response.internalServerError(error).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }


  //leixx,2017-4-7
  patch(Router.Book.checkBookByBarCode, "patch_book_item_bp_state") { request: PatchBookPhysicalStackByBarCodeRequest =>
    for {
      r <- bookService.updateBookItemBusinessAndPhysicalState(request,Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //leixx,2017-4-8
  patch(Router.Book.initBookItemShelfByBarCode, "patch_book_item_bp_state") { request: PatchBookPhysicalStackByBarCodeRequest =>
    for {
      r <- bookService.initBookItemShelf(request,Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //leixx,2017-4-8
  get(Router.Book.getBookItemBusinessAndPhysicalState, "get_book_item_bp_state") { request: GetBookBusinessAndPhysicalStateByBarCodeRequest =>
    for {
      r <- bookService.getBookItemBusinessAndPhysicalState(request,Permission.BookItemManagement)
    } yield r match {
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
    //bookService.findCatalogingByISBN(request).map(resp => response.status(resp._1).body(resp._2).contentType("application/json"))
  }

  //added by kuangyuan 4/17/2017
  get(Router.Book.itemsByBarcode, "get_book_item_by_barcode") {request: GetBookItemByBarcodesRequest =>
    for {
      r <- bookService.findItemByBarcode(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //added by kuangyuan 4/19/2017
  patch(Router.Book.bookItemsShelfStatusByDir, "patch_book_shelf_status_by_directory") { request: PatchBookPhysicalShelfStatusViaFTPRequest =>
    for {
      r <- bookService.patchBooksShelfStatusByDir(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.Book.getCheckProgress, "get_check_progress") { request: GetCheckProgressRequest =>
    for (
      r <- bookService.getCheckProgress(request, Permission.BookItemManagement)
    ) yield r match {
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.PreconditionFailed.code, s) => response.preconditionFailed(s).contentTypeJson()
    }
  }

  //added by kuangyuan 4/26/2017
  get(Router.Book.bookItemsShelfStatusByDir, "get_book_shelf_status_by_directory") { request: GetBookPhysicalShelfStatusViaFTPRequest =>
    for {
      r <- bookService.getBooksShelfStatusByDir(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  //added by kuangyuan 5/6/2017
  post(Router.Book.periodicalBind, "bind_periodicals") { request: PostBookItemBindRequest =>
    for {
      r <- bookService.bindPeriodicals(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  //added by kuangyuan 5/9/2017
  get(Router.Solicited.byId, "get_solicited_periodical_by_id") { request: GetSolicitedByIdRequest =>
    for {
      r <- bookService.findSolicitedById(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Solicited.list, "get_solicited_periodical_list") { request: GetSolicitedListRequest =>
    for {
      r <- bookService.findSolicitedList(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  patch(Router.Solicited.byId, "update_solicited_periodicals_by_id") { request: PatchSolicitedRequest =>
    for {
      r <- bookService.updateSolicitedById(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  delete(Router.Solicited.byId, "delete_solicited_periodicals_by_id") { request: DeleteSolicitedByIdRequest =>
    for {
      r <- bookService.deleteSolicitedById(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  delete(Router.Solicited.list, "delete_solicited_periodicals_bulk") { request: DeleteSolicitedBulkRequest =>
    for {
      r <- bookService.deleteSolicitedBulk(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  post(Router.Solicited.list, "post_solicited_periodicals_by_id") { request: PostSolicitedRequest =>
    for {
      r <- bookService.postSolicited(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  // Added by kuangyuan 5/12/2017
  get(Router.Book.fineByBarcode, "get_fine_by_barcode") { request: GetBookFineByBarcodeRequest =>
    for {
      r <- bookService.findFineByBarcode(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  // Added by kuangyuan 5/13/2017
  patch(Router.Book.deactivate, "deactivate_books") { request: PatchBookItemsActiveStatusRequest =>
    for {
      r <- bookService.deactivateBooks(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  patch(Router.Book.alterStack, "change_books_stacks") { request: PatchBookItemsStackIdRequest =>
    for {
      r <- bookService.updateBookItemsStackId(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  patch(Router.Book.reactivate, "reactivate_books") { request: PatchBookItemsActiveStatusRequest =>
    for {
      r <- bookService.reactivateBooks(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  // Added by kuangyuan 5/23/2017
  patch(Router.Solicited.resolicit, "resolicit_periodical") { request: PatchSolicitedResolicitRequest =>
    for {
      r <- bookService.resolicitPeriodical(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, s) => response.forbidden(NotAcceptableResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Solicited.lateSolicited, "get_late_solicited") { request: GetSolicitedLateRequest =>
    for {
      r <- bookService.getLatePeriodical(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }
}

