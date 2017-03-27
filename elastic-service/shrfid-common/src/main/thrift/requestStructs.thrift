namespace java com.elastic_service.requestStructs

typedef i32 StatusCode
typedef i32 Count
typedef string Docs
typedef string Id
typedef string Username

struct BookReferenceThrift {
    1: required string ISBN;
    2: required string ISSN;
    3: required string keywords;
    4: required string datetime;
}

struct PenaltyRuleThrift {
    1: string method;
    2: double expire_factor;
    3: double lost_factor
}

struct BorrowRuleThrift {
    1: i32 quantity;
    2: i32 day;
    3: bool can_renew = true;
    4: bool can_book = true
}

struct ReaderMemberInsertionThrift {
    1: required string barcode;
    2: required string rfid;
    3: required string levelId;
    4: required list<string> groupIds;
    5: string identity;
    6: string fullName;
    7: string gender;
    8: string dob;
    9: string email;
    10: string mobile;
    11: string address;
    12: string postcode;
    13: string profileImage;
    14: string createAt
}

struct CommonResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct UpsertResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct GetResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct DeleteResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct SearchResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct IndesResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct SearchDocsResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct UpdateResponseThrift {
    1: required StatusCode statusCode;
    2: required Docs docs
}

struct PostBookBranchRequestThrift {
    1: required string Authorization;
    2: required string name;
    3: optional bool isActive = true;
    4: optional bool isRoot = false;
    5: optional string description = ""
}

struct GetBookBranchByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
}

struct GetBookBranchListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string name;
    6: optional bool isActive;
    7: optional bool isRoot;
    8: optional string ordering
}

struct PatchBookBranchByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string name;
    4: optional bool isActive;
    5: optional bool isRoot;
    6: optional string description;
    7: required string datetime
}

struct DeleteBookBranchByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteBookBranchBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids
}

struct PostBookStackRequestThrift {
    1: required string Authorization;
    2: required string name;
    3: optional bool isActive = true;
    4: required string branchId;
    5: optional string description = ""
}

struct GetBookStackByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct GetBookStackListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string name;
    6: optional string branch;
    7: optional bool isActive;
    8: optional string ordering
}

struct PatchBookStackByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string name;
    4: optional bool isActive;
    5: optional string branchId;
    6: optional string description;
    7: required string datetime
}

struct DeleteBookStackByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteBookStackBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids
}

struct GetBookReferenceListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string keyword;
    6: optional string author;
    7: optional string title;
    8: optional string isbn;
    9: optional string publisher;
    10: optional string clc;
    11: optional string publishYear;
    12: optional string topic;
    13: optional string ordering
}

struct PostPreGenBookItemsRequestThrift {
    1: required string Authorization;
    2: required i32 categoryId;
    3: required string stackId;
    4: required i32 quantity
}

struct GetBookItemListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string reference;
    6: optional string title;
    7: optional string barcode;
    8: optional string rfid;
    9: optional i32 categoryId;
    10: optional string stackId;
    11: optional string clc;
    12: optional bool isAvailable;
    13: optional bool isActive;
    14: optional string ordering
}

struct GetBookItemByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct PostBookItemRequestThrift {
    1: required string Authorization;
    2: required string barcode;
    3: optional string rfid;
    4: optional string reference;
    5: required string stackId;
    6: optional BookReferenceThrift info;
    7: optional i32 categoryId = 1
}

struct PostVendorMemberRequestThrift {
    1: required string Authorization;
    2: required string name;
    3: optional bool isActive = true;
    4: optional string description = "";
}

struct GetVendorMemberByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct GetVendorMemberListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string name;
    6: optional bool isActive;
    7: optional string ordering
}

struct PatchVendorMemberByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string name;
    4: optional bool isActive;
    5: optional string description;
    6: string datetime
}

struct DeleteVendorMemberByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteVendorMemberBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids
}

struct PostVendorOrderRequestThrift {
    1: required string Authorization;
    2: string title;
    3: string author;
    4: string publisher;
    5: string isbn;
    6: double price;
    7: i32 quantity;
    8: double total;
    9: string description;
    10: string vendorId;
    11: string orderDate
}

struct PutVendorOrderRowThrift {
    1: string title;
    2: string author;
    3: string publisher;
    4: string isbn;
    5: double price;
    6: i32 quantity;
    7: double totle;
    8: string description;
    9: string vendorId;
    10: string orderDate
}

struct PostVendorOrderBulkRequestThrift {
    1: required string Authorization;
    2: list<PutVendorOrderRowThrift> data
}

struct GetVendorOrderByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}



struct GetVendorOrderListRequestThrift {
    1: required string Authorization;
    2: optional i32 limit = 100;
    3: optional i32 offset = 0;
    4: optional string id;
    5: optional string vendorId;
    6: optional string isbn;
    7: optional string orderDate;
    8: optional string createAt;
    9: optional bool isArrived;
    10: optional string ordering
}

