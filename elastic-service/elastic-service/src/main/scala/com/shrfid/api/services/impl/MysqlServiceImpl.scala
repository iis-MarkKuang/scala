package com.shrfid.api.services.impl

import java.sql.Date
import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.shrfid.api.domains.auth.{AuthGroup, AuthGroupNested, IdUpdating}
import com.shrfid.api.domains.reader._
import com.shrfid.api.persistence.slick.auth.{AuthGroupPermissionRepository, _}
import com.shrfid.api.persistence.slick.readable._
import com.shrfid.api.persistence.slick.reader._
import com.shrfid.api.persistence.slick.vendor.{VendorMemberRepository, VendorOrderRepository}
import com.shrfid.api.services.MysqlService
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import slick.driver.MySQLDriver.api._
/**
  * Created by jiejin on 9/09/2016.
  */
@Singleton
object MysqlServiceImpl

@Singleton
class MysqlServiceImpl @Inject()(authUserRepository: AuthUserRepository,
                                 authGroupRepository: AuthGroupRepository,
                                 authUserGroupRepository: AuthUserGroupRepository,
                                 authPermissionRepository: AuthPermissionRepository,
                                 authUserPermissionRepository: AuthUserPermissionRepository,
                                 authGroupPermissionRepository: AuthGroupPermissionRepository,
                                 bookStackRepository: BookStackRepository,
                                 bookItemRepository: BookItemRepository,
                                 readerLevelRepository: ReaderLevelRepository,
                                 readerGroupRepository: ReaderGroupRepository,
                                 readerMemberRepository: ReaderMemberRepository,
                                 readerMemberBorrowHistoryRepository: ReaderMemberBorrowHistoryRepository,
                                 readerMemberOrderRepository: ReaderMemberOrderRepository,
                                 readerMemberGroupRepository: ReaderMemberGroupRepository,
                                 vendorMemberRepository: VendorMemberRepository,
                                 vendorOrderRepository: VendorOrderRepository) extends MysqlService {


  override def createTable: Future[Unit] = {
    for {
    //_ <- bookStackRepository.dal.createTable()
    //_ <- authUserRepository.dal.createTable()
    //_ <- authGroupRepository.dal.createTable()
    //_ <- authUserGroupRepository.dal.createTable()
    //_ <- authPermissionRepository.dal.createTable()
    //_ <- authUserPermissionRepository.dal.createTable()
    //_ <- authGroupPermissionRepository.dal.createTable()
    //_ <- readerLevelRepository.dal.createTable()
    // _ <- readerMemberRepository.dal.createTable()
    // _ <- readerGroupRepository.dal.createTable()
    // _ <- readerMemberGroupRepository.dal.createTable()
    // _ <- readerMemberBorrowHistoryRepository.dal.createTable()
    //_ <- readerMemberOrderRepository.dal.createTable()
    // _ <- vendorMemberRepository.dal.createTable()
      _ <- vendorOrderRepository.dal.createTable()
    } yield Unit
  }


  override def findAuthUser(id: Int): Future[Option[AuthUserEntity]] = {
    authUserRepository.dal.findById(id).onSuccess(u => Future.value(u))
  }

  override def findAuthUser(username: String): Future[Option[AuthUserEntity]] = {
    authUserRepository.dal.findByFilter(a => a.username === username).map(_.headOption)
  }

  override def findAuthUsers(limit: Int, offset: Int, username: Option[String], identity: Option[String],
                             fullName: Option[String], gender: Option[String], isSuperuser: Option[Boolean],
                             isActive: Option[Boolean], ordering: Option[String]): Future[(Int, Seq[AuthUserEntity])] = {
    for {
      response <- authUserRepository.dal.countByFilter(username, identity, fullName, gender, isSuperuser, Some(true), isActive) flatMap {
        case 0 => Future.value(EmptyList)
        case c => for {
          content <- authUserRepository.dal.findAllByFilter(limit, offset, username, identity, fullName, gender, isSuperuser, Some(true), isActive, ordering)
        } yield (c, content)
      }
    } yield response
  }

  override def updateAuthUserLastLogin(id: Int) = {
    authUserRepository.dal.updateLastLogin(id)
  }

  override def insertAuthUser(username: String, identity: String = "", fullName: String = "", gender: String, dob: Option[Date],
                              email: Option[String], mobile: Option[String], address: Option[String], postcode: Option[String],
                              profileUrl: Option[String]): Future[Int] = {
    val now = DateTime.now(DateTimeZone.forOffsetHours(8))
    authUserRepository.dal.insert(AuthUserEntity(0, username, identity, Config.defaultPassword, fullName, gender, dob, email,
      mobile, address, postcode, profileUrl, false, true, true, now, now, now))
  }

  override def updateAuthUser(id: Int, identity: String = "", fullName: String = "", gender: String, dob: Option[Date],
                              email: String, mobile: String, address: String, postcode: String,
                              profileUrl: String, isStaff: Boolean, isActive: Boolean): Future[Int] = {
    authUserRepository.dal.updateUser(id, identity, fullName, gender, dob, optionStr(email), optionStr(mobile), optionStr(address), optionStr(postcode), optionStr(profileUrl),
      isStaff, isActive)
  }

  override def deleteAuthUser(id: Int): Future[Int] = {
    authUserRepository.dal.deleteById(id)
  }

  override def findAuthPermissions: Future[Seq[AuthPermissionEntity]] = {
    authPermissionRepository.dal.findAll(-1, 0)
  }

  override def findAuthUserPermissions(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[AuthPermissionEntity]] = {
    (isSuperuser, isStaff) match {
      case (true, _) =>
        authPermissionRepository.dal.findAll(-1, 0)
      case (_, true) =>
        for {
          up <- authUserPermissionRepository.dal.findPermissionsByUserId(userId) //.onSuccess{ a=> println(a); Future.value(a)}
          gp <- authGroupPermissionRepository.dal.findPermissionsByUserId(userId) //.onSuccess{ a=> println(a); Future.value(a)}
        } yield (up ++ gp).distinct.sortBy(p => (p.appLabel, p.id))
      case (false, false) =>
        Future.value(Seq())
    }
  }

  override def findAuthGroupPermissionsByUserId(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[AuthGroupPermissionEntity]] = {
    (isSuperuser, isStaff) match {
      case (true, _) =>
        Future.value(Seq())
      case (_, true) =>
        authGroupPermissionRepository.dal.findGroupPermissionsByUserId(userId)
      case (false, false) =>
        Future.value(Seq())
    }
  }

  override def findAuthUserPermissionIds(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[Int]] = {
    (isSuperuser, isStaff) match {
      case (true, _) =>
        authPermissionRepository.dal.findAllIds
      case (_, true) =>
        authUserPermissionRepository.dal.findPermissionIdsByUserId(userId)
      case (false, false) =>
        Future.value(Seq())
    }
  }

  override def findAuthUserPassword(id: Int): Future[Option[String]] = {
    authUserRepository.dal.findPassword(id)
  }

  override def updateAuthUserPassword(userId: Int, password: String): Future[Int] = {
    authUserRepository.dal.updateUserPassword(userId, password)
  }

  override def findAuthGroupPermissions(id: Int): Future[Seq[AuthPermissionEntity]] = {
    authGroupPermissionRepository.dal.findPermissionsByUserId(id)
  }

  override def findAuthGroups(queryFilter: Map[String, _]): Future[(Int, Seq[_])] = {
    val limit = queryFilter("limit").asInstanceOf[Int]
    val offset = queryFilter("offset").asInstanceOf[Int]
    for {
      response <- authGroupRepository.dal.count flatMap {
        case 0 => Future.value(EmptyList)
        case c => queryFilter("nested").asInstanceOf[Boolean] match {
          case false => for {content <- authGroupRepository.dal.findAll(limit, offset)} yield (c, AuthGroup.toDomain(content))
          case true => for {content <- authGroupRepository.dal.findAllNested(limit, offset)} yield (c, AuthGroupNested.toDomain(content))
        }
      }
    } yield response
  }

  override def findAuthGroupsNested(limit: Int, offset: Int): Future[(Int, Seq[AuthGroupNestedEntity])] = {
    for {
      response <- authGroupRepository.dal.count flatMap {
        case 0 => Future.value(EmptyList)
        case c => for {
          content <- authGroupRepository.dal.findAllNested(limit, offset)
        } yield (c, content)
      }
    } yield response
  }

  override def insertAuthGroup(name: String, groupPermissionIds: Seq[Int]): Future[Int] = {
    for {
      groupId <- authGroupRepository.dal.insert(AuthGroupEntity(0, name))
      _ <- authGroupPermissionRepository.dal.insert(groupPermissionIds.map(a => AuthGroupPermissionEntity(0, groupId, a)))
    } yield groupId
  }

  override def updateAuthGroup(id: Int, name: String, insert: Seq[Int], delete: Seq[Int]): Future[Int] = {
    for {
      group <- authGroupRepository.dal.update(AuthGroupEntity(id, name))
      delete <- authGroupPermissionRepository.dal.deleteByFilter(a => a.permissionId.inSetBind(delete) && a.groupId === id)
      insert <- authGroupPermissionRepository.dal.insert(insert.map(a => AuthGroupPermissionEntity(0, id, a)))
    } yield group + delete + insert.sum
  }

  override def deleteAuthGroup(id: Int): Future[Int] = {
    for {
      r <- authGroupRepository.dal.deleteById(id)
    } yield r
  }

  /*override def findReaderMembers(limit: Int, offset: Int, barcode: Option[String], identity: Option[String],
                                 fullName: Option[String], gender: Option[String], dob: Option[String],
                                 readerLevel: Option[String], readerGroup: Option[String],
                                 createAt: Option[String], isActive: Option[Boolean], isSuspend: Option[Boolean],
                                 logicOp: String, ordering: Option[String]): Future[(Int, Seq[ReaderMember])] = {

    for {
      response <- readerMemberRepository.dal.countByFilter(barcode, identity, fullName, gender, dob,
        readerLevel, readerGroup, createAt, isActive, isSuspend, logicOp) flatMap {
        case 0 => Future.value(EmptyList)
        case c => for {
          content <- readerMemberRepository.dal.findAllByFilter(limit, offset, barcode, identity, fullName,
            gender, dob, readerLevel, readerGroup, createAt, isActive, isSuspend, logicOp, ordering)
        } yield (c, content)
      }
    } yield response

  }*/

  override def inactiveReaderMember(by: String, ids: Seq[Int]): Future[Int] = {
    by match {
      case "group_id" => for {
        rmg <- readerMemberGroupRepository.dal.findByFilter(a => a.groupId inSetBind ids)
        memberIds = rmg.map(_.memberId)
        r <- readerMemberRepository.dal.activeMember(memberIds, false)
      } yield r
      case "member_id" => readerMemberRepository.dal.activeMember(ids, false)
    }
  }

  /*override def updateReaderMember(memberId: Int, barcode: String, rfid: Option[String], identity: String,
                                  fullName: String, gender: String, dob: Date, email: Option[String],
                                  mobile: Option[String], address: Option[String], postcode: Option[String],
                                  profileUrl: Option[String], isActive: Boolean, restoreAt: Option[Date], levelId: Int, groupIds: IdUpdating) = {

    for {
      r <- readerMemberRepository.dal.updateMember(memberId, barcode, rfid, identity, fullName, gender, dob, email, mobile, address, postcode, profileUrl,
        isActive, restoreAt, levelId)
      readerGroupDeleted <- readerMemberGroupRepository.dal.deleteByFilter(a => a.groupId.inSetBind(groupIds.delete) && a.memberId === memberId)
      readerGroupInserted <- readerMemberGroupRepository.dal.insert(groupIds.insert.map(g => ReaderMemberGroupEntity(0, memberId, g)))
    } yield r
  }*/

  override def updateReaderMemberCard(id: Int, barcode: String, rfid: String): Future[Int] = {
    for {
      response <- readerMemberRepository.dal.updateMemberCard(id, barcode, optionStr(rfid))
    } yield response
  }

  override def updateReaderMemberSuspend(id: Int, days: Int): Future[Int] = {
    for {
      response <- readerMemberRepository.dal.suspendMember(id, days)
    } yield response
  }


  /*override def findReaderMember(param: String, by: String): Future[Option[ReaderMemberDetail]] = {
    by match {
      case "id" => readerMemberRepository.dal.findDetail(_.id === param.toInt)
      case "barcode" => readerMemberRepository.dal.findDetail(_.barcode === param)
    }
  }*/







  override def updateAuthUserPermission(userId: Int, permissionIds: IdUpdating, groupIds: IdUpdating): Future[Int] = {
    for {
      userGroupDeleted <- authUserGroupRepository.dal.deleteByFilter(a => a.groupId.inSetBind(groupIds.delete) && a.userId === userId)
      userGroupInserted <- authUserGroupRepository.dal.insert(groupIds.insert.map(g => AuthUserGroupEntity(0, userId, g)))
      userPermissionDeleted <- authUserPermissionRepository.dal.deleteByFilter(a => a.permissionId.inSetBind(permissionIds.delete) && a.userId === userId)
      userPermissionInserted <- authUserPermissionRepository.dal.insert(permissionIds.insert.map(p => AuthUserPermissionEntity(0, userId, p)))
    } yield if ((userGroupDeleted + userGroupInserted.sum + userPermissionDeleted + userPermissionInserted.sum) > 0) 1 else 0
  }


  /*override def preGenBookItems(request: PostPreGenBookItemsRequest): Future[(Int, String)] = {
    for {
      maxBarcode <- bookItemRepository.dal.findMaxBarcode(request.categoryId)
      now = DateTime.now(DateTimeZone.forOffsetHours(8))
      count = barcodeDeFmt(maxBarcode.getOrElse("0"), request.categoryId)
      start = count + 1
      end = count + request.quantity
      items = (start to end).toList.map(i => BookItemEntity(0, None, None, barcodeFmt(i, request.categoryId), None, request.categoryId,
        request.stackId, None, None, true, true, None, now, now, None))
      res <- bookItemRepository.dal.insert(items)
    } yield res match {
      case error: Seq[Int] if error.contains(0) =>println(error); (Status.NotAcceptable.code, "")
      case other => (Status.Ok.code, s""" {"start": "${barcodeFmt(start, request.categoryId)}", "end": "${barcodeFmt(end, request.categoryId)}" }""")
    }
  }*/


  override def findReaderBorrowRecords(param: String, by: String, recordOption: String,
                                       limit: Int, offset: Int): Future[(Int, Seq[ReaderMemberBorrowRecord])] = {
    for {
      count <- readerMemberBorrowHistoryRepository.dal.countByFilter(param, by, recordOption)
      records <- readerMemberBorrowHistoryRepository.dal.findByFilter(param, by, recordOption, limit, offset)
    } yield (count, records)

  }

  override def countReaderBorrowRecords(readerId: Int, recordOption: String) = {
    readerMemberBorrowHistoryRepository.dal.countByFilter(readerId.toString, "id", recordOption)
  }

  private def returnBooksById(bookIds: Seq[Int]): Future[Int] = {
    readerMemberBorrowHistoryRepository.dal.updateReturnAt(bookIds)
  }

  private def returnBooksByBarcode(bookBarcodes: Seq[String]): Future[Int] = {
    for {
      bookIds <- bookItemRepository.dal.findIdByBarcode(bookBarcodes)
      result <- returnBooksById(bookIds)
    } yield result
  }

  private def updateBookItemAndRecords(books: Seq[BookItemEntity]) = {

    def toggledPartition[A](xs: List[A])(p: A => Boolean): List[List[A]] = {
      if (xs.isEmpty) Nil
      else xs span p match {
        case (a, b) => a :: toggledPartition(b)(x => !p(x))
      }
    }

    toggledPartition[BookItemEntity](books.toList)(_.isAvailable == true) match {
      case List(List(), borrowed: List[BookItemEntity]) =>
        readerMemberBorrowHistoryRepository.dal.updateReturnAt(borrowed.map(_.id))
      case List(available: List[BookItemEntity]) =>
      //bookItemRepository.dal.updateAvailability(available.map(_.id))
      case List(available, borrowed) =>
        for {
          _ <- readerMemberBorrowHistoryRepository.dal.updateReturnAt(borrowed.map(_.id))
        //  _ <- bookItemRepository.dal.updateAvailability(available.map(_.id))
        } yield Unit
    }

  }

  override def borrowBooks(readerId: Int, bookBarcodes: Seq[String], dueTime: DateTime): Future[Int] = {

    for {
      books <- bookItemRepository.dal.findByFilter(_.barcode.inSetBind(bookBarcodes))
      //_ <- updateBookItemAndRecords(books)
      result <- readerMemberBorrowHistoryRepository.dal.insert(books.map(_.id).map(bIds => ReaderMemberBorrowHistoryEntity(0, bIds, readerId,
        DateTime.now(DateTimeZone.forOffsetHours(8)), dueTime, None)))
    } yield result.product
  }

  override def returnBooks(bookBarcodes: Seq[String]): Future[Int] = {
    returnBooksByBarcode(bookBarcodes)
  }


  /*override def findVendorOrders(queryFilter: Map[String, _]): Future[(Int, Seq[VendorOrder])] = {
    for {
      response <- vendorOrderRepository.dal.countByFilter(queryFilter) flatMap {
        case 0 => Future.value(EmptyList)
        case c => for {
          content <- vendorOrderRepository.dal.findAllByFilter(queryFilter)
        } yield (c, content)
      }
    } yield response
  }*/

  /*def updateVendorOrders(orders: Seq[PatchVendorOrderRow]): Future[Int] = {
    Future.collect(orders.map(o => vendorOrderRepository.dal.updateOrder(o))).map(_.product)
  }*/


  /*override def upsertBookItem(request: PostBookItemRequest, reference: String, title: Option[String], clc: Option[String], userId: Int): Future[(Int, Int, Option[String])] = {
    val titleFixed = title match {
      case Some(t) => Some(t)
      case None => Some(request.info.get.题名与责任者.正题名)
    }
    val clcFixed =  clc match {
      case Some(c) => Some(c.take(Config.clcLength))
      case None => Some(request.info.get.中国图书馆图书分类法分类号.take(Config.clcLength))
    }
    for {
      bookItem <- bookItemRepository.dal.findByFilter(_.barcode === request.barcode)
      bookIndex <- bookItemRepository.dal.findBookIndex(clcFixed, if (clcFixed == bookItem.head.clc) bookItem.head.bookIndex else None)
      id <- bookItemRepository.dal.upsert(BookItemEntity(0, Some(reference), titleFixed,
        request.barcode, request.rfid, request.categoryId, request.stackId, clcFixed, Some(bookIndex),
        bookItem.head.isAvailable, bookItem.head.isActive, Some(userId), now, now, bookItem.head.description))
    } yield (id, bookIndex, bookItem.head.reference)

  }*/
}
