package org.psesd.srx.services.admin.messages

import java.util.UUID

import org.psesd.srx.services.admin.AdminServer
import org.psesd.srx.shared.core._
import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.exceptions.{ArgumentInvalidException, ArgumentNullException}
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.logging.{LogLevel, Logger}
import org.psesd.srx.shared.core.sif._
import org.psesd.srx.shared.data._

import scala.collection.mutable.ArrayBuffer

/** SRX Message service.
  *
  * @version 1.0
  * @since 1.0
  * @author Stephen Pugmire (iTrellis, LLC)
  * */
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
    SrxResourceErrorResult(SifHttpStatusCode.MethodNotAllowed, new UnsupportedOperationException(CoreResource.SrxMessages.toString + " DELETE method not allowed."))
  }

  def create(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    try {
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

      if (result.success) {
        MessageResult(SifRequestAction.Create, SifRequestAction.getSuccessStatusCode(SifRequestAction.Create), result)
      } else {
        throw result.exceptions.head
      }
    } catch {
      case e: Exception =>
        // Logger.log(LogLevel.Error, "Failed to %s %s.".format(SifRequestAction.Create.toString, CoreResource.SrxMessages.toString), e.getMessage, AdminServer.srxService)
        SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, new Exception(""))
    }
  }

  def query(parameters: List[SifRequestParameter]): SrxResourceResult = {
    val id = getMessageIdFromRequestParameters(parameters)
    if (id == null) {
      SrxResourceErrorResult(SifHttpStatusCode.BadRequest, new ArgumentInvalidException("id parameter"))
    } else {
      try {
        val datasource = new Datasource(datasourceConfig)
        val result = datasource.get("select * from srx_services_admin.message where message_id = ?", id)
        datasource.close()
        if (result.success) {
          if (result.rows.isEmpty) {
            SrxResourceErrorResult(SifHttpStatusCode.NotFound, new Exception("%s not found.".format(CoreResource.SrxMessages.toString)))
          } else {
            MessageResult(SifRequestAction.Query, SifHttpStatusCode.Ok, result)
          }
        } else {
          throw result.exceptions.head
        }
      } catch {
        case e: Exception =>
          // Logger.log(LogLevel.Error, "Failed to %s %s.".format(SifRequestAction.Create.toString, CoreResource.SrxMessages.toString), e.getMessage, AdminServer.srxService)
          SrxResourceErrorResult(SifHttpStatusCode.InternalServerError, new Exception(""))
      }
    }
  }

  def update(resource: SrxResource, parameters: List[SifRequestParameter]): SrxResourceResult = {
    SrxResourceErrorResult(SifHttpStatusCode.MethodNotAllowed, new UnsupportedOperationException(CoreResource.SrxMessages.toString + " UPDATE method not allowed."))
  }

  def getMessagesFromResult(result: DatasourceResult): List[SrxMessage] = {
    val messages = ArrayBuffer[SrxMessage]()
    for (row <- result.rows) {
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

  private def getMessageIdFromRequestParameters(parameters: List[SifRequestParameter]): UUID = {
    var id: UUID = null
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
    id
  }

}
