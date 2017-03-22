package com.shrfid.api.services

import java.sql.Date

import com.shrfid.api.domains.auth.IdUpdating
import com.shrfid.api.domains.reader._
import com.shrfid.api.persistence.slick.auth._
import com.twitter.util.Future
import org.joda.time.DateTime

/**
  * Created by jiejin on 9/09/2016.
  */
trait MysqlService {

  def createTable: Future[Unit]

  def findAuthUser(id: Int): Future[Option[AuthUserEntity]]

  def findAuthUser(username: String): Future[Option[AuthUserEntity]]

  def findAuthUsers(limit: Int, offset: Int, username: Option[String], identity: Option[String],
                    fullName: Option[String], gender: Option[String], isSuperuser: Option[Boolean], isActive: Option[Boolean],
                    ordering: Option[String]): Future[(Int, Seq[AuthUserEntity])]

  def insertAuthUser(username: String, identity: String = "", fullName: String = "",
                     gender: String, dob: Option[Date], email: Option[String], mobile: Option[String], address: Option[String],
                     postcode: Option[String], profileUrl: Option[String]): Future[Int]

  def updateAuthUser(id: Int, identity: String = "", fullName: String = "", gender: String, dob: Option[Date],
                     email: String, mobile: String, address: String, postcode: String,
                     profileUrl: String, isStaff: Boolean, isActive: Boolean): Future[Int]

  def deleteAuthUser(id: Int): Future[Int]

  def updateAuthUserLastLogin(id: Int): Future[Int]

  def updateAuthUserPassword(userId: Int, password: String): Future[Int]

  def findAuthUserPassword(id: Int): Future[Option[String]]

  def findAuthPermissions: Future[Seq[AuthPermissionEntity]]

  def findAuthGroups(queryFilter: Map[String, _]): Future[(Int, Seq[_])]

  def findAuthGroupsNested(limit: Int, offset: Int): Future[(Int, Seq[AuthGroupNestedEntity])]

  def insertAuthGroup(name: String, groupPermissionIds: Seq[Int]): Future[Int]

  def deleteAuthGroup(id: Int): Future[Int]

  def updateAuthGroup(id: Int, name: String, insert: Seq[Int], delete: Seq[Int]): Future[Int]

  def findAuthUserPermissions(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[AuthPermissionEntity]]

  def findAuthUserPermissionIds(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[Int]]

  def findAuthGroupPermissionsByUserId(userId: Int, isSuperuser: Boolean, isStaff: Boolean): Future[Seq[AuthGroupPermissionEntity]]

  def findAuthGroupPermissions(id: Int): Future[Seq[AuthPermissionEntity]]


  /*def findReaderMembers(limit: Int, offset: Int, barcode: Option[String], identity: Option[String],
                        fullName: Option[String], gender: Option[String], dob: Option[String], readerLevel: Option[String], readerGroup: Option[String],
                        createAt: Option[String], isActive: Option[Boolean], isSuspend: Option[Boolean], logicOp: String, ordering: Option[String]): Future[(Int, Seq[ReaderMember])]

  def findReaderMember(param: String, by: String): Future[Option[ReaderMemberDetail]]*/

  def findReaderBorrowRecords(param: String, by: String, recordOption: String, limit: Int, offset: Int): Future[(Int, Seq[ReaderMemberBorrowRecord])]

  def countReaderBorrowRecords(readerId: Int, recordOption: String): Future[Int]





  def inactiveReaderMember(by: String, ids: Seq[Int]): Future[Int]

  def updateReaderMemberCard(id: Int, barcode: String, rfid: String): Future[Int]

  def updateReaderMemberSuspend(id: Int, days: Int): Future[Int]


  def updateAuthUserPermission(userId: Int, permissionIds: IdUpdating, groupIds: IdUpdating): Future[Int]

  //def preGenBookItems(request: PostPreGenBookItemsRequest): Future[(Int, String)]


  def borrowBooks(readerId: Int, bookBarcodes: Seq[String], dueTime: DateTime): Future[Int]

  def returnBooks(bookBarcodes: Seq[String]): Future[Int]

}
