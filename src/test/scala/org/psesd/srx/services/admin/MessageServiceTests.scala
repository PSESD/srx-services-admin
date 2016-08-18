package org.psesd.srx.services.admin

import org.psesd.srx.services.admin.messages.MessageService
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.SrxMessage
import org.psesd.srx.shared.core.sif._
import org.scalatest.FunSuite

class MessageServiceTests extends FunSuite {

  val messageId = SifMessageId()

  test("create") {
    val timestamp = SifTimestamp()
    val resource = "xSre"
    val method = "query"
    val status = "status"
    val generatorId = "generatorId"
    val requestId = "requestId"
    val zone = SifZone("testZone")
    val context = SifContext("testContext")
    val studentId = "studentId"
    val description = "description"
    val uri = "http://localhost/test"
    val userAgent = "userAgent"
    val sourceIp = "sourceIp"
    val headers = "content-type: xml"
    val body = "body"
    val message = SrxMessage(
      AdminServer.srxService,
      messageId,
      timestamp,
      Some(resource),
      Some(method),
      Some(status),
      Some(generatorId),
      Some(requestId),
      Some(zone),
      Some(context),
      Some(studentId),
      description,
      Some(uri),
      Some(userAgent),
      Some(sourceIp),
      Some(headers),
      Some(body)
    )

    val result = MessageService.create(message, List[SifRequestParameter]())
    assert(result.success)
    assert(result.exceptions.isEmpty)
    assert(result.toXml.get.toXmlString.contains(messageId.toString))
  }

  test("query bad request") {
    val result = MessageService.query(List[SifRequestParameter](SifRequestParameter("id", "123")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.BadRequest)
    assert(result.toXml.isEmpty)
  }

  test("query not found") {
    val result = MessageService.query(List[SifRequestParameter](SifRequestParameter("id", "13345ffe-ffd8-4c72-a699-a84b08f060e8")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.NotFound)
    assert(result.toXml.isEmpty)
  }

  test("query by id") {
    val result = MessageService.query(List[SifRequestParameter](SifRequestParameter("id", messageId.toString)))
    assert(result.success)
    assert(result.statusCode == SifHttpStatusCode.Ok)
    assert(result.toXml.nonEmpty)
  }

}
