package org.psesd.srx.services.admin

import org.http4s._
import org.http4s.dsl._
import org.psesd.srx.services.admin.config.ZoneConfig
import org.psesd.srx.services.admin.messages.MessageService
import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.sif._
import org.psesd.srx.shared.core._

import scala.concurrent.ExecutionContext

/** SRX Admin Server.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  * */
object AdminServer extends SrxServer {

  private final val ServerUrlKey = "SERVER_URL"
  private val messagesResource = SrxResourceType.SrxMessages.toString
  private val zoneConfigResource = SrxResourceType.SrxZoneConfig.toString

  val sifProvider: SifProvider = new SifProvider(
    SifProviderUrl(Environment.getProperty(ServerUrlKey)),
    SifProviderSessionToken(Environment.getProperty(Environment.SrxSessionTokenKey)),
    SifProviderSharedSecret(Environment.getProperty(Environment.SrxSharedSecretKey)),
    SifAuthenticationMethod.SifHmacSha256
  )

  val srxService: SrxService = new SrxService(
    new SrxServiceComponent(Build.name, Build.version + "." + Build.buildNumber),
    List[SrxServiceComponent](
      new SrxServiceComponent("java", Build.javaVersion),
      new SrxServiceComponent("scala", Build.scalaVersion),
      new SrxServiceComponent("sbt", Build.sbtVersion)
    )
  )

  override def createServerEventMessage(message: SrxMessage): Unit = {
    MessageService.create(message, List[SifRequestParameter]())
  }

  override def serviceRouter(implicit executionContext: ExecutionContext) = HttpService {

    case req@GET -> Root =>
      Ok()

    case _ -> Root =>
      NotImplemented()

    case req@GET -> Root / _ if services(req, SrxResourceType.Ping.toString) =>
      Ok(true.toString)

    case req@GET -> Root / _ if services(req, SrxResourceType.Info.toString) =>
      respondWithInfo(getDefaultSrxResponse(req))


    /* MESSAGES */

    case req@GET -> Root / _ if services(req, messagesResource) =>
      MethodNotAllowed()

    case req@GET -> Root / `messagesResource` / _ =>
      executeRequest(req, None, messagesResource, MessageService)

    case req@POST -> Root / _ if services(req, messagesResource) =>
      executeRequest(req, None, messagesResource, MessageService, SrxMessage.apply)

    case req@PUT -> Root / _ if services(req, messagesResource) =>
      MethodNotAllowed()

    case req@PUT -> Root / `messagesResource` / _ =>
      MethodNotAllowed()

    case req@DELETE -> Root / _ if services(req, messagesResource) =>
      MethodNotAllowed()

    case req@DELETE -> Root / `messagesResource` / _ =>
      MethodNotAllowed()


    /* ZONE CONFIG */

    case req@GET -> Root / _ if services(req, zoneConfigResource) =>
      MethodNotAllowed()

    case req@GET -> Root / `zoneConfigResource` / _ =>
      executeRequest(req, None, zoneConfigResource, ZoneConfig)

    case req@POST -> Root / _ if services(req, zoneConfigResource) =>
      MethodNotAllowed()

    case req@POST -> Root / `zoneConfigResource` / _ =>
      MethodNotAllowed()

    case req@PUT -> Root / _ if services(req, zoneConfigResource) =>
      MethodNotAllowed()

    case req@PUT -> Root / `zoneConfigResource` / _ =>
      MethodNotAllowed()

    case req@DELETE -> Root / _ if services(req, zoneConfigResource) =>
      MethodNotAllowed()

    case req@DELETE -> Root / `zoneConfigResource` / _ =>
      MethodNotAllowed()


    case _ =>
      NotFound()

  }

}
