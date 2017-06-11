package com.shrfid.api.http.Elastic.data

import javax.inject.Inject

import com.shrfid.api.{Time, defaultFlowDataLimit, defaultFlowDataMinDocCount, defaultInterval}
import com.twitter.finatra.request.{Header, QueryParam}
import com.twitter.finatra.validation.{MethodValidation, NotEmpty, ValidationResult}

/**
  * Created by kuang on 2017/3/31.
  */
case class GetBookBorrowRankingRequest @Inject() (@Header Authorization: String,
                                                  @QueryParam @NotEmpty start_time: String,
                                                  @QueryParam end_time: String = Time.now.toString)

case class GetBookFlowStatisticsRequest @Inject() (@Header Authorization : String,
                                                   @QueryParam @NotEmpty start_time : String,
                                                   @QueryParam end_time : String = Time.now.toString,

                                                   //added by kuang 4/14/2017
                                                   @QueryParam interval : String = defaultInterval,
                                                   @QueryParam limit: String = defaultFlowDataLimit,
                                                   @QueryParam minCount: String = defaultFlowDataMinDocCount
                                                  ) {

  //added by kuang 4/14/2017
  @MethodValidation
  def validateAction = {
    ValidationResult.validate(
      minCount.toInt >= 0 &&
        limit.toInt > 0 &&
        Seq("day", "week", "month", "quarter", "year").contains(interval.toLowerCase),
      "record count have to be greater than zero, \r\n" +
        "and interval have to be in Set {DAY, WEEK, MONTH, QUARTER, YEAR} (case ignored) \r\n" +
        "and minimum doc count have to be non-negative"
    )
  }
}

case class GetCategoryFlowStatisticsRequest @Inject() (@Header Authorization: String,
                                                       @QueryParam @NotEmpty start_time: String,
                                                       @QueryParam end_time: String = Time.now.toString,

                                                       //added by kuang 4/14/2017
                                                       @QueryParam interval : String = defaultInterval,
                                                       @QueryParam limit: String = defaultFlowDataLimit,
                                                       @QueryParam minCount: String = defaultFlowDataMinDocCount
                                                      )
{
  //added by kuang 4/14/2017
  @MethodValidation
  def validateAction = {
    ValidationResult.validate(
      minCount.toInt >= 0 &&
        limit.toInt > 0 &&
        Seq("day", "week", "month", "quarter", "year").contains(interval.toLowerCase),
      "record count have to be greater than zero, \r\n" +
        "and interval have to be in Set {DAY, WEEK, MONTH, QUARTER, YEAR} (case ignored) \r\n" +
        "and minimum doc count have to be non-negative"
    )
  }
}

case class GetReaderBorrowRankingRequest @Inject() (@Header Authorization: String,
                                                    @QueryParam @NotEmpty start_time: String,
                                                    @QueryParam end_time: String = Time.now.toString)

case class GetBookLeastBorrowStatsRequest(@Header Authorization: String,
                                          @QueryParam @NotEmpty start_time: String,
                                          @QueryParam milestone: String = Time.now.toString)