package org.psesd.srx.services.admin

import org.scalatest.FunSuite

class ServiceTests extends FunSuite {

  test("service") {
    assert(Service.srxService.service.name.equals(Build.name))
    assert(Service.srxService.service.version.equals(Build.version + "." + Build.buildNumber))
    assert(Service.srxService.buildComponents(0).name.equals("java"))
    assert(Service.srxService.buildComponents(0).version.equals(Build.javaVersion))
    assert(Service.srxService.buildComponents(1).name.equals("scala"))
    assert(Service.srxService.buildComponents(1).version.equals(Build.scalaVersion))
    assert(Service.srxService.buildComponents(2).name.equals("sbt"))
    assert(Service.srxService.buildComponents(2).version.equals(Build.sbtVersion))
  }

}
