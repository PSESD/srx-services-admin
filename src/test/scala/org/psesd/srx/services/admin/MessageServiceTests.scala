package org.psesd.srx.services.admin

import org.psesd.srx.services.admin.messages.MessageService
import org.psesd.srx.shared.core.SrxMessage
import org.psesd.srx.shared.core.sif.{SifContext, SifMessageId, SifTimestamp, SifZone}
import org.scalatest.FunSuite

class MessageServiceTests extends FunSuite {

  test("create") {
    val messageId = SifMessageId()
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

    val result = MessageService.createMessage(message)
    assert(result.success)
    assert(result.exceptions.isEmpty)
    assert(result.id.get.equals(messageId.toString))
  }

}
