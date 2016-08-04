package org.psesd.srx.services.admin

import java.util.UUID

import org.apache.http.HttpStatus
import org.http4s.dsl._
import org.http4s.{Method, Request}
import org.psesd.srx.shared.core.SrxMessage
import org.psesd.srx.shared.core.config.Environment
import org.psesd.srx.shared.core.extensions.HttpTypeExtensions._
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.sif._
import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdminServerTests extends FunSuite {

  private final val ServerDuration = 5000
  val messageId = SifMessageId()
  val timestamp = SifTimestamp()
  val resource = "message"
  val method = "CREATE"
  val status = "success"
  val generatorId = "srx-services-admin test"
  val requestId = UUID.randomUUID().toString
  val zone = SifZone()
  val context = SifContext()
  val studentId = "123"
  val description = "test message"
  val testUri = "https://localhost/message;zoneId=DEFAULT;contextId=DEFAULT"
  val userAgent = "test userAgent"
  val sourceIp = "test sourceIp"
  val headers = "content-type: xml"
  val body = "test body"
  val testMessage = SrxMessage(
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
    Some(testUri),
    Some(userAgent),
    Some(sourceIp),
    Some(headers),
    Some(body)
  )
  private val pendingInterrupts = new ThreadLocal[List[Thread]] {
    override def initialValue = Nil
  }
  private lazy val tempServer = Future {
    delayedInterrupt(ServerDuration)
    intercept[InterruptedException] {
      startServer()
    }
  }

  test("service") {
    assert(AdminServer.srxService.service.name.equals(Build.name))
    assert(AdminServer.srxService.service.version.equals(Build.version + "." + Build.buildNumber))
    assert(AdminServer.srxService.buildComponents(0).name.equals("java"))
    assert(AdminServer.srxService.buildComponents(0).version.equals(Build.javaVersion))
    assert(AdminServer.srxService.buildComponents(1).name.equals("scala"))
    assert(AdminServer.srxService.buildComponents(1).version.equals(Build.scalaVersion))
    assert(AdminServer.srxService.buildComponents(2).name.equals("sbt"))
    assert(AdminServer.srxService.buildComponents(2).version.equals(Build.sbtVersion))
  }

  test("root") {
    val getRoot = Request(Method.GET, uri("/"))
    val task = AdminServer.service.run(getRoot)
    val response = task.run
    assert(response.status.code.equals(HttpStatus.SC_OK))
  }

  test("ping") {
    if(Environment.isLocal) {
      val getPing = Request(Method.GET, uri("/ping"))
      val task = AdminServer.service.run(getPing)
      val response = task.run
      val body = response.body.value
      assert(response.status.code.equals(HttpStatus.SC_OK))
      assert(body.equals(true.toString))
    }
  }

  test("info (localhost)") {
    if(Environment.isLocal) {
      val sifRequest = new SifRequest(TestValues.sifProvider, "info")
      val response = new SifConsumer().query(sifRequest)
      printlnResponse(response)
      val responseBody = response.body.getOrElse("")
      assert(response.statusCode.equals(HttpStatus.SC_OK))
      assert(response.contentType.get.equals(SifContentType.Xml))
      assert(responseBody.contains("<service>"))
    }
  }

  test("create message xml (localhost)") {
    if(Environment.isLocal) {
      val sifRequest = new SifRequest(TestValues.sifProvider, "message")
      sifRequest.generatorId = Some(generatorId)
      sifRequest.body = Some(testMessage.toXml.toXmlString)
      val response = new SifConsumer().create(sifRequest)
      printlnResponse(response)
      assert(response.statusCode.equals(HttpStatus.SC_CREATED))
    }
  }

  test("create message json (localhost)") {
    if(Environment.isLocal) {
      val sifRequest = new SifRequest(TestValues.sifProvider, "message")
      sifRequest.accept = Some(SifContentType.Json)
      sifRequest.contentType = Some(SifContentType.Json)
      sifRequest.generatorId = Some(generatorId)
      sifRequest.body = Some(testMessage.toXml.toJsonString)
      val response = new SifConsumer().create(sifRequest)
      printlnResponse(response)
      assert(response.statusCode.equals(HttpStatus.SC_CREATED))
    }
  }

  private def delayedInterrupt(delay: Long) {
    delayedInterrupt(Thread.currentThread, delay)
  }

  private def delayedInterrupt(target: Thread, delay: Long) {
    val t = new Thread {
      override def run() {
        Thread.sleep(delay)
        target.interrupt()
      }
    }
    pendingInterrupts.set(t :: pendingInterrupts.get)
    t.start()
  }

  private def startServer(): Unit = {
    if(Environment.isLocal) {
      AdminServer.main(Array[String]())
    }
  }

  private def printlnResponse(response: SifResponse): Unit = {
    for (header <- response.getHeaders) {
      println("%s=%s".format(header._1, header._2))
    }
    println(response.body.getOrElse(""))
  }

}
