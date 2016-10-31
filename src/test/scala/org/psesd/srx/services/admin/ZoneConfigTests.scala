package org.psesd.srx.services.admin

import org.psesd.srx.services.admin.config.ZoneConfig
import org.psesd.srx.shared.core.extensions.TypeExtensions._
import org.psesd.srx.shared.core.sif.{SifHttpStatusCode, SifRequestParameter}
import org.scalatest.FunSuite

class ZoneConfigTests extends FunSuite {

  test("constructor") {
    val id = "test"
    val zoneConfigXml = <zone name={id}/>
    val zoneConfig = new ZoneConfig(id, zoneConfigXml.toXmlString)
    assert(zoneConfig.id.equals(id))
    assert(zoneConfig.zoneConfig.contains("<zone"))
  }

  test("factory") {
    val id = "test"
    val zoneConfigXml = <zone name={id}/>
    val zoneConfig = ZoneConfig(zoneConfigXml.toXmlString)
    assert(zoneConfig.id.equals(id))
    assert(zoneConfig.zoneConfig.contains("<zone"))
  }

  test("node") {
    val id = "test"
    val zoneConfigXml = <zone name={id}/>
    val zoneConfig = ZoneConfig(zoneConfigXml, None)
    assert(zoneConfig.id.equals(id))
    assert(zoneConfig.zoneConfig.contains("<zone"))
  }

  test("create not implemented") {
    val result = ZoneConfig.create(TestValues.zoneConfig, List[SifRequestParameter](SifRequestParameter("id", "test")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.InternalServerError)
    assert(result.exceptions.head.getMessage == "ZoneConfig CREATE method not implemented.")
  }

  test("update not implemented") {
    val result = ZoneConfig.update(TestValues.zoneConfig, List[SifRequestParameter](SifRequestParameter("id", "test")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.InternalServerError)
    assert(result.exceptions.head.getMessage == "ZoneConfig UPDATE method not implemented.")
  }

  test("query no id") {
    val result = ZoneConfig.query(List[SifRequestParameter]())
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.BadRequest)
    assert(result.exceptions.head.getMessage == "The id parameter is invalid.")
    assert(result.toXml.isEmpty)
  }

  test("query id = -1") {
    val result = ZoneConfig.query(List[SifRequestParameter](SifRequestParameter("id", "-1")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.BadRequest)
    assert(result.exceptions.head.getMessage == "The id parameter is invalid.")
    assert(result.toXml.isEmpty)
  }

  test("query not found") {
    val result = ZoneConfig.query(List[SifRequestParameter](SifRequestParameter("id", "-99")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.NotFound)
    assert(result.exceptions.head.getMessage == "The requested ZoneConfig resource was not found.")
    assert(result.toXml.isEmpty)
  }

  test("query valid") {
    val result = ZoneConfig.query(List[SifRequestParameter](SifRequestParameter("id", "test")))
    assert(result.success)
    assert(result.statusCode == SifHttpStatusCode.Ok)
    val zoneConfigXml = result.toXml.get
    assert((zoneConfigXml \ "@name").text.equals("test"))
  }

  test("delete not implemented") {
    val result = ZoneConfig.delete(List[SifRequestParameter](SifRequestParameter("id", "test")))
    assert(!result.success)
    assert(result.statusCode == SifHttpStatusCode.InternalServerError)
    assert(result.exceptions.head.getMessage == "ZoneConfig DELETE method not implemented.")
  }

}
