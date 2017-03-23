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
      val writtingMethod = "/api/book/writing_method"
      val language = "/api/book/languages"
      val preGen = "/api/book/pregen"
      val items = "/api/book/items"
      val itemById = "/api/book/items/:id"
      val _return = "/api/book/return"
      val inactivate = "/api/book/inactivate"
      val transfer = "/api/book/transfer"
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


    object ReaderLevel {
      val list = "/api/reader/levels"
      val byId = "/api/reader/levels/:id"
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

    object BookCataloging {
      val catalogingByISBN = "/api/cataloging/:isbn"
    }




    //leixx,2017-2-16
    object SayHello{
      val list = "/api/vendor/sayhello"
    }
    //leixx,2017-2-17
    object LeixxInsertReader2Db{
      val list="/api/reader/insertreader"
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
    val ReaderMemeberManagement = 10
    val BookBranchManagement = 1
    val BookStackManagement = 1
    val BookItemPreGen = 4
    val VendorManagement = 17
    val BookTransaction = 30
    val VendorPlaceOrder = 17
    val BookItemManagement = 20
  }


}
