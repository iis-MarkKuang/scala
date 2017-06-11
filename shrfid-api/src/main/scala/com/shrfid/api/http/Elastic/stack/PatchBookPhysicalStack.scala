package com.shrfid.api.http.Elastic.stack

import com.twitter.finatra.request.{Header, QueryParam}
import com.twitter.finatra.validation.NotEmpty
import com.shrfid.api._
/**
  * Created by leixx on 2017/4/6.
  */
case class PatchBookPhysicalStackByBarCodeRequest(@Header Authorization: String,
                                                  @NotEmpty barcode: String,
                                                  shelf: String
                                            )

case class PatchBookPhysicalShelfStatusViaFTPRequest(@Header Authorization: String,
                                                     @QueryParam directory: String = testFolder,
                                                     @QueryParam username: String = "",
                                                     @QueryParam password: String = "")

case class GetBookBusinessAndPhysicalStateByBarCodeRequest(@Header Authorization: String,
                                                           @QueryParam bar_code: String)

case class GetBookPhysicalShelfStatusViaFTPRequest(@Header Authorization: String,
                                                    @QueryParam directory: String = testFolder,
                                                    @QueryParam username: String = "",
                                                    @QueryParam password: String = "")



