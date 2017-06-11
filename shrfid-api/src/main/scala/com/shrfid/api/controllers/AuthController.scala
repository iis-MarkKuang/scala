package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.http.auth._
import com.shrfid.api.services.AuthService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
/**
  * Created by jiejin on 19/01/2016.
  */
@Singleton
class AuthController @Inject()(authService: AuthService, response: ResponseBuilder)
  extends Controller {

  get("/api/docs/:file", "get_api_docs") { request: DocsRequest =>
    response.ok.file(s"""docs/${request.file}.pdf""")
  }

  get("/api/create_table") { request: Request =>
    for {
      _ <- authService.createTable
    } yield response.ok("""{"result": "ok"}""").contentTypeJson()
  }

  post(Router.authToken, name = "post_auth_token") { request: PostAuthTokenRequest =>
    for {
      r <- authService.getToken(request.username, request.password)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  get(Router.authPermissions, name = "get_auth_permission_list") { request: GetAuthPermissionListRequest =>
    for {
      r <- authService.getPermissions
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  get(Router.authGroups, name = "get_auth_group_list") { request: GetAuthGroupListRequest =>
    for {
      r <- authService.getGroups(request, Router.authGroups)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  put(Router.authGroups, name = "put_auth_group") { request: PutAuthGroupRequest =>
    for {
      r <- authService.insertGroup(token(request.Authorization), request.name, request.permissionIds, Permission.AuthGroupManagement)
    } yield r match {
      case InvalidToken => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case CreateFailed => response.notAcceptable(NotCreatedResponse).contentTypeJson()
      case CreateSucceed => response.created(CreatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String => response.ok(a).contentTypeJson()
    }
  }

  patch(Router.authGroupById, name = "patch_auth_group_by_id") { request: PatchAuthGroupRequest =>
    for {
      r <- authService.updateGroupById(token(request.Authorization), request.id, request.name, request.permissionIds.insert, request.permissionIds.delete, Permission.AuthGroupManagement)
    } yield r match {
      case InvalidToken => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case UpdateFailed => response.notAcceptable(NotUpdatedResponse).contentTypeJson()
      case UpdateSucceed => response.accepted(UpdatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String => response.ok(a).contentTypeJson()
    }
  }

  delete(Router.authGroupById, name = "delete_auth_group_by_id") { request: DeleteAuthGroupRequest =>
    for {
      r <- authService.deleteGroupById(token(request.Authorization), request.id, Permission.AuthGroupManagement)
    } yield r match {
      case InvalidToken => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case DeleteFailed => response.notAcceptable(NotDeletedResponse).contentTypeJson()
      case DeleteSucceed => response.accepted(DeletedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String => response.ok(a).contentTypeJson()
    }
  }

  get(Router.authInfo, name = "get_auth_user_info") { request: GetAuthInfoRequest =>
    for {
      r <- authService.getUserInfo(token(request.Authorization))
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case a: String =>
        response.ok(GetAuthInfoResponse.toHttpResponse(a)).contentTypeJson()
    }
  }

  get(Router.authUsers, name = "get_auth_user_list") { request: GetAuthUserListRequest =>
    for {
      r <- authService.getUsers(token(request.Authorization), request.limit, request.offset, request.username,
        request.identity, request.fullName, request.gender, request.isSuperuser, request.isActive, request.ordering, Router.authUsers)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  get(Router.authUserById, name = "get_auth_user_by_id") { request: GetAuthUserRequest =>
    for {
      r <- authService.getUserById(token(request.Authorization), request.id)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case NotFound =>
        response.notFound("{}").contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  put(Router.authUsers, name = "put_auth_user") { request: PutAuthUserRequest =>
    for {
      r <- authService.insertUser(request, Permission.AuthUserManagement)
    } yield r match {
      case InvalidToken => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case CreateFailed => response.notAcceptable(NotCreatedResponse).contentTypeJson()
      case CreateSucceed => response.created(CreatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String => response.ok(a).contentTypeJson()
    }
  }

  patch(Router.authUserById, name = "patch_auth_user") { request: PatchAuthUserRequest =>
    for {
      r <- authService.updateUser(token(request.Authorization), request.id, request.identity, request.fullName,
        request.gender, request.dob, request.email, request.mobile, request.address, request.postcode, request.profileUrl,
        request.isStaff, request.isActive, Permission.AuthUserManagement)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case UpdateFailed => response.notAcceptable(NotUpdatedResponse).contentTypeJson()
      case UpdateSucceed => response.accepted(UpdatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  delete(Router.authUserById, name = "delete_auth_user") { request: DeleteAuthUserRequest =>
    for (
      r <- authService.deleteUser(token(request.Authorization), request.id, Permission.AuthUserManagement)
    ) yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case DeleteFailed => response.notAcceptable(NotDeletedResponse).contentTypeJson()
      case DeleteSucceed => response.notAcceptable(DeletedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  patch(Router.authUserById + "/permissions", name = "patch_auth_user_permission_by_id") { request: PatchAuthUserPermissionRequest =>
    for {
      r <- authService.updateUserPermission(token(request.Authorization), request.id, request.permissionIds, request.groupIds, Permission.AuthPermissionManagement)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case UpdateFailed => response.notAcceptable(NotUpdatedResponse).contentTypeJson()
      case UpdateSucceed => response.accepted(UpdatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  patch(Router.authUserForceUpdatePassword, name = "patch_auth_user_password_force") { request: PatchAuthUserPasswordForceRequest =>
    for {
      r <- authService.forceUpdateUserPassword(token(request.Authorization), request.id, request.password, Permission.ForceChangePassword)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case UpdateFailed => response.notAcceptable(NotUpdatedResponse).contentTypeJson()
      case UpdateSucceed => response.accepted(UpdatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

  patch(Router.authUserUpdatePassword, name = "patch_auth_user_password") { request: PatchAuthUserPasswordRequest =>
    for {
      r <- authService.updateUserPassword(token(request.Authorization), request.oldPassword, request.newPassword, Permission.ChangePassword)
    } yield r match {
      case InvalidToken =>
        response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case UpdateFailed => response.notAcceptable(NotUpdatedResponse).contentTypeJson()
      case UpdateSucceed => response.accepted(UpdatedResponse).contentTypeJson()
      case NoPermission => response.forbidden(NoPermissionResponse).contentTypeJson()
      case a: String =>
        response.ok(a).contentTypeJson()
    }
  }

}
