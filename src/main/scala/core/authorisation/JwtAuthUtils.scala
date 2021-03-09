package core.authorisation

import java.time.{Instant, LocalTime}

import core.authorisation.MembershipPrivileges.membershipPrivilegesMap
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtSprayJson}

import scala.util.{Failure, Success}
import spray.json._

case class Claims(id: Long, membership: Int)



object JwtAuthUtils extends JwtAuthJsonProtocol  {


  val algorithm = JwtAlgorithm.HS256
  val secretKey = "secret"   // TODO: Retrieve this from somewhere secure

  def generateToken(id: Long, membership: Int, expirationTimeSeconds: Int = 300): String = {
    val claims = JwtClaim (
      expiration = Some(Instant.now.plusSeconds(expirationTimeSeconds).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond),
      issuer = Some("wcc.com"),
      content =
        s"""
          |{
          |"id": ${id + ","}
          |"membership": ${membership}
          |}
          |
          |""".stripMargin
    )
    JwtSprayJson.encode(claims, secretKey, algorithm)
  }

  def isTokenExpired(token: String): Boolean = JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
    case Success(claims) =>
      val a = claims.expiration.getOrElse(0.asInstanceOf[Long])
      a < Instant.now.getEpochSecond
    case Failure(_) =>
      println("Failure")
      true
  }

  def isTokenValid(token: String):Boolean = JwtSprayJson.isValid(token , secretKey, Seq(algorithm))

  def getTokenClaims(token: String): Claims = {
    JwtSprayJson.decode(token, secretKey, Seq(algorithm)) match {
      case Success(claims) => claims.content.parseJson.convertTo[Claims]
    }
  }

  def isTokenApproved(tokenOption: Option[String]): Boolean = tokenOption match {
    case None => false
    case Some(token) if isTokenExpired(token) || !isTokenValid(token) =>
      println(!isTokenValid(token))
      println(isTokenExpired(token))
      false
    case _ => true
  }

  def getMinutesPermittedPerDay(membershipType: Int): Int = membershipPrivilegesMap(membershipType)


  val a = LocalTime.parse("17:00")
  val b = LocalTime.parse("18:00")

  val c = LocalTime.parse("17:30")
  val d = LocalTime.parse("18:30")

  println(c.isAfter(a) && c.isBefore(b))

  // println(membershipPrivalleges(1))

//  val token = generateToken(1, 1, 1000000000)
//  println(token)
//
//  println(isTokenApproved(Option(token)))

}