package com.shrfid.pbkdf2.legacy

/**
  * Created by jiejin on 4/05/2016.
  */

import java.nio.charset.StandardCharsets.UTF_8
import java.security.SecureRandom

import com.shrfid.pbkdf2._

/**
  * This is the legacy API.
  *
  * @param iterations the number of encryption iterations. Default to 20000
  * @param dkLength   derived-key length, default to 32
  * @param cryptoAlgo HMAC+SHA256 is the default as HMAC+SHA1 is now considered weak
  */
case class SecureHash(iterations: Int = 20000, dkLength: Int = 32, cryptoAlgo: String = "HmacSHA512") {

  /**
    * Creates a hashed password using [[PBKDF2]]
    *
    * this function output a string in the following format:
    *
    * salt:key
    *
    * - salt : hex encoded salt
    *
    * - key : hex encoded derived key
    *
    * Example :
    *
    * a9c654289407047fd197516196e14b97bdabfa4bc934d0e9:f2f458b2502ca7595a4c964b14f146bd9c49174fa41b625227602bf4aaffbf5e
    *
    * @param password the password to hash
    */
  def createHash(password: String): String = {
    val random = new SecureRandom
    val salt = new Array[Byte](24) //192 bits
    random.nextBytes(salt)

    val hash = PBKDF2(password.getBytes(UTF_8), salt, iterations, dkLength, cryptoAlgo)

    raw"${toHex(salt)}:${toHex(hash)}"
  }

  /**
    * Validate a password against a password hash
    *
    * this function will first try to validate with the *new* format as generated
    * by [[com.shrfid.pbkdf2.SecureHash.createHash]]. if it fails, it will fall back to the *old* format,
    * making this function very useful when transitioning form the *old* to the *new* format.
    *
    * @param password       the password to validate
    * @param hashedPassword the password hash.
    * @return true is the password is valid
    */
  def validatePassword(password: String, hashedPassword: String): Boolean =
  //Try new format first and then fall back to legacy
    if (com.shrfid.pbkdf2.SecureHash.validatePassword(password, hashedPassword))
      true
    else
      legacyValidatePassword(password, hashedPassword)

  private[this] def legacyValidatePassword(password: String, hashedPassword: String): Boolean = {
    val params = hashedPassword.split(":")
    assert(params.size == 2)
    val salt = fromHex(params(0))
    val hash = PBKDF2(password.getBytes(UTF_8), salt, iterations, dkLength, cryptoAlgo)
    fromHex(params(1)) == hash
  }
}

