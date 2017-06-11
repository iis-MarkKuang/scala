package com.shrfid.api.controllers


import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.stack._
import com.shrfid.api.services.StackService
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.json.modules.FinatraJacksonModule

/**
  * Created by jiejin on 9/10/16.
  */
@Singleton
class StackController @Inject()(stackService: StackService, response: ResponseBuilder, f: FinatraJacksonModule)
  extends Controller {
  post(Router.Stack.list, "post_book_stack") { request: PostBookStackRequest =>
    for {
      r <- stackService.insertStack(request, Permission.BookStackManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.Stack.list, "get_book_stack") { request: GetBookStackListRequest =>
    for {
      r <- stackService.findStacks(request)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.Stack.byId, "get_book_stack_by_id") { request: GetBookStackByIdRequest =>
    for {
      r <- stackService.findStackById(request, Permission.BookStackManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.Stack.byId, "patch_bookstack_by_id") { request: PatchBookStackByIdRequest =>
    for {
      r <- stackService.updateStack(request, Permission.BookStackManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.Stack.byId, "delete_bookstack_by_id") { request: DeleteBookStackByIdRequest =>
    for {
      r <- stackService.deleteStackById(request, Permission.BookStackManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.Stack.list, "delete_bookstack_bulk") { request: DeleteBookStackBulkRequest =>
    for {
      r <- stackService.deleteStackBulk(request, Permission.BookStackManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, reason) => response.notAcceptable(reason).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

}