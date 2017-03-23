//package com.shrfid.api.services.impl
//
//import javax.inject.{Inject, Singleton}
//
//import com.shrfid.api.controllers.Permission._
//import com.shrfid.api.domains.auth.AuthUserInfo
//import com.shrfid.api.persistence.slick.auth.AuthUserEntity
//import com.shrfid.api.services._
//import com.shrfid.api.{Config, _}
//import com.shrfid.pbkdf2.SecureHash
//import com.twitter.finagle.http.Status
//import com.twitter.util.Future
//import io.really.jwt.JWT
//import org.joda.time.DateTime
//import play.api.libs.json.Json
//
///**
//  * Created by jiejin on 9/09/2016.
//  */
//
//@Singleton
//object TokenServiceImpl
//
//@Singleton
//class TokenServiceImpl @Inject()(redisService: RedisService) extends TokenService {
//
//  override def validatePassword(a: String, b: String): Future[Boolean] = {
//    Future.value(SecureHash.validatePassword(a, b))
//  }
//
//  private def genJwt(u: AuthUserEntity): Future[String] = {
//    val payload = Json.obj("iss" -> Config.jwtIssuer,
//      "username" -> u.username,
//      "nbf" -> DateTime.now.toString())
//    Future.value(JWT.encode(Config.jwtSecretKey, payload))
//  }
//
//  private def genJwt(u: AuthUserEntity, isValid: Boolean): Future[String] = {
//    isValid match {
//      case true => genJwt(u)
//      case false => Future.value(Empty)
//    }
//  }
//
//  override def getToken(password: String, user: Option[AuthUserEntity]): Future[String] = {
//    user match {
//      case Some(u) =>
//        for {
//          isValid <- validatePassword(password, u.password)
//          token <- genJwt(u, isValid)
//        } yield token
//      case None => Future.value(Empty)
//    }
//  }
//
//  private def checkUserPermissions(userInfo: AuthUserInfo, permissionId: Int): Future[Boolean] = {
//    userInfo.isStaff match {
//      case true => Future.value(userInfo.permissions.map(u => u.id).contains(permissionId))
//      case false => Future.value(false)
//    }
//  }
//
//  def checkPermissionWithCallBack[T](token: String, permission: PermissionCode)(args: T, callback: (Username, T) => Future[(Int, String)]): Future[(Int, String)] = {
//    for {
//      response <- redisService.getUserInfo(token) flatMap {
//        case NotFound => Future.value((Status.Unauthorized.code, InvalidToken))
//        case a =>
//          val userInfo = Json.parse(a).as[AuthUserInfo]
//          checkUserPermissions(userInfo, permission) flatMap {
//            case true =>
//              callback(userInfo.username, args)
//            case false => Future.value((Status.Forbidden.code, NoPermission))
//          }
//      }
//    } yield response
//  }
//
//}
