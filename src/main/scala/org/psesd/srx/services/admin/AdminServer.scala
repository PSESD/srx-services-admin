package org.psesd.srx.services.admin

import org.http4s._
import org.http4s.dsl._
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
  private val messagesResource = CoreResource.SrxMessages.toString

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

    case req@GET -> Root / _ if services(req, CoreResource.Ping.toString) =>
      Ok(true.toString)

    case req@GET -> Root / _ if services(req, CoreResource.Info.toString) =>
      respondWithInfo(getDefaultSrxResponse(req))

    case req@GET -> Root / `messagesResource` / _ =>
      executeRequest(req, messagesResource, MessageService)

    case req@POST -> Root / _ if services(req, messagesResource) =>
      executeRequest(req, messagesResource, MessageService, SrxMessage.apply)

    case req@PUT -> Root / _ if services(req, messagesResource) =>
      MethodNotAllowed()

    case req@DELETE -> Root / `messagesResource` / _ =>
      MethodNotAllowed()

    case _ =>
      NotFound()

  }

}
