package com.shrfid.api.persistence.slick.vendor

import javax.inject.{Inject, Singleton}

import com.shrfid.api.modules.SlickDatabaseModule.SlickDatabaseSource
import com.shrfid.api.persistence.slick.{BaseDalImpl, BaseEntity, BaseTable}
import slick.driver.MySQLDriver.api._
import slick.lifted.Tag
import slick.profile.SqlProfile.ColumnOption.Nullable

/**
  * Created by jiejin on 20/10/16.
  */
case class VendorMemberEntity(id: Int,
                              name: String,
                              description: Option[String]) extends BaseEntity

class VendorMemberTable(tag: Tag) extends BaseTable[VendorMemberEntity](tag, "vendor_member") {

  def name = column[String]("name", O.Length(80))

  def description = column[Option[String]]("description", O.Length(254), Nullable)

  override def * = (id, name, description) <>((VendorMemberEntity.apply _).tupled, VendorMemberEntity.unapply)

  def idx = index("vendor_member_name_uniq", name, unique = true)
}

@Singleton
class VendorMemberRepository @Inject()(db: SlickDatabaseSource) {
  lazy val vendorMembers = TableQuery[VendorMemberTable]

  val dal = new BaseDalImpl[VendorMemberTable, VendorMemberEntity](db)(vendorMembers) {

  }
}