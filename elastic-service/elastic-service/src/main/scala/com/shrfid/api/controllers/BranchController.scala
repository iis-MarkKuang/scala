package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.Elastic.branch._
import com.shrfid.api.services.BranchService
import com.twitter.finagle.http.Status
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder

/**
  * Created by jiejin on 9/10/16.
  */
@Singleton
class BranchController @Inject()(branchService: BranchService, response: ResponseBuilder)
  extends Controller {

  // branch
  post(Router.Branch.list, "put_book_branch") { request: PostBookBranchRequest =>
    for {
      r <- branchService.insertBranch(request, Permission.BookBranchManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (Status.Created.code, s) => response.created(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.Branch.list, "get_book_branch") { request: GetBookBranchListRequest =>
    for {
      r <- branchService.findBranches(request)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  get(Router.Branch.byId, "get_book_branch_by_id") { request: GetBookBranchByIdRequest =>
    for {
      r <- branchService.findBranchById(request, Permission.BookBranchManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.Ok.code, s) => response.ok(s).contentTypeJson()
      case (Status.NotFound.code, _) => response.notFound(RecordNotFoundResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  patch(Router.Branch.byId, "patch_book_branch") { request: PatchBookBranchByIdRequest =>
    for {
      r <- branchService.updateBranchById(request, Permission.BookBranchManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (Status.NotAcceptable.code, error) => response.notAcceptable(error).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.Branch.byId, "delete_book_branch_by_id") { request: DeleteBookBranchByIdRequest =>
    for {
      r <- branchService.deleteBranchById(request, Permission.BookBranchManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

  delete(Router.Branch.list, "delete_book_branch_bulk") { request: DeleteBookBranchBulkRequest =>
    for {
      r <- branchService.deleteBranchBulk(request, Permission.BookBranchManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, s) => response.ok(s).contentTypeJson()
    }
  }

}