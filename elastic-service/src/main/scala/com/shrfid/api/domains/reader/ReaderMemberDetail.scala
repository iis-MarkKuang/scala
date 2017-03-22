package com.shrfid.api.domains.reader

import java.sql.Date

import org.joda.time.DateTime

/**
  * Created by jiejin on 22/9/16.
  */

case class ReaderMemberDetail(id: Int,
                              barcode: String,
                              rfid: Option[String],
                              level: ReaderLevel,
                              groups: Seq[ReaderGroup],
                              identity: String,
                              password: String,
                              fullName: String,
                              gender: String,
                              dob: Date,
                              email: Option[String],
                              mobile: Option[String],
                              address: Option[String],
                              postcode: Option[String],
                              profileImage: Option[String],
                              restoreAt: Option[Date],
                              createAt: DateTime,
                              updateAt: DateTime,
                              lastLogin: DateTime,
                              isActive: Boolean)

