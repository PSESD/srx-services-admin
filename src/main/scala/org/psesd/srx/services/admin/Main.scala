package org.psesd.srx.services.admin

import org.http4s.server.ServerApp
import org.http4s.server.blaze._
import org.psesd.srx.shared.core.config.Environment

/** Application entry point.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  **/
object Main extends ServerApp {

  private final val ServerPortKey = "SERVER_PORT"

  def server(args: List[String]) = BlazeBuilder
    .bindHttp(Environment.getProperty(ServerPortKey).toInt)
    .mountService(AdminService.service, AdminService.RootUrl)
    .start

}
