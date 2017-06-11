package com.shrfid.api

/**
  * Created by jiejin on 19/09/2016.
  */
package object controllers {
  def token(authorization: String) = authorization.substring(7)

  object Router {
    val docs = "/api/docs"


    val authToken = "/api/auth/token"
    val authPermissions = "/api/auth/permissions"
    val authGroups = "/api/auth/groups"
    val authGroupById = "/api/auth/groups/:id"
    val authInfo = "/api/auth/info"
    val authUsers = "/api/auth/users"
    val authUserById = "/api/auth/users/:id"
    val authUserForceUpdatePassword = "/api/auth/users/:id/password/force"
    val authUserUpdatePassword = "/api/auth/password"


    object Book {
      val importReference = "/api/book/elastic/:filename"
      val writingMethod = "/api/book/writing_method"
      val language = "/api/book/languages"
      val preGen = "/api/book/pregen"
      val items = "/api/book/items"
      val itemById = "/api/book/items/:id"
      val _return = "/api/book/return"
      val inactivate = "/api/book/inactivate"
      val transfer = "/api/book/transfer"
      val checkBookByBarCode="/api/book/checkbook"
      val initBookItemShelfByBarCode="/api/book/initbookshelf"
      val getBookItemBusinessAndPhysicalState="/api/book/get_book_business_shelf_state"

      // Added by kuangyuan 4/17/2017
      val itemsByBarcode = "/api/book/items/search"

      // Added by kuangyuan 4/19/2017
      val bookItemsShelfStatusByDir="/api/book/shelf_status"

      // Added by kuangyuan 4/20/2017
      val getCheckProgress = "/api/book/check/progress"

      // Added by kuangyuan 5/7/2017
      val periodicalBind = "/api/book/periodicals/bind"

      // Added by kuangyuan 5/12/2017
      val fineByBarcode = "/api/book/fine"

      // Added by kuangyuan 5/13/2017
      val deactivate = "/api/book/items/deactivate"
      val alterStack = "/api/book/items/alterstack"

      // Added by kuangyuan 5/15/2017
      val reactivate = "/api/book/items/reactivate"
    }

    object Branch {
      val list = "/api/book/branches"
      val byId = "/api/book/branches/:id"
    }

    object Stack {
      val list = "/api/book/bookstacks"
      val byId = "/api/book/bookstacks/:id"
    }

    object Reference {
      val list = "/api/book/reference"
      val byId = "/api/book/reference/:id"
    }
    object Reservation {
      val list = "/api/book/reservation"
      val byId = "/api/book/reservation/:id"
    }

    object ReaderLevel {
      val list = "/api/reader/levels"
      val byId = "/api/reader/levels/:id"
    }

    object ReaderLevelNew {
      val list = "/api/reader/levels_new"
      val byId = "/api/reader/levels_new/:id"
    }

    object ReaderGroup {
      val list = "/api/reader/groups"
      val byId = "/api/reader/groups/:id"
    }

    object ReaderMember {
      val list = "/api/reader/members"
      val byId = "/api/reader/members/:id"

      val bulk = "/api/reader/bulk/members"
      val currentHoldings = "/api/reader/members/:id/holdings"

      // added by kuangyuan 4/12/2017
      val delayFine = "/api/reader/members/:id/fine"
      val lostFine = "/api/reader/members/:id/lostfine"
      val deductCredit = "/api/reader/members/:id/credit/deduct"

      // added by kuangyuan 5/4/2017
      val findByBarcode = "/api/reader/members/find_by_barcode"

      // added by kuangyuan 5/9/2018
      val delayedDetail = "/api/reader/members/:id/delayed_detail"
    }


    object VendorMember {
      val list = "/api/vendor/members"
      val byId = "/api/vendor/members/:id"
    }

    object VendorOrder {
      val list = "/api/vendor/orders"
      val byId = "/api/vendor/orders/:id"
      val bulk = "/api/vendor/bulk/orders"
    }

    // added by yuan kuang 4/12/2017
    object Data {
      val categoryFlowStatistics = "/api/data/flow_category"
      val bookFlowStatistics = "/api/data/flow_book"
      val bookBorrowRanking = "/api/data/ranking_book"
      val readerBorrowRanking = "/api/data/ranking_reader"
      val stockStatistics = "/api/data/stock"
      val leastFreqBorrowStats = "/api/data/least_borrow_stats"
    }

    // added by kuang yuan 5/9/2017
    object Solicited {
      val list = "/api/solicited"
      val byId = "/api/solicited/:id"

      val resolicit = "/api/solicited/:id/resolicit"
      val lateSolicited = "/api/solicited/late"

      val search = "/api/solicited/search"
    }

    // added by kuang yuan 5/11/2017
    object PeriodicalBatch {
      val list = "/api/period_batch"
      val byId = "/api/period_batch/:id"
    }

  }

  object Permission {
    type PermissionCode = Int
    val ForceChangePassword = 2
    val AuthPermissionManagement = 2
    val AuthUserManagement = 2
    val AuthGroupManagement = 2
    val ChangePassword = 5
    val ReaderLevelManagement = 3
    val ReaderGroupManagement = 10
    val ReaderMemberManagement = 10
    val BookBranchManagement = 1
    val BookStackManagement = 1
    val BookItemPreGen = 4
    val VendorManagement = 17
    val BookTransaction = 30
    val VendorPlaceOrder = 17
    val BookItemManagement = 20

  }


}
