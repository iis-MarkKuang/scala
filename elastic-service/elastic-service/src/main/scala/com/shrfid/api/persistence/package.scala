package com.shrfid.api

import slick.driver.MySQLDriver.api._
import slick.lifted.Rep

/**
  * Created by jiejin on 13/09/2016.
  */


package object persistence {
  val All = -1

  val Zero = 0

  def logic(c: Rep[Boolean], d: Rep[Boolean], logicOp: String) = logicOp match {
    case "and" => c && d
    case "or" => c || d
  }

  def logicOption(a: Rep[Option[Boolean]], b: Rep[Option[Boolean]], logicOp: String) = logicOp match {
    case "and" => a && b
    case "or" => a || b
  }
}
