package org.psesd.srx.services.admin.messages

import org.psesd.srx.shared.core.SrxMessage
import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.exceptions.ArgumentNullException
import org.psesd.srx.shared.data._

/** SRX Message service.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  **/
object MessageService {

  private final val DatasourceClassNameKey = "DATASOURCE_CLASS_NAME"
  private final val DatasourceMaxConnectionsKey = "DATASOURCE_MAX_CONNECTIONS"
  private final val DatasourceTimeoutKey = "DATASOURCE_TIMEOUT"
  private final val DatasourceUrlKey = "DATASOURCE_URL"

  private lazy val datasourceConfig = new DatasourceConfig(
    Environment.getProperty(DatasourceUrlKey),
    Environment.getProperty(DatasourceClassNameKey),
    Environment.getProperty(DatasourceMaxConnectionsKey).toInt,
    Environment.getProperty(DatasourceTimeoutKey).toLong
  )

  def createMessage(message: SrxMessage): MessageResult = {
    if (message == null) {
      throw new ArgumentNullException("message parameter")
    }

    val datasource = new Datasource(datasourceConfig)

    val result = datasource.execute(
      "insert into srx_services_admin.message (" +
        "message_id, message_time, component, component_version, resource, method, status, " +
        "generator_id, request_id, zone_id, context_id, student_id, description, " +
        "uri, source_ip, user_agent, headers, body) values (" +
        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
      message.messageId.id,
      message.timestamp,
      message.srxService.service.name,
      message.srxService.service.version,
      message.resource.getOrElse(""),
      message.method.getOrElse(""),
      message.status.getOrElse(""),
      message.getGeneratorId,
      message.getRequestId,
      message.getZoneId,
      message.getContextId,
      message.studentId.getOrElse(""),
      message.description,
      message.getUri,
      message.getSourceIp,
      message.getUserAgent,
      message.getHeaders,
      message.getBody
    )

    datasource.close()

    MessageResult(message.messageId, result)
  }

}
