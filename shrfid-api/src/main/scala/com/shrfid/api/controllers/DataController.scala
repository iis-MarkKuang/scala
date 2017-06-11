package com.shrfid.api.controllers

import javax.inject.{Inject, Singleton}

import com.shrfid.api._
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.shrfid.api.http.Elastic.data._
import com.shrfid.api.services.{BookService, ReaderService}
import com.twitter.finagle.http.Status

/**
  * Created by kuang on 2017/3/30.
  */
@Singleton
class DataController @Inject()(bookService: BookService,
                               readerService: ReaderService,
                               response: ResponseBuilder)
  extends Controller{


  get(Router.Data.bookBorrowRanking, "get_book_borrow_ranking") { request: GetBookBorrowRankingRequest =>
    for {
      r <- bookService.findBookBorrowRanking(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Data.categoryFlowStatistics, "get_category_flow_statistics") { request: GetCategoryFlowStatisticsRequest =>
    for {
      r <- bookService.findCategoryFlowStatistics(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Data.readerBorrowRanking, "get_reader_borrow_ranking") { request: GetReaderBorrowRankingRequest =>
    for {
      r <- readerService.findReaderBorrowRanking(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Data.bookFlowStatistics, "get_book_flow_statistics") { request: GetBookFlowStatisticsRequest =>
    for {
      r <- bookService.findBookFlowStatistics(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  get(Router.Data.stockStatistics, "get_stock_statistics") { request: GetStockStatisticsRequest =>
    for {
      r <- bookService.findStockStatistics(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }
  }

  // Added by kuangyuan 5/19/2017
  get(Router.Data.leastFreqBorrowStats, "get_least_frequent_borrowed_stats") { request: GetBookLeastBorrowStatsRequest =>
    for {
      r <- bookService.findBookLeastBorrowStats(request, Permission.BookItemManagement)
    } yield r match {
      case (Status.Unauthorized.code, InvalidToken) => response.unauthorized(UnauthorizedResponse).contentTypeJson()
      case (Status.Forbidden.code, NoPermission) => response.forbidden(NoPermissionResponse).contentTypeJson()
      case (_, a) => response.ok(a).contentTypeJson()
    }

  }
}
