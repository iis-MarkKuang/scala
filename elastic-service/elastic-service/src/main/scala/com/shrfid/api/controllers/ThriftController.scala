package com.shrfid.api.controllers


import com.google.inject.Inject
import com.shrfid.api.domains.reader.{BorrowRule => DomainBorrowRule, PenaltyRule => DomainPenaltyRule}
import com.shrfid.api.services.ElasticService
import com.twitter.finatra.thrift.Controller
import com.elastic_service.elasticServer.ElasticServerThrift._
import com.elastic_service.elasticServer._
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future
import com.shrfid.api.http.Elastic.book.item._
import com.shrfid.api.http.Elastic.reader.group._
import com.shrfid.api.http.Elastic.reader.level._
import com.shrfid.api.http.Elastic.reader.member._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.shrfid.api.services._
/**
  * Created by kuang on 2017/3/18.
  */
class ThriftController @Inject() (
  elasticService: ElasticService,
  bookService: BookService,
  branchService: BranchService,
  stackService: StackService,
  vendorService: VendorService,
  readerService: ReaderService,
  responseBuilder: ResponseBuilder
)
  extends Controller
  with ElasticServerThrift.BaseServiceIface {

  override val increment = handle(Increment) { args: Increment.Args =>
    var res = "2"

    val f = bookService.increment(args.a)
    f onSuccess(
      a => a match {
        case true =>
          res = "1"
        case false =>
          res = "0"
      }
    ) onFailure(
      ex => res = ex.getMessage()
    )

    Future(res)
  }

  override val updateBookInfoItem = handle(UpdateBookInfoItem) { args: UpdateBookInfoItem.Args =>
    bookService.updateBookInfoItem(args)
  }

  override val insertBookBranch = handle(InsertBookBranch) { args: InsertBookBranch.Args =>
    branchService.insertBookBranch(args)
  }

  override val findBookBranchById = handle(FindBookBranchById) { args: FindBookBranchById.Args =>
    branchService.findBookBranchById(args)
  }

  override val findBookBranches = handle(FindBookBranches) { args: FindBookBranches.Args =>
    branchService.findBranches(args)
  }

  override val updateBookBranchById = handle(UpdateBookBranchById) { args: UpdateBookBranchById.Args =>
    branchService.updateBookBranchById(args)
  }

  override val deleteBookBranchById = handle(DeleteBookBranchById) { args: DeleteBookBranchById.Args =>
    branchService.deleteBranchById(args)
  }

  override val deleteBookBranchBulk = handle(DeleteBookBranchBulk) { args: DeleteBookBranchBulk.Args =>
    branchService.deleteBranchBulk(args)
  }

  override val insertBookStack = handle(InsertBookStack) { args: InsertBookStack.Args =>
    stackService.insertStack(args)
  }

  override val findBookStackById = handle(FindBookStackById) { args: FindBookStackById.Args =>
    stackService.findStackById(args)
  }

  override val findBookStacks = handle(FindBookStacks) { args: FindBookStacks.Args =>
    stackService.findStacks(args)
  }

  override val updateBookStackById = handle(UpdateBookStackById) { args: UpdateBookStackById.Args =>
    stackService.updateStack(args)
  }

  override val deleteBookStackById = handle(DeleteBookStackById) { args: DeleteBookStackById.Args =>
    stackService.deleteStackById(args)
  }

  override val deleteBookStackBulk = handle(DeleteBookStackBulk) { args: DeleteBookStackBulk.Args =>
    stackService.deleteStackBulk(args)
  }

  override val insertReference = handle(InsertReference) { args: InsertReference.Args =>
    bookService.insertReference(args)
  }

  override val findReferenceById = handle(FindReferenceById) { args: FindReferenceById.Args =>
    bookService.findReferenceById(args)
  }

  override val findReference = handle(FindReference) { args: FindReference.Args =>
    bookService.findReference(args)
  }

  override val preGenBookItems = handle(PreGenBookItems) { args: PreGenBookItems.Args =>
    bookService.preGenItems(args)
  }

  override val findBookItemById = handle(FindBookItemById) { args: FindBookItemById.Args =>
    bookService.findItemById(args)
  }

  override val findBookItems = handle(FindBookItems) { args: FindBookItems.Args =>
    bookService.findItems(args)
  }

  override val insertBookItem = handle(InsertBookItem) { args: InsertBookItem.Args =>
    bookService.insertBookItem(args)
  }

  override val insertVendorMember = handle(InsertVendorMember) { args: InsertVendorMember.Args =>
    vendorService.insertVendor(args)
  }

  override val findVendorMemberById = handle(FindVendorMemberById) { args: FindVendorMemberById.Args =>
    vendorService.findVendorById(args)
  }

  override val findVendorMembers = handle(FindVendorMembers) { args: FindVendorMembers.Args =>
    vendorService.findVendors(args)
  }

  override val updateVendorMemberById = handle(UpdateVendorMemberById) { args: UpdateVendorMemberById.Args =>
    vendorService.updateVendorById(args)
  }

  override val deleteVendorMemberById = handle(DeleteVendorMemberById) { args: DeleteVendorMemberById.Args =>
    vendorService.deleteVendorById(args)
  }

  override val deleteVendorMemberBulk = handle(DeleteVendorMemberBulk) { args: DeleteVendorMemberBulk.Args =>
    vendorService.deleteVendorBulk(args)
  }

  override val insertVendorOrder = handle(InsertVendorOrder) { args: InsertVendorOrder.Args =>
    vendorService.insertOrder(args)
  }

  override val insertVendorOrderBulk = handle(InsertVendorOrderBulk) { args: InsertVendorOrderBulk.Args =>
    vendorService.insertOrderBulk(args)
  }

  override val findVendorOrderById = handle(FindVendorOrderById) { args: FindVendorOrderById.Args =>
    vendorService.findOrderById(args)
  }

  override val findVendorOrders = handle(FindVendorOrders) { args: FindVendorOrders.Args =>
    vendorService.findOrders(args)
  }

  override val updateVendorOrders = handle(UpdateVendorOrders) { args: UpdateVendorOrders.Args =>
    vendorService.patchOrders(args)
  }

  override val deleteVendorOrderById = handle(DeleteVendorOrderById) { args: DeleteVendorOrderById.Args =>
    vendorService.deleteOrderById(args)
  }

  override val deleteVendorOrderBulk = handle(DeleteVendorOrderBulk) { args: DeleteVendorOrderBulk.Args =>
    vendorService.deleteOrderBulk(args)
  }

  override val insertReaderLevel = handle(InsertReaderLevel) { args: InsertReaderLevel.Args =>
    readerService.insertLevel(args)
  }

  override val findReaderLevelById = handle(FindReaderLevelById) { args: FindReaderLevelById.Args =>
    readerService.findLevelById(args)
  }

  override val findReaderLevels = handle(FindReaderLevels) { args: FindReaderLevels.Args =>
    readerService.findLevels(args)
  }

  override val updateReaderLevelById = handle(UpdateReaderLevelById) { args: UpdateReaderLevelById.Args =>
    readerService.updateLevelById(args)
  }

  override val deleteReaderLevelById = handle(DeleteReaderLevelById) { args: DeleteReaderLevelById.Args =>
    readerService.deleteLevelById(args)
  }

  override val deleteReaderLevelBulk = handle(DeleteReaderLevelBulk) { args: DeleteReaderLevelBulk.Args =>
    readerService.deleteLevelBulk(args)
  }

  override val insertReaderGroup = handle(InsertReaderGroup) { args: InsertReaderGroup.Args =>
    readerService.insertGroup(args)
  }

  override val findReaderGroupById = handle(FindReaderGroupById) { args: FindReaderGroupById.Args =>
    readerService.findGroupById(args)
  }

  override val findReaderGroups = handle(FindReaderGroups) { args: FindReaderGroups.Args =>
    readerService.findGroups(args)
  }

  override val updateReaderGroup = handle(UpdateReaderGroup) { args: UpdateReaderGroup.Args =>
    readerService.updateGroupById(args)
  }

  override val deleteReaderGroupById = handle(DeleteReaderGroupById) { args: DeleteReaderGroupById.Args =>
    readerService.deleteGroupById(args)
  }

  override val deleteReaderGroupBulk = handle(DeleteReaderGroupBulk) { args: DeleteReaderGroupBulk.Args =>
    readerService.deleteGroupBulk(args)
  }

  override val insertReaderMember = handle(InsertReaderMember) { args: InsertReaderMember.Args =>
    readerService.insertMember(args)
  }

  override val insertReaderMemberBulk = handle(InsertReaderMemberBulk) { args: InsertReaderMemberBulk.Args =>
    readerService.insertMemberBulk(args)
  }

  override val findReaderMemberById = handle(FindReaderMemberById) { args: FindReaderMemberById.Args =>
    readerService.findMemberById(args)
  }

  override val findReaderMembers = handle(FindReaderMembers) { args: FindReaderMembers.Args =>
    readerService.findMembers(args)
  }

  override val patchReaderMemberBulk = handle(PatchReaderMemberBulk) { args: PatchReaderMemberBulk.Args =>
    readerService.updateMemberBulk(args)
  }

  override val patchReaderMemberById = handle(PatchReaderMemberById) { args: PatchReaderMemberById.Args =>
    readerService.updateMemberById(args)
  }

  override val borrowItems = handle(BorrowItems) { args: BorrowItems.Args =>
    readerService.borrowItems(args)
  }

  override val renewItems = handle(RenewItems) { args: RenewItems.Args =>
    readerService.renewItems(args)
  }

  override val findBorrowRecords = handle(FindBorrowRecords) { args: FindBorrowRecords.Args =>
    readerService.findBorrowRecords(args)
  }

  override val returnBooks = handle(ReturnBooks) { args: ReturnBooks.Args =>
    readerService.returnBooks(args)
  }

  override val reserveBooks = handle(ReserveBooks) { args: ReserveBooks.Args =>
    readerService.reserveBooks(args)
  }

  override val reserveBook = handle(ReserveBook) { args: ReserveBook.Args =>
    readerService.reserveBook(args)
  }
}
