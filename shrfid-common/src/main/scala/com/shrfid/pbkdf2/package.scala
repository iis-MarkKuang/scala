package com.shrfid

/**
  * Created by jiejin on 4/05/2016.
  */
package object pbkdf2 {
  def toHex(buff: Array[Byte]): String = {
    val bi = new java.math.BigInteger(1, buff)
    val hex = bi.toString(16)
    val paddingLength = (buff.length * 2) - hex.size
    if (paddingLength > 0)
      ("0" * paddingLength) + hex
    else
      hex
  }

  def fromHex(hex: String): Array[Byte] = {
    val binary = new Array[Byte](hex.length / 2)
    for (i <- 0 until binary.length)
      binary.update(i, Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16).asInstanceOf[Byte])
    binary
  }
}
