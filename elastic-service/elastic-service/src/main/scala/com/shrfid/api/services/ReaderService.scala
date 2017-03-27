package com.shrfid.api.services

import java.sql.Date

import com.elastic_service.elasticServer.ElasticServerThrift._
import com.google.inject.{Inject, Singleton}
import com.shrfid.api.domains.reader.{BorrowRule => DomainBorrowRule, PenaltyRule => DomainPenaltyRule}
import com.shrfid.api.http.Elastic.book.item.PostBookItemsReturnRequest
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Created by kuang on 2017/3/27.
  */
@Singleton
class ReaderService @Inject()(
  elasticService: ElasticService
) {
  def insertLevel(args : InsertReaderLevel.Args) = {
    elasticService.insertReaderLevel(args.user, PostReaderLevelRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4.asInstanceOf[DomainBorrowRule],
      args.request._5.asInstanceOf[DomainPenaltyRule],
      args.request._6
    )).map(a => a._2)
  }

  def findLevels(args: FindReaderLevels.Args) = {
    elasticService.findReaderLevels(args.user, GetReaderLevelListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  def findLevelById(args: FindReaderLevelById.Args) = {
    elasticService.findReaderLevelById(args.user, GetReaderLevelByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def updateLevelById(args: UpdateReaderLevelById.Args) = {
    elasticService.updateReaderLevelById(args.user, PatchReaderLevelByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5.asInstanceOf[Option[BorrowRule]],
      args.request._6.asInstanceOf[Option[PenaltyRule]],
      args.request._7,
      args.request._8
    )).map(a => a._2)
  }

  def deleteLevelById(args: DeleteReaderLevelById.Args) = {
    elasticService.deleteReaderLevelById(args.user, DeleteReaderLevelByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def deleteLevelBulk(args: DeleteReaderLevelBulk.Args) = {
    elasticService.deleteReaderLevelBulk(args.user, DeleteReaderLevelBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def insertGroup(args: InsertReaderGroup.Args) = {
    elasticService.insertReaderGroup(args.user, PostReaderGroupRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  def findGroupById(args: FindReaderGroupById.Args) = {
    elasticService.findReaderGroupById(args.user, GetReaderGroupByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def findGroups(args: FindReaderGroups.Args) = {
    elasticService.findReaderGroups(args.user, GetReaderGroupListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7
    )).map(a => a._2)
  }

  def updateGroupById(args: UpdateReaderGroup.Args) = {
    elasticService.updateReaderGroup(args.user, PatchReaderGroupByIdRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5
    )).map(a => a._2)
  }

  def deleteGroupById(args: DeleteReaderGroupById.Args) = {
    elasticService.deleteReaderGroupById(args.user, DeleteReaderGroupByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def deleteGroupBulk(args: DeleteReaderGroupBulk.Args) = {
    elasticService.deleteReaderGroupBulk(args.user, DeleteReaderGroupBulkRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def insertMember(args: InsertReaderMember.Args) = {
    elasticService.insertReaderMember(args.user, PostReaderMemberRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
      args.request._11,
      args.request._12,
      args.request._13,
      args.request._14,
      new DateTime(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss").parseDateTime(args.request._15))
    )).map(a => a._2)
  }

  def insertMemberBulk(args: InsertReaderMemberBulk.Args) = {
    elasticService.insertReaderMemberBulk(args.user, PostReaderMemberBulkRequest(
      args.request._1,
      args.request._2.asInstanceOf[Seq[ReaderMemberInsertion]]
    )).map(a => a._2)
  }

  def findMemberById(args: FindReaderMemberById.Args) = {
    elasticService.findReaderMemberById(args.user, GetReaderMemberByIdRequest(
      args.request._1,
      args.request._2
    )).map(a => a._2)
  }

  def findMembers(args: FindReaderMembers.Args) = {
    elasticService.findReaderMembers(args.user, GetReaderMemberListRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5,
      args.request._6,
      args.request._7,
      args.request._8,
      args.request._9,
      args.request._10,
      args.request._11,
      args.request._12,
      args.request._13,
      args.request._14,
      args.request._15,
      args.request._16,
      args.request._17,
      args.request._18,
      args.request._19,
      args.request._20,
      args.request._21
    )).map(a => a._2)
  }

  def updateMemberBulk(args: PatchReaderMemberBulk.Args) = {
    elasticService.patchReaderMemberBulk(args.user, PatchReaderMemberBulkRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4,
      args.request._5
    )).map(a => a._2)
  }

  def updateMemberById(args: PatchReaderMemberById.Args) = {
    elasticService.patchReaderMemberById(args.user, PatchReaderMemberByIdRequest.toDomain(args.request)).map(a => a._2)
  }

  def borrowItems(args: BorrowItems.Args) = {
    elasticService.borrowItems(args.user, PostReaderMemberBorrowItemsRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  def renewItems(args: RenewItems.Args) = {
    elasticService.renewItems(args.user, PostReaderMemberRenewItemsRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  def findBorrowRecords(args: FindBorrowRecords.Args) = {
    elasticService.findBorrowRecords(args.user, GetReaderMemberBorrowRecordListRequest(
      args.request._1,
      args.request._2,
      args.request._3.asInstanceOf[Option[String]],
      args.request._4,
      args.request._5,
      args.request._6
    )).map(a => a._2)
  }

  def returnBooks(args: ReturnBooks.Args) = {
    elasticService.returnBooks(args.user, PostBookItemsReturnRequest(
      args.request._1,
      args.request._2,
      args.request._3
    )).map(a => a._2)
  }

  def reserveBooks(args: ReserveBooks.Args) = {
    elasticService.reserveBooks(args.user, PostReaderMemberReserveItemsRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }

  def reserveBook(args: ReserveBook.Args) = {
    elasticService.reserveBook(args.user, PostReaderMemberReserveItemRequest(
      args.request._1,
      args.request._2,
      args.request._3,
      args.request._4
    )).map(a => a._2)
  }
}
