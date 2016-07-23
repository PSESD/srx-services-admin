package org.psesd.srx.services.admin

import org.http4s.MediaType._
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Content-Type`
import org.http4s.server._

import scala.concurrent.ExecutionContext

/** SRX Admin Service.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  **/
object AdminService {

  final val RootUrl = "/admin"

  def service(implicit executionContext: ExecutionContext = ExecutionContext.global): HttpService = Router(
    "" -> rootService
  )

  def rootService(implicit executionContext: ExecutionContext) = HttpService {

    case req@GET -> Root =>
      Ok()

    case _ -> Root =>
      MethodNotAllowed()

    case GET -> Root / "ping" =>
      Ok("pong")

    case req@POST -> Root / "echo" =>
      Ok(req.body).putHeaders(`Content-Type`(`text/plain`))

    case req@GET -> Root / "echo" =>
      Ok("echo data")
  }

}
