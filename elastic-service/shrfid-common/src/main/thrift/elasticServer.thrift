namespace java com.elastic_service.elasticServer

include "requestStructs.thrift"
include "finatra_thrift_exceptions.thrift"

service ElasticServerThrift {

  /**
   * Increment a number\
   **/
  string increment(
    1: i32 a
  ) throws (
    1: finatra_thrift_exceptions.ServerError serverError,
    2: finatra_thrift_exceptions.UnknownClientIdError unknownClientIdError,
    3: finatra_thrift_exceptions.NoClientIdError noClientIdError
  )

  string updateBookInfoItem(
    1: string id,
    2: string item,
    3: optional string oldId
  )

  string insertBookBranch(
    1: requestStructs.Username user,
    2: requestStructs.PostBookBranchRequestThrift request
  )

  string findBookBranchById(
    1: requestStructs.Username user,
    2: requestStructs.GetBookBranchByIdRequestThrift request
  )

  string findBookBranches(
    1: requestStructs.GetBookBranchListRequestThrift request
  )

  string updateBookBranchById(
    1: requestStructs.Username user,
    2: requestStructs.PatchBookBranchByIdRequestThrift request
  )

  string deleteBookBranchById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteBookBranchByIdRequestThrift request
  )

  string deleteBookBranchBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteBookBranchBulkRequestThrift request
  )

  string insertBookStack(
    1: requestStructs.Username user,
    2: requestStructs.PostBookStackRequestThrift request
  )

  string findBookStackById(
    1: requestStructs.Username user,
    2: requestStructs.GetBookStackByIdRequestThrift request
  )

  string findBookStacks(
    1: requestStructs.GetBookStackListRequestThrift request
  )

  string updateBookStackById(
    1: requestStructs.Username user,
    2: requestStructs.PatchBookStackByIdRequestThrift request
  )

  string deleteBookStackById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteBookStackByIdRequestThrift request
  )

  string deleteBookStackBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteBookStackBulkRequestThrift request
  )

  string insertReference(
    1: string _id,
    2: string _source
  )

  string findReferenceById(
    1: string _id
  )

  string findReference(
    1: requestStructs.GetBookReferenceListRequestThrift request
  )

  string preGenBookItems(
    1: requestStructs.Username user,
    2: requestStructs.PostPreGenBookItemsRequestThrift request
  )

  string findBookItemById(
    1: requestStructs.Username user,
    2: requestStructs.GetBookItemByIdRequestThrift request
  )

  string findBookItems(
    1: requestStructs.Username user,
    2: requestStructs.GetBookItemListRequestThrift request
  )

  string insertBookItem(
    1: requestStructs.Username user,
    2: requestStructs.PostBookItemRequestThrift request
  )

  string insertVendorMember(
    1: requestStructs.Username user,
    2: requestStructs.PostVendorMemberRequestThrift request
  )

  string findVendorMemberById(
    1: requestStructs.Username user,
    2: requestStructs.PostVendorMemberRequestThrift request
  )

  string findVendorMembers(
    1: requestStructs.Username user,
    2: requestStructs.GetVendorMemberListRequestThrift request
  )

  string updateVendorMemberById(
    1: requestStructs.Username user,
    2: requestStructs.PatchVendorMemberByIdRequestThrift request
  )

  string deleteVendorMemberById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteVendorMemberByIdRequestThrift request
  )

  string deleteVendorMemberBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteVendorMemberBulkRequestThrift request
  )

  string insertVendorOrder(
    1: requestStructs.Username user,
    2: requestStructs.PostVendorOrderRequestThrift request
  )

  string insertVendorOrderBulk(
    1: requestStructs.Username user,
    2: requestStructs.PostVendorOrderBulkRequestThrift request
  )

  string findVendorOrderById(
    1: requestStructs.Username user,
    2: requestStructs.GetVendorOrderByIdRequestThrift request
  )

  string findVendorOrders(
    1: requestStructs.Username user,
    2: requestStructs.GetVendorOrderListRequestThrift request
  )

  string updateVendorOrders(
    1: requestStructs.Username user,
    2: requestStructs.PatchVendorOrderBulkRequestThrift request
  )

  string deleteVendorOrderById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteVendorOrderByIdRequestThrift request
//  ) throws (
//    1: finatra_thrift_exceptions.ClientError clientError
  )

  string deleteVendorOrderBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteVendorOrderBulkRequestThrift request
  )

  string insertReaderLevel(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderLevelRequestThrift request
  )

  string findReaderLevelById(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderLevelByIdRequestThrift request
  )

  string findReaderLevels(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderLevelListRequestThrift request
  )

  string updateReaderLevelById(
    1: requestStructs.Username user,
    2: requestStructs.PatchReaderLevelByIdRequestThrift request
  )

  string deleteReaderLevelById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteReaderLevelByIdRequestThrift request
  )

  string deleteReaderLevelBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteReaderLevelBulkRequestThrift request
  )

  string insertReaderGroup(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderGroupRequestThrift request
  )

  string findReaderGroupById(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderGroupByIdRequestThrift request
  )

  string findReaderGroups(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderGroupListRequestThrift request
  )

  string updateReaderGroup(
    1: requestStructs.Username user,
    2: requestStructs.PatchReaderGroupByIdRequestThrift request
  )
// u

  string deleteReaderGroupById(
    1: requestStructs.Username user,
    2: requestStructs.DeleteReaderGroupByIdRequestThrift request
  )

  string deleteReaderGroupBulk(
    1: requestStructs.Username user,
    2: requestStructs.DeleteReaderGroupBulkRequestThrift request
  )

  string insertReaderMember(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberRequestThrift request
  )

  string insertReaderMemberBulk(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberBulkRequestThrift request
  )

  string findReaderMemberById(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderMemberByIdRequestThrift request
  )

  string findReaderMembers(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderMemberListRequestThrift request
  )

  string patchReaderMemberBulk(
    1: requestStructs.Username user,
    2: requestStructs.PatchReaderMemberBulkRequestThrift request
  )

  string patchReaderMemberById(
    1: requestStructs.Username user,
    2: requestStructs.PatchReaderMemberByIdRequestThrift request
  )

  string borrowItems(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberBorrowItemsRequestThrift request
  )

  string renewItems(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberRenewItemsRequestThrift request
  )

  string findBorrowRecords(
    1: requestStructs.Username user,
    2: requestStructs.GetReaderMemberBorrowRecordListRequestThrift request
  )

  string returnBooks(
    1: requestStructs.Username user,
    2: requestStructs.PostBookItemsReturnRequestThrift request
  )

  string reserveBooks(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberReserveBooksRequestThrift request
  )

  string reserveBook(
    1: requestStructs.Username user,
    2: requestStructs.PostReaderMemberReserveBookRequestThrift request
  )
}
