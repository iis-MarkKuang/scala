package com.shrfid.api.domains.reader

import com.shrfid.api.persistence.slick.book.BookItemEntity
import org.joda.time.DateTime

/**
  * Created by jiejin on 7/11/16.
  */
case class ReaderMemberBorrowRecord(id: Int,
                                    book: BookItemEntity,
                                    readerMemberId: Int,
                                    borrowAt: DateTime,
                                    dueAt: DateTime,
                                    returnAt: Option[DateTime])