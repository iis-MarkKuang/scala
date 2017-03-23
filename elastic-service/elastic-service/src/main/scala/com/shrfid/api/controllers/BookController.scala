package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.book.reference.{GetBookCatalogingByISBNRequest, GetBookReferenceById, GetBookReferenceListRequest}
import com.shrfid.api.services.BookService
import com.twitter.finagle.http.{Request, Status}
import com.twitter.finatra.http.Controller
//import com.twitter.finatra.thrift.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
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
  get(Router.Book.writtingMethod, "get_book_writing_method") { request: Request =>
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
  post(Router.Book.items, "post_book_item") { request: PostBookItemRequest =>
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

  get(Router.BookCataloging.catalogingByISBN, "get_book_cataloging_by_isbn") { request: GetBookCatalogingByISBNRequest =>
   for {
       r <- bookService.findCatalogingByISBN(request)
     } yield r match {
       case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
       case (Status.BadRequest.code, s) => response.badRequest(s).contentTypeJson()
       case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
       case (_, s) => response.ok(s).contentTypeJson()
     }

    //bookService.findCatalogingByISBN(request).map(resp => response.status(resp._1).body(resp._2).contentType("application/json"))

  }



}

