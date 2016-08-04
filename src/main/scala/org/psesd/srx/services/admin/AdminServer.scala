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
  private final val ServerSessionTokenKey = "SERVER_SESSION_TOKEN"
  private final val ServerSharedSecretKey = "SERVER_SHARED_SECRET"

  val sifProvider: SifProvider = new SifProvider(
    SifProviderUrl(Environment.getProperty(ServerUrlKey)),
    SifProviderSessionToken(Environment.getProperty(ServerSessionTokenKey)),
    SifProviderSharedSecret(Environment.getProperty(ServerSharedSecretKey)),
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

  override def serviceRouter(implicit executionContext: ExecutionContext) = HttpService {

    case req@GET -> Root =>
      Ok()

    case _ -> Root =>
      NotImplemented()

    case GET -> Root / "ping" =>
      Ok(true.toString)

    case req@GET -> Root / _ if req.pathInfo.startsWith("/info") =>
      respondWithInfo(getDefaultSrxResponse(req)).toHttpResponse

    case req@GET -> Root / _ if req.pathInfo.startsWith("/message") =>
      NotImplemented()

    case req@POST -> Root / _ if req.pathInfo.startsWith("/message") =>
      respondWithCreateMessage(getDefaultSrxResponse(req)).toHttpResponse

    case req@GET -> Root / _ if req.pathInfo.startsWith("/messages") =>
      NotImplemented()

    case _ =>
      NotFound()

  }

  private def respondWithCreateMessage(srxResponse: SrxResponse): SrxResponse = {
    if (!srxResponse.hasError) {
      try {

        val result = MessageService.createMessage(SrxMessage(srxResponse.srxRequest.getBodyXml.orNull))
        if(result.success) {
          srxResponse.sifResponse.statusCode = Created.code
        } else {
          val errorMessage = {
            if(result.exceptions.nonEmpty) {
              result.exceptions.head.getMessage
            } else {
              ""
            }
          }
          srxResponse.setError(new SifError(
            InternalServerError.code,
            "Message",
            "Failed to create message.",
            errorMessage
          ))
        }
        srxResponse.sifResponse.bodyXml = Option(
          <createResponse>
            <creates>
              <create id={result.messageId.toString} advisoryId="1" statusCode={Created.code.toString}/>
            </creates>
          </createResponse>
        )
      } catch {
        case e: Exception =>
          srxResponse.setError(new SifError(
            InternalServerError.code,
            "Message",
            "Failed to create message.",
            e.getMessage
          ))
      }
    }
    srxResponse
  }
}