struct PatchVendorOrderRowThrift {
    1: required string id;
    2: optional string title;
    3: optional string author;
    4: optional string publisher;
    5: optional string isbn;
    6: optional double price;
    7: optional i32 quantity;
    8: optional double total;
    9: optional double actualPrice;
    10: optional i32 actualQuantity;
    11: optional double actualTotal;
    12: optional string orderDate;
    13: optional string arriveAt;
    14: optional string description;
    15: optional string vendorId
}

struct PatchVendorOrderBulkRequestThrift {
    1: required string Authorization;
    2: required list<PatchVendorOrderRowThrift> data
}


struct DeleteVendorOrderByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteVendorOrderBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids;
}

struct PostReaderLevelRequestThrift {
    1: required string Authorization;
    2: string name;
    3: double deposit;
    4: BorrowRuleThrift borrowRule;
    5: PenaltyRuleThrift penaltyRule;
    6: optional string description = ""
}

struct GetReaderGroupByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct GetReaderLevelListRequestThrift {
    1: required string Authorization;
    2: i32 limit = 100;
    3: i32 offset = 0;
    4: optional string id;
    5: optional string name;
    6: optional bool isActive;
    7: optional string ordering
}

struct GetReaderLevelByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct PostBookItemsReturnRequestThrift {
    1: required string Authorization;
    2: required list<string> bookBarcodes;
    3: string location
}

struct GetReaderMemberBorrowRecordListRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string record = "current";
    4: i32 limit = 100;
    5: i32 offset = 0;
    6: optional string ordering
}

struct PatchReaderLevelByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string name;
    4: optional double deposit;
    5: optional BorrowRuleThrift borrowRule;
    6: optional PenaltyRuleThrift penaltyRule;
    7: optional string description;
    8: string datetime
}


struct DeleteReaderLevelByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteReaderLevelBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids
}

struct PostReaderGroupRequestThrift {
    1: required string Authorization;
    2: string name;
    3: bool isActive = true;
    4: string description = ""
}

struct GetReaderGroupListRequestThrift {
    1: required string Authorization;
    2: i32 limit = 100;
    3: i32 offset = 0;
    4: optional string id;
    5: optional string name;
    6: optional bool isActive;
    7: optional string ordering
}

struct PatchReaderGroupByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: optional string name;
    4: optional bool isActive;
    5: optional string description;
    6: string datetime
}

struct DeleteReaderGroupByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct DeleteReaderGroupBulkRequestThrift {
    1: required string Authorization;
    2: required list<string> ids
}

struct PostReaderMemberRequestThrift {
    1: required string Authorization;
    2: required string barcode;
    3: string rfid = "";
    4: string levelId;
    5: list<string> groupIds;
    6: required string identity;
    7: string fullName;
    8: string gender;
    9: string dob;
    10: string email = "";
    11: string mobile = "";
    12: string address = "";
    13: string postcode = "";
    14: string profileImage = "";
    15: string createAt
}

struct PostReaderMemberBulkRequestThrift {
    1: required string Authorization;
    2: required list<ReaderMemberInsertionThrift> data
}


struct GetReaderMemberByIdRequestThrift {
    1: required string Authorization;
    2: required string id
}

struct GetReaderMemberListRequestThrift {
    1: required string Authorization;
    2: i32 limit = 100;
    3: i32 offset = 0;
    4: optional string id;
    5: optional string barcode;
    6: optional string rfid;
    7: optional string identity;
    8: optional string fullname;
    9: optional string gender;
    10: optional string email;
    11: optional string mobile;
    12: optional string address;
    13: optional string postcode;
    14: optional string dob;
    15: optional string levelId;
    16: optional string groupId;
    17: optional string createAt;
    18: optional bool isActive;
    19: optional bool isSuspend;
    20: optional string ordering;
    21: optional bool isOwing;
}

struct PatchReaderMemberBulkRequestThrift {
    1: required string Authorization;
    2: string action;
    3: optional list<string> ids;
    4: optional i32 days;
    5: optional list<string> groupIds
}

struct PatchReaderMemberByIdRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: string action;
    4: optional i32 days;
    5: optional string barcode;
    6: optional string rfid;
    7: optional string fullName;
    8: optional string gender;
    9: optional string email;
    10: optional string mobile;
    11: optional string address;
    12: optional string postcode;
    13: optional string dob;
    14: optional string levelId;
    15: optional list<string> groupIds;
    16: optional string profileImage;
    17: optional bool isActive;
    18: string datetime
}

struct PostReaderMemberBorrowItemsRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: list<string> bookBarcodes;
    4: string location
}

struct PostReaderMemberRenewItemsRequestThrift {
    1: required string Authorization;
    2: required string id;
    3: list<string> bookBarcodes;
    4: string location
}

struct PostReaderMemberReserveBooksRequestThrift {
    1: required string Authorization;
    2: required string Id;
    3: required list<string> bookBarcodes;
    4: string location
}

struct PostReaderMemberReserveBookRequestThrift {
    1: required string Authorization;
    2: required string Id;
    3: required string bookBarcode;
    4: string location
}
