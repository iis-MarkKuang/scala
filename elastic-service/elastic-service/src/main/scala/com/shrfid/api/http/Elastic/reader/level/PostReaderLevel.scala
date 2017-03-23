package com.shrfid.api.http.Elastic.reader.level

import com.shrfid.api.domains.reader.{BorrowRule => DomainBorrowRule, PenaltyRule => DomainPenaltyRule}
import com.twitter.finatra.request.Header

/**
  * Created by jiejin on 27/9/16.
  */

/*case class PutReaderLevelRequest(@Header Authorization: String,
                                 name: String,
                                 deposit: Double,
                                 generalBookRule: BorrowRule,
                                 journalRule: BorrowRule,
                                 ancientBookRule: BorrowRule,
                                 otherMediaRule: BorrowRule,
                                 penaltyRule: PenaltyRule,
                                 canRenew: Boolean,
                                 canBook: Boolean)*/


case class PostReaderLevelRequest(@Header Authorization: String,
                                  name: String,
                                  deposit: Double,
                                  borrowRule: DomainBorrowRule,
                                  penaltyRule: DomainPenaltyRule,
                                  description: String = "")