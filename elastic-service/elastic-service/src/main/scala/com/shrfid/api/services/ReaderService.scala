package com.shrfid.api.services

import javax.inject.Inject

import com.shrfid.api._
import com.shrfid.api.controllers.Permission.PermissionCode
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._

/**
  * Created by jiejin on 12/09/2016.
  */
class ReaderService @Inject()(authService: AuthService,
                              mysqlService: MysqlService,
                              redisService: RedisService,
                              tokenService: TokenService,
                              elasticService: ElasticService) {

  /*










  def getMembers(token: String,
                 limit: Int,
                 offset: Int,
                 barcode: Option[String],
                 identity: Option[String],
                 fullName: Option[String],
                 gender: Option[String],
                 dob: Option[String],
                 readerLevel: Option[String],
                 readerGroup: Option[String],
                 createAt: Option[String],
                 isActive: Option[Boolean],
                 isSuspend: Option[Boolean],
                 ordering: Option[String],
                 logicOp: String,
                 path: String) = {
    for {
      members <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a => mysqlService.findReaderMembers(limit, offset, barcode, identity, fullName, gender, dob,
          readerLevel, readerGroup, createAt, isActive, isSuspend, logicOp, ordering) flatMap {
          case EmptyList => Future.value(EmptyListResponse)
          case other => Future.value(GetReaderMemberListResponse.toHttpResponse(other._2, s"""$path?barcode=${barcode.getOrElse("")}&gender=${gender.getOrElse("")}&identity=${identity.getOrElse("")}&full_name=${fullName.getOrElse("")}&reader_level=${readerLevel.getOrElse("")}&reader_group=${readerGroup.getOrElse("")}&create_at=${createAt.getOrElse("")}&is_active=${isActive.getOrElse("")}&is_suspend=${isSuspend.getOrElse("")}""", limit, offset, other._1))
        }
      }
    } yield members
  }

  def getMemberByParam(token: String,
                       param: String,
                       by: String) = {
    for {
      member <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          mysqlService.findReaderMember(param, by) flatMap {
            case None => Future.value(NotFound)
            case Some(other) => Future.value(GetReaderMemberResponse.toHttpResponse(other))
          }
      }
    } yield member
  }

  def insertMember(token: String, barcode: String,
                   rfid: String,
                   level: Int,
                   groups: Seq[Int],
                   identity: String = "",
                   fullName: String,
                   gender: String,
                   dob: String,
                   email: String,
                   mobile: String,
                   address: String,
                   postcode: String,
                   profileImage: String,
                   createAt: String, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true => mysqlService.insertReaderMember(barcode, rfid, level, groups, identity, fullName, gender, dob,
              email, mobile, address, postcode, profileImage, createAt) flatMap {
              case 0 => Future.value(CreateFailed)
              case other => Future.value(CreateSucceed)
            }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def insertBulkMember(token: String, members: Seq[ReaderMemberInsertion], permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true => mysqlService.insertReaderMembers(members) flatMap {
              case failed if failed.map(_._2).contains(0) => Future.value(failed.toString)
              case other => Future.value(CreateSucceed)
            }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateMember(token: String,
                   memberId: Int,
                   barcode: String,
                   rfid: Option[String],
                   identity: String,
                   fullName: String,
                   gender: String,
                   dob: Date,
                   email: Option[String],
                   mobile: Option[String],
                   address: Option[String],
                   postcode: Option[String],
                   profileUrl: Option[String],
                   isActive: Boolean,
                   restoreAt: Option[Date],
                   levelId: Int,
                   groupIds: IdUpdating,
                   permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateReaderMember(memberId, barcode, rfid, identity, fullName, gender, dob, email, mobile, address, postcode, profileUrl,
                isActive, restoreAt, levelId, groupIds) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)

              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateMemberInactive(token: String, action: String, by: String, ids: Seq[Int], permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              action match {
                case "inactivate" =>
                  mysqlService.inactiveReaderMember(by, ids) flatMap {
                    case 0 => Future.value(UpdateFailed)
                    case other => Future.value(UpdateSucceed)
                  }
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateMemberCard(token: String, id: Int, barcode: String, rfid: String, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateReaderMemberCard(id, barcode, rfid) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def updateMemberSuspend(token: String, id: Int, days: Int, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.updateReaderMemberSuspend(id, days) flatMap {
                case 0 => Future.value(UpdateFailed)
                case other => Future.value(UpdateSucceed)
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }

  def borrowItems(token: String, param: String, by: String, bookBarcodes: Seq[String], permissionId: Int): Future[(Status, String)] = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value((Status.Unauthorized, InvalidToken))
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              // TODO
              mysqlService.findReaderMember(param, by) flatMap {
                case None => Future.value((Status.NotAcceptable, """{"errors": ["读者不存在"]}"""))
                case Some(other) => (other.isActive, other.restoreAt) match {
                  case (true, None) => //Future.value("""{"result": "ok"}""")
                    borrow(other.level.generalBookRule, other.id, bookBarcodes)
                  case (true, Some(d)) => Future.value((Status.NotAcceptable, s"""{"errors": ["读者被停证直至${d.toString}"]}"""))
                  case (false, _) => Future.value((Status.NotAcceptable, s"""{"errors": ["该读者证已被注销"]}"""))
                }
              }
            case false => Future.value((Status.Forbidden, NoPermission))
          }
      }
    } yield response
  }

  private def borrow(borrowRule: BorrowRule, readerId: Int, bookBarcodes: Seq[String]): Future[(Status, String)] = {
    val n = bookBarcodes.length
    for {
      response <- mysqlService.countReaderBorrowRecords(readerId, "current") flatMap {

        case count => count + n <= borrowRule.quantity match {
          case true =>
            val dueTime = DateTime.now().toLocalDate.plusDays(borrowRule.day + 1).toDateTimeAtStartOfDay.minus(1000)
            mysqlService.borrowBooks(readerId, bookBarcodes, dueTime) flatMap {
              case 0 => Future.value((Status.InternalServerError, s"""{"errors": ["请求失败请稍后尝试"]}"""))
              case other => Future.value((Status.Ok, s"""{"result": "成功借阅${n}本图书,应还时间为$dueTime"}"""))
            }
          case false => Future.value((Status.NotAcceptable, s"""{"errors": ["超过可借上限, 当前已借: $count, 此次请求: $n"]}"""))
        }
      }
    } yield response
  }

  def findBorrowRecords(token: String, param: String, by: String, recordOption: String,
                        limit: Int, offset: Int, path: String, permissionId: Int) = {
    for {
      response <- redisService.getUserInfo(token) flatMap {
        case NotFound => Future.value(InvalidToken)
        case a =>
          val userInfo = Json.parse(a).as[AuthUserInfo]
          checkUserPermissions(userInfo, permissionId) flatMap {
            case true =>
              mysqlService.findReaderBorrowRecords(param, by, recordOption, limit, offset) flatMap {
                case EmptyList => Future.value(EmptyListResponse)
                case other => Future.value(GetReaderMemberBorrowRecordListResponse.toHttpResponse(other._2,
                  s"""$path?by=$by&record=$recordOption""", limit, offset, other._1))
              }
            case false => Future.value(NoPermission)
          }
      }
    } yield response
  }*/


  // reader level
  def insertLevel(request: PostReaderLevelRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertReaderLevel)
  }

  def findLevels(request: GetReaderLevelListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderLevels)
  }

  def findLevelById(request: GetReaderLevelByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderLevelById)
  }

  def updateLevelById(request: PatchReaderLevelByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateReaderLevelById)
  }

  def deleteLevelById(request: DeleteReaderLevelByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteReaderLevelById)
  }

  def deleteLevelBulk(request: DeleteReaderLevelBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteReaderLevelBulk)
  }

  // reader group
  def insertGroup(request: PostReaderGroupRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertReaderGroup)
  }

  def findGroups(request: GetReaderGroupListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderGroups)
  }

  def findGroupById(request: GetReaderGroupByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderGroupById)
  }

  def updateGroupById(request: PatchReaderGroupByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.updateReaderGroup)
  }

  def deleteGroupById(request: DeleteReaderGroupByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteReaderGroupById)
  }

  def deleteGroupBulk(request: DeleteReaderGroupBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.deleteReaderGroupBulk)
  }


  // reader member
  def insertMember(request: PostReaderMemberRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertReaderMember)
  }

  def insertMemberBulk(request: PostReaderMemberBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.insertReaderMemberBulk)
  }

  def findMemberById(request: GetReaderMemberByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderMemberById)
  }

  def findMembers(request: GetReaderMemberListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findReaderMembers)
  }

  def updateMemberBulk(request: PatchReaderMemberBulkRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.patchReaderMemberBulk)
  }

  def updateMemberById(request: PatchReaderMemberByIdRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.patchReaderMemberById)
  }

  def borrowItems(request: PostReaderMemberBorrowItemsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.borrowItems)
  }

  def renewItems(request: PostReaderMemberRenewItemsRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.renewItems)
  }

  def findBorrowRecords(request: GetReaderMemberBorrowRecordListRequest, permission: PermissionCode) = {
    tokenService.checkPermissionWithCallBack(token(request.Authorization), permission)(request, elasticService.findBorrowRecords)
  }

}
