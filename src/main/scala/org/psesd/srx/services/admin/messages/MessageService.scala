package org.psesd.srx.services.admin.messages

import java.util.UUID

import org.psesd.srx.shared.core._
import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.exceptions.{ArgumentInvalidException, ArgumentNullException}
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.sif._
import org.psesd.srx.shared.data._

import scala.collection.mutable.ArrayBuffer

/** SRX Message service.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  **/
object MessageService extends SrxResourceService {

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

  def delete(parameters: List[SifRequestParameter]): SrxResourceResult = {
    throw new NotImplementedError("SrxMessage DELETE not implemented.")
  }

  def create(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    if (resource == null) {
      throw new ArgumentNullException("resource parameter")
    }

    val message = resource.asInstanceOf[SrxMessage]

    val datasource = new Datasource(datasourceConfig)

    val result = datasource.create(
      "insert into srx_services_admin.message (" +
        "message_id, message_time, component, component_version, resource, method, status, " +
        "generator_id, request_id, zone_id, context_id, student_id, description, " +
        "uri, source_ip, user_agent, headers, body) values (" +
        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "RETURNING message_id;",
      "message_id",
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

    MessageResult(SifRequestAction.Create, result)
  }

  def query(parameters: List[SifRequestParameter]): SrxResourceResult = {
    var id: UUID = null
    var result: SrxResourceResult = MessageResult(SifRequestAction.Query, null)

    try {
      if (parameters != null && parameters.nonEmpty) {
        val idParameter = parameters.find(p => p.key == "id").orNull
        if (idParameter != null) {
          id = SifMessageId(idParameter.value).id
        }
      }
    } catch {
      case e: Exception =>
    }

    if(id != null) {
      try {
        val datasource = new Datasource(datasourceConfig)
        val queryResut = datasource.get("select * from srx_services_admin.message where message_id = ?", id)
        datasource.close()
        result = MessageResult(SifRequestAction.Query, queryResut)
        if(result.statusCode == 404) {
          result.exceptions += new Exception("Message not found.")
        }
      } catch {
        case e: Exception =>
          result.exceptions += e
          result.statusCode = 500
      }
    } else {
      result.exceptions += new ArgumentInvalidException("id parameter")
      result.statusCode = 400
    }

    result
  }

  def update(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    throw new NotImplementedError("SrxMessage UPDATE not implemented.")
  }

  def getMessagesFromResult(result: DatasourceResult): List[SrxMessage] = {
    val messages = ArrayBuffer[SrxMessage]()
    for(row <- result.rows) {
      val message = new SrxMessage(
        new SrxService(
          new SrxServiceComponent(
            row.getString("component").orNull,
            row.getString("component_version").orNull
          ),
          List[SrxServiceComponent]()
        ),
        new SifMessageId(row.getUuid("message_id").orNull),
        row.getTimestamp("message_time").orNull,
        row.getString("description").orNull
      )
      message.resource = row.getString("resource")
      message.method = row.getString("method")
      message.status = row.getString("status")
      message.generatorId = row.getString("generator_id")
      message.requestId = row.getString("request_id")
      val zoneId = row.getString("zone_id").orNull
      if (!zoneId.isNullOrEmpty) {
        message.zone = Some(SifZone(zoneId))
      }
      val contextId = row.getString("context_id").orNull
      if (!contextId.isNullOrEmpty) {
        message.context = Some(SifContext(contextId))
      }
      message.studentId = row.getString("student_id")
      message.uri = row.getString("uri")
      message.userAgent = row.getString("user_agent")
      message.sourceIp = row.getString("source_ip")
      message.headers = row.getString("headers")
      message.body = row.getString("body")
      messages += message
    }
    messages.toList
  }

}
