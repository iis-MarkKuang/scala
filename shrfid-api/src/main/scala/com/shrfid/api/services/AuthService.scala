package com.shrfid.api.services

import java.sql.Date
import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.auth.{AuthGroup, AuthGroupNested, AuthUserInfo, IdUpdating}
import com.shrfid.api.http.auth._
import com.shrfid.pbkdf2.SecureHash
import com.twitter.util.Future
import play.api.libs.json.Json
/**
  * Created by jiejin on 19/01/2016.
  */
@Singleton
class AuthService @Inject()(mysqlService: MysqlService, redisService: RedisService, tokenService: TokenService) {
  def getToken(username: String, password: String): Future[String] = {
    for {
      u <- mysqlService.findAuthUser(username)
      token <- tokenService.getToken(password, u) flatMap {
        case Empty => Future.value(InvalidToken)
        case InactiveUserResponse => Future.value(InactiveUserResponse)
        case a =>
          val user = u.get
          for {
            _ <- mysqlService.updateAuthUserLastLogin(user.id)
            permissions <- mysqlService.findAuthUserPermissions(user.id, user.isSuperuser, user.isStaff)
            _ <- redisService.setToken(a, Json.stringify(Json.toJson(AuthUserInfo.toDomain(user, permissions))))
          } yield Json.stringify(Json.toJson(PostAuthTokenResponse(a)))
      }
    } yield token
  }

  def getUserInfo(token: String): Future[String] = {
    for {
      u <- redisService.getUserInfo(token)
    } yield u match {
      case NotFound => InvalidToken
      case a => a
    }
  }

  def getUserById(token: String, id: Int): Future[String] = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a => for {
          u <- mysqlService.findAuthUser(id) flatMap {
            case None => Future.value(NotFound)
            case Some(user) => for {
              userPermissionIds <- mysqlService.findAuthUserPermissionIds(user.id, user.isSuperuser, user.isStaff)
              groupPermissions <- mysqlService.findAuthGroupPermissionsByUserId(user.id, user.isSuperuser, user.isStaff)
            } yield GetAuthUserResponse.toHttpResponse(user, userPermissionIds, groupPermissions)
          }
        } yield u
      }
    } yield response
  }

  def getUsers(token: String,
               limit: Int,
               offset: Int,
               username: Option[String],
               identity: Option[String],
               fullName: Option[String],
               gender: Option[String],
               isSuperuser: Option[Boolean],
               isActive: Option[Boolean],
               ordering: Option[String],
               path: String) = {
    for {
      authUsers <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a => for {
          us <- mysqlService.findAuthUsers(limit, offset, username, identity, fullName, gender, isSuperuser, isActive, ordering)
        } yield GetAuthUserListResponse.toHttpResponse(us._2, s"""$path?username=$username&identity=$identity&full_name=$fullName&gender=$gender&is_superuser=$isSuperuser&is_active=$isActive&ordering=$ordering""", limit, offset, us._1)
      }
    } yield authUsers
  }

  def insertUser(request: PutAuthUserRequest, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(request.Authorization.substring(7)) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.insertAuthUser(request.username, request.identity, request.fullName, request.gender, request.dob,
                request.email, request.mobile, request.address, request.postcode, request.profileUrl) flatMap {
                case 0 => Future.value(CreateFailed)
                case other => Future.value(CreateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateUser(token: String, id: Int, identity: String = "", fullName: String = "", gender: String, dob: Option[Date],
                 email: String, mobile: String, address: String, postcode: String,
                 profileUrl: String, isStaff: Boolean, isActive: Boolean, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateAuthUser(id, identity, fullName, gender, dob,
                email, mobile, address, postcode, profileUrl, isStaff, isActive) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  // Added by kuangyuan 5/18/2017
  def deleteUser(token: String, id: Int, permissionId: Int): Future[String] = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true => mysqlService.deleteAuthUser(id) flatMap {
              case 0 => Future.value(DeleteFailed)
              case other => Future.value(DeleteSucceed)
            }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def getPermissions(token: String): Future[String] = {
    for {
      permissions <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a => for {
          ps <- mysqlService.findAuthPermissions
        } yield GetAuthPermissionListResponse.toHttpResponse(ps)
      }
    } yield permissions
  }


  def getPermissions: Future[String] = {
    for {
      ps <- mysqlService.findAuthPermissions
    } yield GetAuthPermissionListResponse.toHttpResponse(ps)

  }

  def getGroups(request: GetAuthGroupListRequest, path: String): Future[String] = {
    for {
      groups <- redisService.getUserInfo(token(request.Authorization)) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a => mysqlService.findAuthGroups(request.queryFilter) flatMap {
          case not if not._2.isInstanceOf[Vector[AuthGroup]] => Future.value(request.response(not.asInstanceOf[(Int, Seq[AuthGroup])], path))
          case other => Future.value(request.nestedResponse(other.asInstanceOf[(Int, Seq[AuthGroupNested])], path))
        }
      }
    } yield groups
  }

  def insertGroup(token: String, name: String, groupPermissionIds: Seq[Int], permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.insertAuthGroup(name, groupPermissionIds) flatMap {
                case 0 => Future.value(CreateFailed)
                case other => Future.value(CreateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateGroupById(token: String, id: Int, name: String, insert: Seq[Int], delete: Seq[Int], permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateAuthGroup(id, name, insert, delete) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def deleteGroupById(token: String, id: Int, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
//              mysqlService.deleteAuthGroup(id) flatMap {
//                case 0 => Future.value(DeleteFailed)
//                case other => Future.value(DeleteSucceed)
//              }

              // modified by kuang yuan 5/2/2017
              mysqlService.deleteAuthGroupNew(id) flatMap {
                case a => Future.value(a._2)
              }

            case false =>
              Future.value(NoPermission)
          }
      }
    } yield response
  }

  def createTable = {
    for {
      _ <- mysqlService.createTable
    } yield Unit
  }


  def forceUpdateUserPassword(token: String, userId: Int, password: String, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true => mysqlService.updateAuthUserPassword(userId, SecureHash.createHash(password)) flatMap {
              case 0 => Future.value(UpdateFailed)
              case other => Future.value(UpdateSucceed)
            }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateUserPassword(token: String, oldPassword: String, newPassword: String, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.findAuthUserPassword(userInfo.id) flatMap {
                case None => Future.value(UpdateFailed)
                case Some(password) =>
                  for {
                    isVaild <- tokenService.validatePassword(oldPassword, password) flatMap {
                      case true =>
                        mysqlService.updateAuthUserPassword(userInfo.id, SecureHash.createHash(newPassword)) flatMap {
                          case 0 => Future.value(UpdateFailed)
                          case other => Future.value(UpdateSucceed)
                        }
                      case false =>
                        Future.value(OldPasswordNotMatch)
                    }
                  } yield isVaild
              }

            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateUserPermission(token: String, userId: Int, permissionIds: IdUpdating, groupIds: IdUpdating, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateAuthUserPermission(userId, permissionIds, groupIds) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }
}
